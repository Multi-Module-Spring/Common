package com.wis.util.core_util.string;


import java.util.UUID;


public interface StringUtil {
    String BLANK = "";

    <V> String parse(V v);

    default boolean isEmpty(String s) {
        return s == null || s.isEmpty() || s.trim().isEmpty();
    }

    default boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    default <V> String nvl(V v) {
        return nvl(v, BLANK);
    }

    default <V> String nvl(V v, String nvl) {
        String s = parse(v);
        return (s == null || s.isEmpty()) ? nvl : s;
    }

    default String substring(String v, int length) {
        v = nvl(v);
        if (v.length() > length) {
            v = v.substring(0, length);
        }
        return v;
    }

    default String upper(String s) {
        return nvl(s).toUpperCase();
    }

    default String lower(String s) {
        return nvl(s).toLowerCase();
    }

    default boolean equals(String a, String b) {
        return nvl(a).equals(nvl(b));
    }

    default boolean notEquals(String a, String b) {
        return !equals(a, b);
    }

    default boolean equalsIgnoreCase(String a, String b) {
        return nvl(a).equalsIgnoreCase(nvl(b));
    }

    default boolean notEqualsIgnoreCase(String a, String b) {
        return !equalsIgnoreCase(a, b);
    }

    default String uuid() {
        return UUID.randomUUID().toString();
    }

    String format(String pattern, Object... a);

    String randomKey(int length);
}