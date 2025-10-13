package com.wis.common.util.core_util.database;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SQLBuilder {
    private final List<String> selectFields = new ArrayList<>();
    private final List<String> joinClauses = new ArrayList<>();
    @Getter
    private final List<Object> params = new ArrayList<>();
    private final StringBuilder sql = new StringBuilder();

    private final Map<Class<?>, String> classToAlias = new HashMap<>();
    private final Map<String, String> aliasToTable = new HashMap<>();
    private final Set<String> replacedSelectAliases = new HashSet<>();

    private String lastJoinClause;
    private int paramIndex = 1;

    public static SQLBuilder build() {
        return new SQLBuilder();
    }

    public SQLBuilder select(Class<?> clazz, String alias) {
        if (alias != null && !alias.isBlank()) {
            classToAlias.put(clazz, alias);
            aliasToTable.put(alias, resolveTableName(clazz));
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = camelToSnake(field.getName());
            String fullAlias = (alias != null && !alias.isBlank()) ? alias + "." + fieldName : fieldName;

            if (!replacedSelectAliases.contains(fullAlias)) {
                selectFields.add(fullAlias);
            }
        }

        return this;
    }


    public SQLBuilder select(Class<?> entityClass, Class<?> fieldLimitClass) {
        return select(entityClass,null, fieldLimitClass);
    }


    public SQLBuilder select(Class<?> entityClass, String alias, Class<?> fieldLimitClass) {
        if (alias != null && !alias.isBlank()) {
            classToAlias.put(entityClass, alias);
            aliasToTable.put(alias, resolveTableName(entityClass));
        }
        if(fieldLimitClass == null) {
            return select(entityClass, alias);
        }

        Set<String> limitFields = Arrays.stream(fieldLimitClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        Field[] entityFields = entityClass.getDeclaredFields();
        boolean hasCommonField = false;

        for (Field field : entityFields) {
            String name = field.getName();
            if (limitFields.contains(name)) {
                hasCommonField = true;
                selectFields.add(
                        (alias != null && !alias.isBlank())
                                ? alias + "." + camelToSnake(name)
                                : camelToSnake(name)
                );
            }
        }

        if (!hasCommonField) {
            throw new IllegalArgumentException(
                    "Class giới hạn `" + fieldLimitClass.getSimpleName() +
                            "` không có field nào trùng với entity `" + entityClass.getSimpleName() + "`"
            );
        }

        return this;
    }



    public SQLBuilder select(Class<?> clazz) {
        return select(clazz, null,null);
    }

    public SQLBuilder select(String column) {
        selectFields.add(column);
        return this;
    }

    public SQLBuilder from(Class<?> clazz) {
        buildSelectIfNeeded();
        String alias = classToAlias.get(clazz);
        String table = resolveTableName(clazz);
        if (alias != null && !alias.isBlank()) {
            sql.append("FROM ").append(table).append(" ").append(alias).append(" ");
        } else {
            sql.append("FROM ").append(table).append(" ");
        }
        return this;
    }
    //WHERE
    public SQLBuilder where(Class<?> clazz, String condition, Object... values) {
        sql.append("WHERE ");
        return appendCondition(clazz, condition+ " = ?", values);
    }

    public SQLBuilder where(String condition, Object... values) {
        return where(null, condition, values);
    }
    //

    //AND
    public SQLBuilder and(Class<?> clazz, String condition, Object... values) {
        sql.append("AND ");
        return appendCondition(clazz, condition+ " = ?", values);
    }

    public SQLBuilder and(String condition, Object... values) {
        return and(null, condition, values);
    }
    //

    //OR
    public SQLBuilder or(Class<?> clazz, String condition, Object... values) {
        sql.append("OR ");
        return appendCondition(clazz, condition, values);
    }

    public SQLBuilder or(String condition, Object... values) {
        return or(null, condition, values);
    }
    //

    //IN
    private SQLBuilder in(Class<?> clazz, String column, Collection<?> values, boolean orCase) {
        if (!sql.toString().contains("WHERE")) {
            sql.append("WHERE ");
        } else {
            sql.append(orCase ? "OR ":"AND ");
        }

        String alias = (clazz != null) ? getAlias(clazz) : detectDefaultAlias();
        String snakeColumn = camelToSnake(column);

        if (alias != null && !alias.isBlank()) {
            sql.append(alias).append(".").append(snakeColumn);
        } else {
            sql.append(snakeColumn);
        }

        sql.append(" = ANY($").append(paramIndex).append(") ");
        params.add(values.toArray());
        paramIndex++;
        return this;
    }


    public SQLBuilder andIn(Class<?> clazz, String column, Collection<?> values) {
        return in(clazz, column, values,false);
    }

    public SQLBuilder andIn(String column, Collection<?> values) {
        return in(null, column, values,false);
    }

    public SQLBuilder orIn(String column, Collection<?> values) {
        return in(null, column, values,true);
    }

    public SQLBuilder orIn(Class<?> clazz,String column, Collection<?> values) {
        return in(clazz, column, values,true);
    }
    //

    public SQLBuilder insertInto(Class<?> clazz, Map<String, Object> values) {
        String table = resolveTableName(clazz);
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        int count = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (count++ > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(camelToSnake(entry.getKey()));
            placeholders.append("$").append(paramIndex);
            params.add(entry.getValue());
            paramIndex++;
        }

        sql.append("INSERT INTO ").append(table).append(" (")
                .append(columns).append(") VALUES (")
                .append(placeholders).append(") ");

        return this;
    }

    public SQLBuilder insertIntoMany(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return this;
        Set<String> columns = rows.get(0).keySet();
        sql.append("INSERT INTO (")
                .append(columns.stream().map(this::camelToSnake).collect(Collectors.joining(", ")))
                .append(") VALUES ");

        int rowCount = 0;
        for (Map<String, Object> row : rows) {
            if (rowCount++ > 0) sql.append(", ");
            sql.append("(");
            int colCount = 0;
            for (String col : columns) {
                if (colCount++ > 0) sql.append(", ");
                sql.append("$").append(paramIndex);
                params.add(row.get(col));
                paramIndex++;
            }
            sql.append(")");
        }
        sql.append(" ");
        return this;
    }

    public SQLBuilder update(Class<?> clazz, Map<String, Object> values) {
        return update(clazz, null, values);
    }

    public SQLBuilder update(Class<?> clazz, String alias, Map<String, Object> values) {
        String table = resolveTableName(clazz);

        sql.append("UPDATE ").append(table);

        if (alias != null && !alias.isBlank()) {
            sql.append(" ").append(alias);
            classToAlias.put(clazz, alias);
            aliasToTable.put(alias, table);
        } else {
            classToAlias.putIfAbsent(clazz, null);
        }

        sql.append(" SET ");

        int count = 0;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (count++ > 0) sql.append(", ");
            sql.append(camelToSnake(entry.getKey()))
                    .append(" = $").append(paramIndex).append(" ");
            params.add(entry.getValue());
            paramIndex++;
        }

        return this;
    }

    public SQLBuilder deleteFrom(Class<?> clazz) {
        return deleteFrom(clazz, null);
    }


    public SQLBuilder deleteFrom(Class<?> clazz, String alias) {
        String table = resolveTableName(clazz);
        sql.append("DELETE FROM ").append(table);
        if (alias != null && !alias.isBlank()) {
            sql.append(" ").append(alias);
            classToAlias.put(clazz, alias);
            aliasToTable.put(alias, table);
        }
        sql.append(" ");
        return this;
    }


    public SQLBuilder orderBy(String field) {
        return orderBy(null, field);
    }

    public SQLBuilder orderByDesc(String field) {
        return orderByDesc(null, field);
    }


    public SQLBuilder orderBy(Class<?> clazz, String field) {
        String alias = classToAlias.get(clazz);
        String column = camelToSnake(field);

        if (alias != null && !alias.isBlank()) {
            sql.append("ORDER BY ").append(alias).append(".").append(column).append(" ");
        } else {
            sql.append("ORDER BY ").append(column).append(" ");
        }
        return this;
    }


    public SQLBuilder orderByDesc(Class<?> clazz, String field) {
        String alias = classToAlias.get(clazz);
        String column = camelToSnake(field);

        if (alias != null && !alias.isBlank()) {
            sql.append("ORDER BY ").append(alias).append(".").append(column).append(" DESC ");
        } else {
            sql.append("ORDER BY ").append(column).append(" DESC ");
        }
        return this;
    }

    private SQLBuilder appendCondition(Class<?> clazz, String condition, Object... values) {
        String alias = (clazz != null) ? getAlias(clazz) : detectDefaultAlias();

        if (!condition.contains(".")) {
            condition = Arrays.stream(condition.split(","))
                    .map(String::trim)
                    .map(c -> {
                        int spaceIndex = c.indexOf(" ");
                        if (spaceIndex > 0) {
                            String field = c.substring(0, spaceIndex);
                            String rest = c.substring(spaceIndex);
                            field = toSnakeCase(field);
                            return (alias != null && !alias.isBlank())
                                    ? alias + "." + field + rest
                                    : field + rest;
                        }
                        c = toSnakeCase(c);
                        return (alias != null && !alias.isBlank())
                                ? alias + "." + c
                                : c;
                    })
                    .collect(Collectors.joining(", "));
        }

        // Replace ? placeholders with $n parameters
        for (Object value : values) {
            int index = condition.indexOf("?");
            if (index >= 0) {
                condition = condition.substring(0, index) + "$" + paramIndex + condition.substring(index + 1);
                params.add(value);
                paramIndex++;
            }
        }

        sql.append(condition).append(" ");
        return this;
    }

    private static String toSnakeCase(String input) {
        if (input == null || input.isBlank()) return input;
        String result = input
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase();
        return result;
    }


    private String getAlias(Class<?> clazz) {
        String alias = classToAlias.get(clazz);
        if (alias == null || alias.isBlank()) {
            throw new IllegalStateException("Chưa đăng ký alias cho class: " + clazz.getSimpleName());
        }
        return alias;
    }

    private String detectDefaultAlias() {
        if (!classToAlias.isEmpty()) {
            return classToAlias.values().iterator().next();
        }
        return null;
    }



    public SQLBuilder join(Class<?> clazz) {
        return addJoin("JOIN", clazz);
    }

    public SQLBuilder leftJoin(Class<?> clazz) {
        return addJoin("LEFT JOIN", clazz);
    }

    public SQLBuilder rightJoin(Class<?> clazz) {
        return addJoin("RIGHT JOIN", clazz);
    }

    public SQLBuilder fullJoin(Class<?> clazz) {
        return addJoin("FULL JOIN", clazz);
    }

    public SQLBuilder crossJoin(Class<?> clazz) {
        return addJoin("CROSS JOIN", clazz);
    }

    private SQLBuilder addJoin(String type, Class<?> clazz) {
        String alias = classToAlias.get(clazz);
        String table = resolveTableName(clazz);

        if (alias != null && !alias.isBlank()) {
            lastJoinClause = type + " " + table + " " + alias;
        } else {
            lastJoinClause = type + " " + table;
        }

        joinClauses.add(lastJoinClause);
        return this;
    }

    public SQLBuilder groupBy(Class<?> clazz, String... fields) {
        String alias = classToAlias.get(clazz);

        String clause = Arrays.stream(fields)
                .map(this::camelToSnake)
                .map(f -> (alias != null && !alias.isBlank()) ? alias + "." + f : f)
                .collect(Collectors.joining(", "));

        sql.append("GROUP BY ").append(clause).append(" ");
        return this;
    }

    public SQLBuilder selectIfElse(Class<?> clazz, String field, Map<String, Object> whenThenMap, Object elseValue, String alias) {
        String tableAlias = (clazz != null) ? getAlias(clazz) : null;
        String snakeField = camelToSnake(field);
        String snakeAlias = camelToSnake(alias);

        String fullField = (tableAlias != null && !tableAlias.isBlank()) ? tableAlias + "." + snakeField : snakeField;
        String fullAliasField = (tableAlias != null && !tableAlias.isBlank()) ? tableAlias + "." + snakeAlias : snakeAlias;

        replacedSelectAliases.add(fullAliasField);

        StringBuilder builder = new StringBuilder("CASE ");
        for (Map.Entry<String, Object> entry : whenThenMap.entrySet()) {
            builder.append("WHEN ")
                    .append(fullField)
                    .append(" = ")
                    .append(formatSqlValue(entry.getKey()))
                    .append(" THEN ")
                    .append(formatSqlValue(entry.getValue()))
                    .append(" ");
        }

        builder.append("ELSE ").append(formatSqlValue(elseValue))
                .append(" END AS ").append(snakeAlias);

        selectFields.add(builder.toString());
        return this;
    }


    private String formatSqlValue(Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            return "'" + value.toString() + "'";
        }
    }



    public SQLBuilder on(String condition) {
        if (lastJoinClause != null) {
            int index = joinClauses.lastIndexOf(lastJoinClause);
            lastJoinClause += " ON " + condition;
            joinClauses.set(index, lastJoinClause);
            lastJoinClause = null;
        }
        return this;
    }

    public String getSql() {
        buildSelectIfNeeded();

        StringBuilder finalSql = new StringBuilder();
        String joins = String.join(" ", joinClauses).trim();
        String body = sql.toString().trim().replaceAll("\\s+", " ");

        int fromIndex = body.indexOf("FROM");
        if (fromIndex == -1) {
            // Nếu chưa có FROM thì trả về nguyên SELECT + JOIN (nếu có)
            return body + (joins.isEmpty() ? "" : " " + joins);
        }

        String beforeFrom = body.substring(0, fromIndex).trim();
        String afterFrom = body.substring(fromIndex).trim();

        if (!joins.isEmpty()) {
            int firstClauseEnd = findNextClause(afterFrom);
            String fromPart = firstClauseEnd > 0
                    ? afterFrom.substring(0, firstClauseEnd).trim()
                    : afterFrom.trim();
            String restPart = firstClauseEnd > 0
                    ? afterFrom.substring(firstClauseEnd).trim()
                    : "";

            finalSql.append(beforeFrom)
                    .append(" ")
                    .append(fromPart)
                    .append(" ")
                    .append(joins)
                    .append(" ");

            if (!restPart.isEmpty()) {
                finalSql.append(restPart).append(" ");
            }
        } else {
            finalSql.append(body);
        }

        return finalSql.toString().trim().replaceAll("\\s+", " ");
    }


    private int findNextClause(String afterFrom) {
        List<String> keywords = Arrays.asList("WHERE ", "GROUP BY", "ORDER BY", "LIMIT ", "OFFSET ");
        int minIndex = afterFrom.length();
        for (String keyword : keywords) {
            int idx = afterFrom.toUpperCase().indexOf(keyword);
            if (idx > 0 && idx < minIndex) {
                minIndex = idx;
            }
        }
        return (minIndex == afterFrom.length()) ? -1 : minIndex;
    }



    private void buildSelectIfNeeded() {
        if (!selectFields.isEmpty() && !sql.toString().contains("SELECT")) {
            selectFields.removeIf(field -> {
                String normalized = field.trim().split(" ")[0];
                return replacedSelectAliases.contains(normalized);
            });

            sql.insert(0, "SELECT " + String.join(", ", selectFields) + " ");
        }
    }
    private static final Set<String> DANGEROUS_IDENTIFIERS = Set.of(
            "user", "order", "select", "group", "where", "table", "limit", "offset"
    );
    private boolean isPostgresKeywordOrDangerous(String name) {
        return DANGEROUS_IDENTIFIERS.contains(name.toLowerCase());
    }

    private String resolveTableName(Class<?> clazz) {
        String rawName = camelToSnake(clazz.getSimpleName());
        if (isPostgresKeywordOrDangerous(rawName)) {
            return "\"" + rawName + "\"";
        }
        return rawName;
    }


    private String camelToSnake(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public SQLBuilder returning(String... columns) {
        if (columns.length > 0) {
            sql.append("RETURNING ").append(
                    Arrays.stream(columns).map(this::camelToSnake).collect(Collectors.joining(", "))
            ).append(" ");
        } else {
            sql.append("RETURNING * ");
        }
        return this;
    }

    public SQLBuilder merge(Class<?> clazz, Map<String, Object> values, String conflictKey, List<String> updateFields) {
        insertInto(clazz, values);
        sql.append("ON CONFLICT (").append(camelToSnake(conflictKey)).append(") DO UPDATE SET ");

        List<String> updates = new ArrayList<>();
        for (String field : updateFields) {
            updates.add(camelToSnake(field) + " = EXCLUDED." + camelToSnake(field));
        }
        sql.append(String.join(", ", updates)).append(" ");
        return this;
    }


    public SQLBuilder truncate(Class<?> clazz) {
        sql.append("TRUNCATE TABLE ").append(resolveTableName(clazz)).append(" ");
        return this;
    }

    public SQLBuilder exists(String subQuery) {
        sql.append("WHERE EXISTS (").append(subQuery).append(") ");
        return this;
    }

    public SQLBuilder notExists(String subQuery) {
        sql.append("WHERE NOT EXISTS (").append(subQuery).append(") ");
        return this;
    }

    public SQLBuilder union(SQLBuilder other) {
        sql.append(" UNION ").append(other.getSql()).append(" ");
        params.addAll(other.getParams());
        return this;
    }

    public SQLBuilder unionAll(SQLBuilder other) {
        sql.append(" UNION ALL ").append(other.getSql()).append(" ");
        params.addAll(other.getParams());
        return this;
    }

}
