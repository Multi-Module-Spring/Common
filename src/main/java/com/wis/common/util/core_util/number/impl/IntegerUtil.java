package com.wis.common.util.core_util.number.impl;

import com.wis.common.util.core_util.number.NumberUtil;
import com.wis.common.util.core_util.string.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@RequiredArgsConstructor
@Component
public class IntegerUtil implements NumberUtil<Integer> {
    public static final Integer ZERO = 0;

    private final StringUtil stringUtil;

    @Override
    public <V> Integer parse(V v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).intValue();
        }
        if (v instanceof BigInteger) {
            return ((BigInteger) v).intValue();
        }
        if (v instanceof Double) {
            return ((Double) v).intValue();
        }
        if (v instanceof Float) {
            return ((Float) v).intValue();
        }
        if (v instanceof Long) {
            return ((Long) v).intValue();
        }
        if (v instanceof Integer) {
            return (Integer) v;
        }
        String s = stringUtil.nvl(v);
        if (stringUtil.isEmpty(s)) {
            return null;
        }
        return Integer.parseInt(s);
    }

    @Override
    public <V> Integer nvl(V v) {
        Integer n = parse(v);
        return nvl(n, ZERO);
    }

    @Override
    public boolean equals(Integer a, Integer b) {
        a = nvl(a);
        b = nvl(b);
        return a.equals(b) || a.compareTo(b) == 0;
    }

    @Override
    public boolean lessThan(Integer a, Integer b) {
        return nvl(a).compareTo(nvl(b)) < 0;
    }

    @Override
    public boolean greaterThan(Integer a, Integer b) {
        return nvl(a).compareTo(nvl(b)) > 0;
    }

    @Override
    public Integer abs(Integer n) {
        return n == null ? null : Math.abs(n);
    }

    @Override
    public Integer round(Integer n, int scale) {
        if (n == null) {
            return null;
        }
        double ten = 10D;
        double pow = Math.pow(ten, scale);
        return parse(Math.round(n * pow) / pow);
    }

    @Override
    public Integer random(Integer min, Integer max) {
        min = nvl(min);
        max = nvl(max);
        return min + random.nextInt((max - min) + 1);
    }

    @Override
    public Integer sum(Collection<Integer> c) {
        return c == null ? ZERO : c.stream().reduce(ZERO, Integer::sum);
    }

    @Override
    public Integer min(Collection<Integer> c) {
        return c == null ? null : c.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Integer max(Collection<Integer> c) {
        return c == null ? null : c.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
