package com.wis.util.core_util.number.impl;

import com.wis.util.core_util.number.NumberUtil;
import com.wis.util.core_util.string.StringUtil;
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
public class LongUtil implements NumberUtil<Long> {
    public static final Long ZERO = 0L;

    private final StringUtil stringUtil;

    @Override
    public <V> Long parse(V v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).longValue();
        }
        if (v instanceof BigInteger) {
            return ((BigInteger) v).longValue();
        }
        if (v instanceof Double) {
            return ((Double) v).longValue();
        }
        if (v instanceof Float) {
            return ((Float) v).longValue();
        }
        if (v instanceof Long) {
            return (Long) v;
        }
        if (v instanceof Integer) {
            return ((Integer) v).longValue();
        }
        String s = stringUtil.nvl(v);
        if (stringUtil.isEmpty(s)) {
            return null;
        }
        return Long.parseLong(s);
    }

    @Override
    public <V> Long nvl(V v) {
        Long n = parse(v);
        return nvl(n, ZERO);
    }

    @Override
    public boolean equals(Long a, Long b) {
        a = nvl(a);
        b = nvl(b);
        return a.equals(b) || a.compareTo(b) == 0;
    }

    @Override
    public boolean lessThan(Long a, Long b) {
        return nvl(a).compareTo(nvl(b)) < 0;
    }

    @Override
    public boolean greaterThan(Long a, Long b) {
        return nvl(a).compareTo(nvl(b)) > 0;
    }

    @Override
    public Long abs(Long n) {
        return n == null ? null : Math.abs(n);
    }

    @Override
    public Long round(Long n, int scale) {
        if (n == null) {
            return null;
        }
        double ten = 10D;
        double pow = Math.pow(ten, scale);
        return parse(Math.round(n * pow) / pow);
    }

    @Override
    public Long random(Long min, Long max) {
        min = nvl(min);
        max = nvl(max);
        Long a = ((Double) random.nextDouble()).longValue();
        Long b = max - min;
        return min + (a * b);
    }

    @Override
    public Long sum(Collection<Long> c) {
        return c == null ? ZERO : c.stream().reduce(ZERO, Long::sum);
    }

    @Override
    public Long min(Collection<Long> c) {
        return c == null ? null : c.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Long max(Collection<Long> c) {
        return c == null ? null : c.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
