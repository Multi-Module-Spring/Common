package com.wis.main.util.core_util;

import com.wis.i18n.TranslateCommon;
import com.wis.i18n.exception.TranslateCommonException;
import com.wis.main.util.core_util.date.DateTimePattern;
import com.wis.main.util.core_util.date.DateTimeUtil;
import com.wis.main.util.core_util.i18n.I18nResolver;
import com.wis.main.util.core_util.language.LanguageUtil;
import com.wis.main.util.core_util.mapper.LambdaUtil;
import com.wis.main.util.core_util.number.impl.*;
import com.wis.main.util.core_util.string.StringUtil;
import com.wis.main.util.message.MessageUtil;
import com.wis.main.util.core_util.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class CoreBean {

    @Autowired
    protected StringUtil stringUtil;

    @Autowired
    protected IntegerUtil integerUtil;

    @Autowired
    protected LongUtil longUtil;

    @Autowired
    protected DateTimeUtil dateTimeUtil;

    @Autowired
    protected DoubleUtil doubleUtil;

    @Autowired
    protected BigDecimalUtil bigDecimalUtil;

    @Autowired
    protected BigIntegerUtil bigIntegerUtil;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected Mapper mapper;

    @Autowired
    protected LanguageUtil languageUtil;

    @Autowired
    protected I18nResolver i18nResolver;


    protected <V> String value(V v) {
        if (v == null) {
            return StringUtil.BLANK;
        }
        if (v instanceof LocalDateTime) {
            LocalDateTime vv = (LocalDateTime) v;
            return dateTimeUtil.format(vv, DateTimePattern.YYYYMMDDHHMISSFFF);
        }
        if (v instanceof LocalDate) {
            LocalDate vv = (LocalDate) v;
            return dateTimeUtil.format(vv, DateTimePattern.YYYYMMDD);
        }
        return stringUtil.parse(v);
    }

    protected <V> V verifyNotNull(Supplier<V> supplier) {
        try {
            V value = supplier.get();
            if (value == null) {
                String lambda = supplier.toString();
                String fieldName = LambdaUtil.extractLambdaName(lambda);
                throw new TranslateCommonException(
                        HttpStatus.BAD_REQUEST,
                        TranslateCommon.CANNOT_BE_NULL,
                        fieldName
                );
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate variable", e);
        }
    }

    public static String buildSelectFromClass(Class<?> clazz, String tableAlias) {
        boolean hasAlias = tableAlias != null && !tableAlias.isBlank();

        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .map(field -> {
                    String columnName = toSnakeCase(field);
                    if (hasAlias) {
                        return tableAlias + "." + columnName + " AS " + tableAlias + "_" + columnName;
                    } else {
                        return columnName;
                    }
                })
                .collect(Collectors.joining(",\n"));
    }

    public static String buildSelectFromClass(Class<?> clazz) {
        return buildSelectFromClass(clazz, null);
    }
    private static String toSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
