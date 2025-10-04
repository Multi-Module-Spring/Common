package com.wis.util.core_util.number.impl;

import com.wis.util.core_util.number.NumberUtil;
import com.wis.util.core_util.string.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;

@Slf4j
@RequiredArgsConstructor
@Component
public class BigDecimalUtil implements NumberUtil<BigDecimal> {
    private final StringUtil stringUtil;

    @Override
    public <V> BigDecimal parse(V v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof BigInteger) {
            return new BigDecimal((BigInteger) v);
        }
        if (v instanceof Double) {
            return BigDecimal.valueOf((Double) v);
        }
        if (v instanceof Float) {
            return BigDecimal.valueOf((Float) v);
        }
        if (v instanceof Long) {
            return new BigDecimal((Long) v);
        }
        if (v instanceof Integer) {
            return new BigDecimal((Integer) v);
        }
        String s = stringUtil.nvl(v);
        if (stringUtil.isEmpty(s)) {
            return null;
        }
        return new BigDecimal(s);
    }

    @Override
    public <V> BigDecimal nvl(V v) {
        BigDecimal n = parse(v);
        return nvl(n, BigDecimal.ZERO);
    }

    @Override
    public boolean equals(BigDecimal a, BigDecimal b) {
        a = nvl(a);
        b = nvl(b);
        return a.equals(b) || a.compareTo(b) == 0;
    }

    @Override
    public boolean lessThan(BigDecimal a, BigDecimal b) {
        return nvl(a).compareTo(nvl(b)) < 0;
    }

    @Override
    public boolean greaterThan(BigDecimal a, BigDecimal b) {
        return nvl(a).compareTo(nvl(b)) > 0;
    }

    @Override
    public BigDecimal abs(BigDecimal n) {
        return n == null ? null : n.abs();
    }

    @Override
    public BigDecimal round(BigDecimal n, int scale) {
        if (n == null) {
            return null;
        }
        BigDecimal ten = parse(10);
        BigDecimal pow = ten.pow(scale);
        return n.multiply(pow).setScale(scale, RoundingMode.HALF_UP)
                .divide(pow, scale, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal random(BigDecimal min, BigDecimal max) {
        min = nvl(min);
        max = nvl(max);
        BigDecimal a = parse(random.nextDouble());
        BigDecimal b = max.subtract(min);
        return min.add(a.multiply(b));
    }

    @Override
    public BigDecimal sum(Collection<BigDecimal> c) {
        return c == null ? BigDecimal.ZERO : c.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal min(Collection<BigDecimal> c) {
        return c == null ? null : c.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public BigDecimal max(Collection<BigDecimal> c) {
        return c == null ? null : c.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
