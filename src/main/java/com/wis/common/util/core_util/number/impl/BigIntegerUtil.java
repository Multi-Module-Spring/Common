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
public class BigIntegerUtil implements NumberUtil<BigInteger> {
    private final StringUtil stringUtil;

    @Override
    public <V> BigInteger parse(V v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).toBigIntegerExact();
        }
        if (v instanceof BigInteger) {
            return (BigInteger) v;
        }
        if (v instanceof Double) {
            return BigDecimal.valueOf((Double) v).toBigIntegerExact();
        }
        if (v instanceof Float) {
            return BigDecimal.valueOf((Float) v).toBigIntegerExact();
        }
        if (v instanceof Long) {
            return new BigDecimal((Long) v).toBigIntegerExact();
        }
        if (v instanceof Integer) {
            return new BigDecimal((Integer) v).toBigIntegerExact();
        }
        String s = stringUtil.nvl(v);
        if (stringUtil.isEmpty(s)) {
            return null;
        }
        return new BigDecimal(s).toBigIntegerExact();
    }

    @Override
    public <V> BigInteger nvl(V v) {
        BigInteger n = parse(v);
        return nvl(n, BigInteger.ZERO);
    }

    @Override
    public boolean equals(BigInteger a, BigInteger b) {
        a = nvl(a);
        b = nvl(b);
        return a.equals(b) || a.compareTo(b) == 0;
    }

    @Override
    public boolean lessThan(BigInteger a, BigInteger b) {
        return nvl(a).compareTo(nvl(b)) < 0;
    }

    @Override
    public boolean greaterThan(BigInteger a, BigInteger b) {
        return nvl(a).compareTo(nvl(b)) > 0;
    }

    @Override
    public BigInteger abs(BigInteger n) {
        return n == null ? null : n.abs();
    }

    @Override
    public BigInteger round(BigInteger n, int scale) {
        if (n == null) {
            return null;
        }
        BigInteger ten = parse(10);
        BigInteger pow = ten.pow(scale);
        return n.multiply(pow)
                .divide(pow);
    }

    @Override
    public BigInteger random(BigInteger min, BigInteger max) {
        min = nvl(min);
        max = nvl(max);
        BigInteger a = parse(random.nextDouble());
        BigInteger b = max.subtract(min);
        return min.add(a.multiply(b));
    }

    @Override
    public BigInteger sum(Collection<BigInteger> c) {
        return c == null ? BigInteger.ZERO : c.stream().reduce(BigInteger.ZERO, BigInteger::add);
    }

    @Override
    public BigInteger min(Collection<BigInteger> c) {
        return c == null ? null : c.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public BigInteger max(Collection<BigInteger> c) {
        return c == null ? null : c.stream().max(Comparator.naturalOrder()).orElse(null);
    }
}
