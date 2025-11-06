package com.wis.main.util.core_util.drools.annotation;

@FunctionalInterface
public interface DroolAction<T, R> extends java.io.Serializable {
    void apply(T request, R result);
}

