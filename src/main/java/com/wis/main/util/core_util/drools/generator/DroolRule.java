package com.wis.main.util.core_util.drools.generator;

import com.wis.main.util.core_util.drools.annotation.DroolAction;
import com.wis.main.util.core_util.drools.model.Field;
import com.wis.main.util.core_util.mapper.LambdaUtil;

import java.lang.invoke.SerializedLambda;
import java.util.*;
import java.util.stream.Collectors;

public class DroolRule<T, E> {

    private final Class<T> requestType;
    private final Class<E> resultType;

    private final List<String> patternConditions = new ArrayList<>();
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
        addCondition(getter, "==", value);
        return this;
    }

    public <R> DroolRule<T, E> whenNotEquals(Field<T, R> getter, Object value) {
        addCondition(getter, "!=", value);
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenGreaterThan(Field<T, R> getter, Object value) {
        addCondition(getter, ">", value);
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenGreaterOrEqual(Field<T, R> getter, Object value) {
        addCondition(getter, ">=", value);
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenLessThan(Field<T, R> getter, Object value) {
        addCondition(getter, "<", value);
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenLessOrEqual(Field<T, R> getter, Object value) {
        addCondition(getter, "<=", value);
        return this;
    }

    public <R extends Number & Comparable<R>> DroolRule<T, E> whenRange(Field<T, R> getter, Object start, Object end) {
        String field = LambdaUtil.extractFieldName(getter);
        patternConditions.add(String.format("%s >= %s, %s <= %s", field, start, field, end));
        return this;
    }

    public <R> DroolRule<T, E> whenIn(Field<T, R> getter, List<?> values) {
        String field = LambdaUtil.extractFieldName(getter);
        String joined = values.stream().map(this::quoteIfString).collect(Collectors.joining(", "));
        patternConditions.add(String.format("%s in (%s)", field, joined));
        return this;
    }

    public <R> DroolRule<T, E> whenNotIn(Field<T, R> getter, List<?> values) {
        String field = LambdaUtil.extractFieldName(getter);
        String joined = values.stream().map(this::quoteIfString).collect(Collectors.joining(", "));
        patternConditions.add(String.format("%s not in (%s)", field, joined));
        return this;
    }

    public <R> DroolRule<T, E> whenMatches(Field<T, R> getter, String regex) {
        String field = LambdaUtil.extractFieldName(getter);
        patternConditions.add(String.format("%s matches %s", field, quoteIfString(regex)));
        return this;
    }

    public <R> DroolRule<T, E> whenContains(Field<T, R> getter, String substring) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s().contains(%s)", method, quoteIfString(substring)));
        return this;
    }

    public <R> DroolRule<T, E> whenNotNull(Field<T, R> getter) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() != null", method));
        return this;
    }

    public <R> DroolRule<T, E> whenIsNull(Field<T, R> getter) {
        String method = "get" + capitalize(LambdaUtil.extractFieldName(getter));
        evalConditions.add(String.format("$req.%s() == null", method));
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
            thenParts.add(String.format("$result.%s(%s);", implMethod, valStr));
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
        // combine pattern conditions + eval conditions
        String patternStr = String.join(", ", patternConditions);
        String evalStr = evalConditions.stream()
                .map(s -> "eval(" + s + ")")
                .collect(Collectors.joining("\n        "));

        String whenBlock = String.format(
                "$req : %s(%s)\n        %s\n        $result : %s()",
                requestType.getSimpleName(),
                patternStr,
                evalStr,
                resultType.getSimpleName()
        );

        String thenBlock = String.join("\n        ", thenParts);

        return String.format("""
                        rule "%s"
                            when
                                %s
                            then
                                %s
                                update($result);
                        end
                        """,
                ruleName,
                whenBlock.trim(),
                thenBlock
        );
    }

    public String buildImport() {
        return String.format("""
                        package %s;
                        
                        import %s;
                        import %s;
                        
                        """,
                packageName,
                requestType.getName(),
                resultType.getName()
        );
    }

    // ===================== UTIL =====================

    private void addCondition(Field<T, ?> getter, String operator, Object value) {
        String field = LambdaUtil.extractFieldName(getter);
        patternConditions.add(String.format("%s %s %s", field, operator, quoteIfString(value)));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String quoteIfString(Object val) {
        if (val instanceof String) return "\"" + val + "\"";
        return String.valueOf(val);
    }
}
