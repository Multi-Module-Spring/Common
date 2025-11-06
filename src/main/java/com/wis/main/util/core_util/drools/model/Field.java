package com.wis.main.util.core_util.drools.model;

import java.io.Serializable;

@FunctionalInterface
public interface Field<T, R> extends Serializable {
    R apply(T t);
}

