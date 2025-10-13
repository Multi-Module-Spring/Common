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
public class DoubleUtil implements NumberUtil<Double> {
    public static final Double ZERO = 0D;

    private final StringUtil stringUtil;

    @Override
    public <V> Double parse(V v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).doubleValue();
        }
        if (v instanceof BigInteger) {
            return ((BigInteger) v).doubleValue();
        }
        if (v instanceof Double) {
            return (Double) v;
        }
        if (v instanceof Float) {
            return ((Float) v).doubleValue();
        }
        if (v instanceof Long) {
            return ((Long) v).doubleValue();
        }
        if (v instanceof Integer) {
            return ((Integer) v).doubleValue();
        }
        String s = stringUtil.nvl(v);
        if (stringUtil.isEmpty(s)) {
            return null;
        }
        return Double.parseDouble(s);
    }

    @Override
    public <V> Double nvl(V v) {
        Double n = parse(v);
        return nvl(n, ZERO);
    }

    @Override
    public boolean equals(Double a, Double b) {
        a = nvl(a);
        b = nvl(b);
        return a.equals(b) || a.compareTo(b) == 0;
    }

    @Override
    public boolean lessThan(Double a, Double b) {
        return nvl(a).compareTo(nvl(b)) < 0;
    }

    @Override
    public boolean greaterThan(Double a, Double b) {
        return nvl(a).compareTo(nvl(b)) > 0;
    }

    @Override
    public Double abs(Double n) {
        return n == null ? null : Math.abs(n);
    }

    @Override
    public Double round(Double n, int scale) {
        if (n == null) {
            return null;
        }
        double ten = 10D;
        double pow = Math.pow(ten, scale);
        return parse(Math.round(n * pow) / pow);
    }

    @Override
    public Double random(Double min, Double max) {
        min = nvl(min);
        max = nvl(max);
        Double a = random.nextDouble();
        Double b = max - min;
        return min + (a * b);
    }

    @Override
    public Double sum(Collection<Double> c) {
        return c == null ? ZERO : c.stream().reduce(ZERO, Double::sum);
    }

    @Override
    public Double min(Collection<Double> c) {
        return c == null ? null : c.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Double max(Collection<Double> c) {
        return c == null ? null : c.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
