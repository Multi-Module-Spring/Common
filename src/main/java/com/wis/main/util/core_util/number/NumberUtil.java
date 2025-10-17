package com.wis.main.util.core_util.number;


import java.util.Collection;
import java.util.Random;

public interface NumberUtil<N extends Number> {
    Random random = new Random();

    default boolean isNull(N n) {
        return n == null;
    }

    <V> N parse(V v);

    default <V> N tryParse(V v) {
        try {
            return parse(v);
        } catch (Exception e) {
            return null;
        }
    }

    <V> N nvl(V v);

    default N nvl(N n, N nvl) {
        return n == null ? nvl : n;
    }

    boolean equals(N a, N b);

    default boolean notEquals(N a, N b) {
        return !equals(a, b);
    }

    boolean lessThan(N a, N b);

    default boolean lessThanOrEquals(N a, N b) {
        return lessThan(a, b) || equals(a, b);
    }

    boolean greaterThan(N a, N b);

    default boolean greaterThanOrEquals(N a, N b) {
        return greaterThan(a, b) || equals(a, b);
    }

    N abs(N n);

    N round(N n, int scale);

    N random(N min, N max);

    N sum(Collection<N> c);

    default N min(N a, N b) {
        if (lessThan(a, b)) {
            return a;
        }
        return b;
    }

    N min(Collection<N> c);

    default N max(N a, N b) {
        if (greaterThan(a, b)) {
            return a;
        }
        return b;
    }

    N max(Collection<N> c);
}
