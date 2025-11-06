package com.wis.main.util.core_util.drools.generator;

import com.wis.main.util.core_util.drools.annotation.DroolRuleConfig;

import java.io.IOException;
import java.lang.reflect.Field;
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
            System.out.println("[Drools] No data to generate " + subFix);
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

        int index = 0;
        for (T config : configs) {
            String rule = buildRule(meta, config, index++);
            sb.append(rule).append("\n\n");
        }

        Files.createDirectories(baseDir);
        Path filePath = baseDir.resolve(subFix);
        Files.writeString(filePath, sb.toString(), StandardCharsets.UTF_8);

        System.out.println("[Drools] Generated annotated rule file: " + filePath.toAbsolutePath());
    }

    private String buildRule(DroolRuleConfig meta, Object obj, int index) {
        String when = replacePlaceholders(meta.when(), obj);
        String then = replacePlaceholders(meta.then(), obj);

        String idPart = getSafeIdentifier(obj, meta.packageName());
        String ruleName = meta.ruleNamePrefix() + idPart + "_" + index;

        return String.format("""
        rule "%s"
            when
                %s
            then
                %s
        end
        """, ruleName, when, then);
    }

    private String getSafeIdentifier(Object obj, String declaredName) {
        try {
            var field = obj.getClass().getDeclaredField(declaredName);
            field.setAccessible(true);
            Object val = field.get(obj);
            if (val != null) {
                return val.toString().replaceAll("[^A-Za-z0-9]", "_");
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return String.valueOf(obj.hashCode());
    }

    private String replacePlaceholders(String template, Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                String placeholder = "{" + field.getName() + "}";
                template = template.replace(placeholder, val == null ? "null" : val.toString());
            } catch (IllegalAccessException ignored) {}
        }
        return template;
    }
}
