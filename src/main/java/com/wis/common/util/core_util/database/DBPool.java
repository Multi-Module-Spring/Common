package com.wis.common.util.core_util.database;

import com.wis.common.exception.ServiceException;
import com.wis.common.model.core.PagingInfo;
import com.wis.common.util.core_util.mapper.Mapper;
import com.wis.common.util.core_util.paging.PagingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DBPool {

    private final DatabaseUtil databaseUtil;
    private final Mapper mapper;
    private final PagingContext pagingContext;

    public int executeUpdate(String sql, Class<?> clazz, List<Object> params) {
        sql = convertPostgresStyle(sql, params.size());
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params.toArray());

//            log.info("[SQL_EXECUTE] Called from: {}", getCallerInfo());
//            log.info("[SQL_EXECUTE] Target class: {}", clazz.getSimpleName());
//            log.info("[SQL_EXECUTE] SQL: {}", sql);
//            log.info("[SQL_EXECUTE] Params: {}", params);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw ServiceException.withDetail("BAD_REQUEST", e.getMessage(), null);
        }
    }

    public <T> List<T> executeQuery(String sql, Class<T> clazzTobeMapped, List<Object> params) {
        return executeQuery(sql, clazzTobeMapped, params.toArray());
    }

    public <T> List<T> executeQuery(String sql, Class<T> clazzTobeMapped, Object... params) {
        if (pagingContext.get() != null) {
            sql = applyPagingWithCount(sql);
        }
        sql = convertPostgresStyle(sql, params.length);

        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> rows = new ArrayList<>();
            Integer totalCount = null;

            while (rs.next()) {
                if (totalCount == null) {
                    try {
                        totalCount = rs.getInt("total_count");
                        pagingContext.setTotalCount(totalCount);
                    } catch (SQLException ignored) {
                    }
                }
                rows.add(mapRowToMap(rs));
            }

//            log.info("[SQL_EXECUTE] Called from: {}", getCallerInfo());
//            log.info("[SQL_EXECUTE] SQL: {}", sql);
//            log.info("[SQL_EXECUTE] Params: {}", Arrays.toString(params));

            return rows.stream()
                    .map(mapper.mapTo(clazzTobeMapped))
                    .toList();
        } catch (SQLException e) {
            throw ServiceException.withDetail("CONNECTION", e.getMessage(), null);
        }
    }

    public void executeNonQuery(String sql, Object... params) {
        sql = convertPostgresStyle(sql, params.length);
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            stmt.execute();

//        log.info("[SQL_EXECUTE_NONQUERY] SQL: {}", sql);
//        log.info("[SQL_EXECUTE_NONQUERY] Params: {}", Arrays.toString(params));

        } catch (SQLException e) {
            throw ServiceException.withDetail("CONNECTION", e.getMessage(), null);
        }
    }


    public <T> T executeQueryUnique(String sql, Class<T> clazzTobeMapped, Object... params) {
        List<T> results = executeQuery(sql, clazzTobeMapped, params);
        if (results.size() > 1) {
            throw ServiceException.of(HttpStatus.BAD_REQUEST, "MORE_THAN_ONE_RESULT");
        }
        return results.size() == 1 ? results.getFirst() : null;
    }

    public <T> T executeQueryUnique(String sql, Class<T> clazzTobeMapped, List<Object> params) {
        return executeQueryUnique(sql, clazzTobeMapped, params.toArray());
    }

    private String convertPostgresStyle(String sql, int paramCount) {
        String converted = sql;
        for (int i = 1; i <= paramCount; i++) {
            converted = converted.replace("$" + i, "?");
        }
        return converted;
    }

    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    private Map<String, Object> mapRowToMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnLabel(i);
            Object value = rs.getObject(i);

            if (value instanceof PGobject pgObj && "jsonb".equals(pgObj.getType())) {
                try {
                    value = mapper.mapTo(pgObj.getValue(), Map.class);
                } catch (Exception e) {
                    throw new SQLException("Failed to parse JSONB column " + columnName, e);
                }
            }

            if (value instanceof String str) {
                if (str.matches("\\d{4}-\\d{2}-\\d{2}T.*[+-]\\d{2}:\\d{2}")) {
                    try {
                        value = OffsetDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                .toLocalDateTime();
                    } catch (Exception ignore) {
                        // nếu parse fail thì để nguyên string
                    }
                }
            }

            // convert snake_case -> camelCase
            String camelName = toCamelCase(columnName);
            map.put(camelName, value);
        }
        return map;
    }


    private String toCamelCase(String snake) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : snake.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    protected String applyPaging(String sql) {
        PagingInfo paging = pagingContext.get();
        if (paging != null) {
            log.debug("[PAGING_APPLY] page: {}, pageSize: {}, limit: {}, offset: {}",
                    paging.getPage(), paging.getPageSize(),
                    paging.getLimit(), paging.getOffset());

            return sql + " LIMIT " + paging.getLimit() + " OFFSET " + paging.getOffset();
        }
        return sql;
    }

    protected String applyPagingWithCount(String baseSql) {
        PagingInfo paging = pagingContext.get();
        if (paging == null) {
            return "SELECT sub_data.*, COUNT(*) OVER() AS total_count FROM (" + baseSql + ") sub_data";
        }
        return "SELECT sub_data.*, COUNT(*) OVER() AS total_count " +
                "FROM (" + baseSql + ") sub_data " +
                "LIMIT " + paging.getLimit() +
                " OFFSET " + paging.getOffset();
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (!className.equals(this.getClass().getName()) && !className.startsWith("java.")) {
                String fileName = element.getFileName(); // vd: Service.java
                int lineNumber = element.getLineNumber();
                return className + "." + element.getMethodName() + "(" + fileName + ":" + lineNumber + ")";
            }
        }
        return "Unknown Source";
    }
}
