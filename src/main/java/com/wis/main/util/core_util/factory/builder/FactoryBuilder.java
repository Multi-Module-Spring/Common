package com.wis.main.util.core_util.factory.builder;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class FactoryBuilder<V> {

    private final Class<V> clazz;

    private final Map<Class<?>, Map<Object, Object>>
            registry;

    private final Function<Class<?>, Map<Object, Object>>
            loader;

    @SuppressWarnings("unchecked")
    public <K> V with(K key) {

        Map<Object, Object> map =
                registry.computeIfAbsent(
                        clazz,
                        loader
                );

        Object value = map.get(key);

        if (value == null) {
            throw new IllegalArgumentException(
                    "No implementation found: "
                            + key
            );
        }

        return (V) value;
    }
}