package com.wis.util.core_util.date.impl;

import com.wis.util.core_util.date.DateTimeFormatter;
import com.wis.util.core_util.date.DateTimeUtil;
import com.wis.util.core_util.string.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@RequiredArgsConstructor
@Component
public class DateTimeUtilImpl implements DateTimeUtil {
    private final DateTimeFormatter dateTimeFormatter;
    private final StringUtil stringUtil;

    @Override
    public String format(LocalDateTime v, String pattern) {
        String result = stringUtil.BLANK;
        pattern = stringUtil.nvl(pattern);
        if (v == null || stringUtil.isEmpty(pattern)) {
            return result;
        }
        java.time.format.DateTimeFormatter df = dateTimeFormatter.get(pattern);
        result = df == null ? stringUtil.BLANK : v.format(df);
        return result;
    }

    @Override
    public String format(LocalDate v, String pattern) {
        pattern = stringUtil.nvl(pattern);
        if (v == null || stringUtil.isEmpty(pattern)) {
            return stringUtil.BLANK;
        }
        java.time.format.DateTimeFormatter df = dateTimeFormatter.get(pattern);
        return df == null ? stringUtil.BLANK : v.format(df);
    }

    @Override
    public <V> LocalDateTime toLocalDateTime(V v, String pattern) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDateTime) {
            return (LocalDateTime) v;
        }
        if (v instanceof LocalDate) {
            return ((LocalDate) v).atStartOfDay();
        }
        String vv = stringUtil.nvl(v);
        pattern = stringUtil.nvl(pattern);
        if (stringUtil.isEmpty(vv) || stringUtil.isEmpty(pattern)) {
            return null;
        }
        java.time.format.DateTimeFormatter df = dateTimeFormatter.get(pattern);
        return LocalDateTime.parse(vv, df);
    }

    @Override
    public <V> LocalDateTime tryToLocalDateTime(V v, String pattern) {
        try {
            return toLocalDateTime(v, pattern);
        } catch (Exception e) {
            log.debug("Exception {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public <V> LocalDate toLocalDate(V v, String pattern) {
        if (v == null) {
            return null;
        }
        if (v instanceof LocalDate) {
            return (LocalDate) v;
        }
        if (v instanceof LocalDateTime) {
            return ((LocalDateTime) v).toLocalDate();
        }
        String vv = stringUtil.nvl(v);
        pattern = stringUtil.nvl(pattern);
        if (stringUtil.isEmpty(vv) || stringUtil.isEmpty(pattern)) {
            return null;
        }
        java.time.format.DateTimeFormatter df = dateTimeFormatter.get(pattern);
        return LocalDate.parse(vv, df);
    }

    @Override
    public <V> LocalDate tryToLocalDate(V v, String pattern) {
        try {
            return toLocalDate(v, pattern);
        } catch (Exception e) {
            log.debug("Exception {}", e.getMessage(), e);
            return null;
        }
    }
}
