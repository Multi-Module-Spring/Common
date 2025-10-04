package com.wis.util.core_util.string.impl;


import com.wis.util.core_util.number.impl.IntegerUtil;
import com.wis.util.core_util.string.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StringUtilImpl implements StringUtil {
    @Autowired
    protected MessageFormatter messageFormatter;

    protected IntegerUtil integerUtil;

    @Override
    public <V> String parse(V v) {
        if (v == null) {
            return BLANK;
        }
        if (v instanceof String) {
            String s = (String) v;
            return s.isEmpty() ? BLANK : s.trim();
        }
        if (v instanceof Integer) {
            return ((Integer) v).toString();
        }
        if (v instanceof Long) {
            return ((Long) v).toString();
        }
        String s = String.valueOf(v);
        return s.isEmpty() ? BLANK : s.trim();
    }

    @Override
    public String format(String pattern, Object... a) {
        pattern = nvl(pattern);
        return (a == null || isEmpty(pattern)) ? pattern : messageFormatter.get(pattern).format(a);
    }

    @Override
    public String randomKey(int length) {
        StringBuilder s = new StringBuilder();
        Integer min = 0;
        Integer max = 9;
        for (int i = 0; i < length; i++) {
            s.append(integerUtil.random(min, max));
        }
        return s.toString();
    }
}
