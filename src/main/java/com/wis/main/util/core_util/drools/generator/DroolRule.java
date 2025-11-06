package com.wis.main.util.core_util.drools.generator;

import com.wis.main.util.core_util.drools.annotation.DroolAction;
import com.wis.main.util.core_util.drools.model.Field;
import com.wis.main.util.core_util.mapper.LambdaUtil;

import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DroolRule<T, E> {

    private final Class<T> requestType;
    private final Class<E> resultType;
    private final List<String> evalConditions = new ArrayList<>();
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
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() == %s", method, quoteIfString(value)));
        return this;
    }

    public <R> DroolRule<T, E> whenNotEquals(Field<T, R> getter, Object value) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() != %s", method, quoteIfString(value)));
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenRange(Field<T, R> getter, String start, String end) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() >= %s && $req.%s() <= %s", method, start, method, end));
        return this;
    }

    public <R> DroolRule<T, E> whenIn(Field<T, R> getter, List<?> values) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        String joined = values.stream().map(this::quoteIfString).collect(Collectors.joining(", "));
        evalConditions.add(String.format("java.util.List.of(%s).contains($req.%s())", joined, method));
        return this;
    }

    public <R> DroolRule<T, E> whenNotNull(Field<T, R> getter) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() != null", method));
        return this;
    }

    public DroolRule<T, E> whenCustom(String expression) {
        evalConditions.add(expression);
        return this;
    }

    // ===================== THEN =====================

    public <V> DroolRule<T, E> then(DroolAction<E, V> setter, V value) {
        try {
            SerializedLambda lambda = LambdaUtil.getSerializedLambda(setter);
            String implMethod = lambda.getImplMethodName();

            if (!implMethod.startsWith("set")) {
                throw new IllegalArgumentException("Method must start with 'set' but was " + implMethod);
            }

            String valStr = (value instanceof String) ? "\"" + value + "\"" : String.valueOf(value);
            String action = String.format("$result.%s(%s);", implMethod, valStr);
            thenParts.add(action);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse setter lambda: " + e.getMessage(), e);
        }
        return this;
    }

    public DroolRule<T, E> thenRaw(String droolsCode) {
        thenParts.add(droolsCode.trim());
        return this;
    }

    // ===================== BUILD =====================

    public String build() {
        String joinedEval = evalConditions.isEmpty()
                ? ""
                : "eval(" + String.join(" && ", evalConditions) + ")";

        String whenBlock = String.format(
                "$req : %s()%s\n        $result : %s(boxCode == null)",
                requestType.getSimpleName(),
                joinedEval.isEmpty() ? "" : "\n        " + joinedEval,
                resultType.getSimpleName()
        );

        String thenBlock = String.join("\n        ", thenParts);

        return String.format("""
                package %s;

                import %s;
                import %s;

                rule "%s"
                    when
                        %s
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
                thenBlock
        );
    }

    // ===================== UTIL =====================

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String quoteIfString(Object val) {
        if (val instanceof String) return "\"" + val + "\"";
        return String.valueOf(val);
    }
}
