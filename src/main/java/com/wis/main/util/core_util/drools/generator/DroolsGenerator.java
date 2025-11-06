package com.wis.main.util.core_util.drools.generator;

import com.wis.main.util.core_util.drools.annotation.DroolRuleConfig;
import com.wis.main.util.core_util.string.StringUtil;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class DroolsGenerator<T> {

    private final Path baseDir;

    public DroolsGenerator(String baseRulePath) {
        this.baseDir = Paths.get(baseRulePath);
    }

    public void generateFile(String subFix, List<T> configs) throws IOException {
        if (configs == null || configs.isEmpty()) {
            System.out.println("[Drools] ⚠️ No data to generate " + subFix);
            return;
        }

        Class<?> clazz = configs.getFirst().getClass();
        DroolRuleConfig meta = clazz.getAnnotation(DroolRuleConfig.class);
        if (meta == null) {
            throw new IllegalStateException("[Drools] Missing @DroolRuleConfig on class " + clazz.getSimpleName());
        }

        subFix = meta.packageName() + "_" + subFix + ".drl";

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(meta.packageName()).append(";\n\n");
        sb.append(meta.imports()).append("\n\n");

        for (T config : configs) {
            String ruleContent = buildRule(meta, config);
            sb.append(ruleContent).append("\n\n");
        }

        Files.createDirectories(baseDir);
        Path filePath = baseDir.resolve(subFix);
        Files.writeString(filePath, sb.toString(), StandardCharsets.UTF_8);

        System.out.println("[Drools] Generated annotated rule file: " + filePath.toAbsolutePath());
    }

    private String buildRule(DroolRuleConfig meta, Object obj) {
        try {
            if (meta.ruleSource() != void.class) {
                Class<?> src = meta.ruleSource();
                Method method = null;

                for (Method m : src.getDeclaredMethods()) {
                    if (m.getName().equals("buildRule")) {
                        method = m;
                        break;
                    }
                }

                if (method == null) {
                    throw new IllegalStateException("[Drools] Không tìm thấy buildRule trong " + src.getSimpleName());
                }

                Object ruleObj;
                if (method.getParameterCount() == 1) {
                    ruleObj = method.invoke(null, obj);
                } else {
                    ruleObj = method.invoke(null);
                }

                if (!(ruleObj instanceof DroolRule<?, ?> rule)) {
                    throw new IllegalStateException("[Drools] buildRule() phải trả về DroolRule<?, ?>");
                }
                rule.pkg(meta.packageName());

                String full = rule.build();
                int startIndex = full.indexOf("rule ");
                if (startIndex == -1) {
                    throw new RuntimeException("[Drools] Không tìm thấy rule trong output!");
                }
                return full.substring(startIndex).trim();
            }

            return StringUtil.BLANK;

        } catch (Exception e) {
            throw new RuntimeException("[Drools] 🔥 Error building rule: " + e.getMessage(), e);
        }
    }


    private static Method getMethod(DroolRuleConfig meta) {
        Class<?> src = meta.ruleSource();
        Method method = null;

        for (Method m : src.getDeclaredMethods()) {
            if (m.getName().equals("buildRule")) {
                method = m;
                break;
            }
        }

        if (method == null) {
            throw new IllegalStateException("[Drools] Không tìm thấy hàm buildRule trong " + src.getSimpleName());
        }
        return method;
    }

    private String getRouteId(Object obj) {
        try {
            Field f = obj.getClass().getDeclaredField("routeLineId");
            f.setAccessible(true);
            Object val = f.get(obj);
            return val == null ? "UNKNOWN" : val.toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String getSafeIdentifier(Object obj, String declaredName) {
        try {
            var field = obj.getClass().getDeclaredField(declaredName);
            field.setAccessible(true);
            Object val = field.get(obj);
            if (val != null) {
                return val.toString().replaceAll("[^A-Za-z0-9]", "_");
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return String.valueOf(obj.hashCode());
    }

    private String replacePlaceholders(String template, Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                String placeholder = "{" + field.getName() + "}";
                template = template.replace(placeholder, val == null ? "null" : val.toString());
            } catch (IllegalAccessException ignored) {
            }
        }
        return template;
    }
}
