package com.wis.main.util.core_util.drools.generator;

import com.wis.main.util.core_util.drools.annotation.DroolAction;
import com.wis.main.util.core_util.drools.model.Field;
import com.wis.main.util.core_util.mapper.LambdaUtil;

import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;

public class DroolRule<T, E> {

    private final Class<T> requestType;
    private final Class<E> resultType;
    private final List<String> whenParts = new ArrayList<>();
    private final List<String> thenParts = new ArrayList<>();
    private String packageName = "default_pkg";
    private String ruleName = "Rule_Default";

    private DroolRule(Class<T> requestType, Class<E> resultType) {
        this.requestType = requestType;
        this.resultType = resultType;
    }

    public static <T, E> DroolRule<T, E> of(Class<T> req, Class<E> res) {
        return new DroolRule<>(req, res);
    }

    public DroolRule<T, E> pkg(String pkg) {
        this.packageName = pkg;
        return this;
    }

    public DroolRule<T, E> name(String name) {
        this.ruleName = name;
        return this;
    }

    // ===================== WHEN =====================
    public <R> DroolRule<T, E> whenEquals(Field<T, R> getter, Object value) {
        String method = LambdaUtil.extractMethodName(getter);
        String val = quoteIfString(value);
        whenParts.add(String.format(
                "$req : %s()\n    eval($req.%s() == %s)",
                requestType.getSimpleName(), method, val
        ));
        return this;
    }

    public <R> DroolRule<T, E> whenNotEquals(Field<T, R> getter, Object value) {
        String method = LambdaUtil.extractMethodName(getter);
        String val = quoteIfString(value);
        whenParts.add(String.format(
                "$req : %s()\n    eval($req.%s() != %s)",
                requestType.getSimpleName(), method, val
        ));
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenRange(Field<T, R> getter, String start, String end) {
        String method = LambdaUtil.extractMethodName(getter);
        whenParts.add(String.format(
                "$req : %s()\n    eval($req.%s() >= %s && $req.%s() <= %s)",
                requestType.getSimpleName(), method, start, method, end
        ));
        return this;
    }

    public <R> DroolRule<T, E> whenIn(Field<T, R> getter, List<?> values) {
        String method = LambdaUtil.extractMethodName(getter);
        String joined = values.stream()
                .map(this::quoteIfString)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        whenParts.add(String.format(
                "$req : %s()\n    eval(java.util.List.of(%s).contains($req.%s()))",
                requestType.getSimpleName(), joined, method
        ));
        return this;
    }

    /** whenNotNull - kiểm tra khác null */
    public <R> DroolRule<T, E> whenNotNull(Field<T, R> getter) {
        String method = LambdaUtil.extractMethodName(getter);
        whenParts.add(String.format(
                "$req : %s()\n    eval($req.%s() != null)",
                requestType.getSimpleName(), method
        ));
        return this;
    }

    /** whenCustom - nhận thẳng biểu thức eval */
    public DroolRule<T, E> whenCustom(String evalExpression) {
        whenParts.add(String.format(
                "$req : %s()\n    eval(%s)",
                requestType.getSimpleName(), evalExpression
        ));
        return this;
    }

    // ===================== THEN =====================

    /** then - dùng setter thật (method reference) */
    public <V> DroolRule<T, E> then(DroolAction<E, V> setter, V value) {
        try {
            SerializedLambda lambda = LambdaUtil.getSerializedLambda(setter);
            String implMethod = lambda.getImplMethodName();

            if (!implMethod.startsWith("set")) {
                throw new IllegalArgumentException("Method must start with 'set' but was " + implMethod);
            }

            String action = String.format("$result.%s(\"%s\");", implMethod, value);
            thenParts.add(action);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse setter lambda: " + e.getMessage(), e);
        }
        return this;
    }

    /** thenRaw - chèn thẳng code Drools (nếu muốn) */
    public DroolRule<T, E> thenRaw(String droolsCode) {
        thenParts.add(droolsCode.trim());
        return this;
    }

    // ===================== BUILD =====================

    public String build() {
        String whenBlock = String.join("\n        ", whenParts);
        String thenBlock = String.join("\n        ", thenParts);

        return String.format("""
                package %s;

                import %s;
                import %s;

                rule "%s"
                    when
                        %s
                        $result : %s(boxCode == null)
                    then
                        %s
                        update($result);
                end
                """,
                packageName,
                requestType.getName(),
                resultType.getName(),
                ruleName,
                whenBlock,
                resultType.getSimpleName(),
                thenBlock
        );
    }

    // ===================== UTIL =====================

    private String quoteIfString(Object val) {
        if (val instanceof String) {
            return "\"" + val + "\"";
        }
        return String.valueOf(val);
    }
}
