package com.wis.main.util.core_util.factory.abstracter;

import com.wis.main.util.core_util.factory.FactoryType;
import jakarta.annotation.PostConstruct;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFactory<K, V> {

    private final Map<K, V> registry =
            new ConcurrentHashMap<>();

    private Class<?> keyClass;

    protected abstract List<V> getInstances();

    @PostConstruct
    void init() {

        resolveKeyClass();

        for (V instance : getInstances()) {

            Class<?> targetClass =
                    AopUtils.getTargetClass(instance);

            FactoryType annotation =
                    targetClass.getAnnotation(FactoryType.class);

            if (annotation == null) {
                continue;
            }

            @SuppressWarnings("unchecked")
            K key = (K) convertValue(annotation.value());

            registry.put(key, instance);

            System.out.println(
                    "Factory registered: "
                            + key
                            + " -> "
                            + targetClass.getSimpleName()
            );
        }
    }

    private void resolveKeyClass() {

        Type type = getClass().getGenericSuperclass();

        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalStateException(
                    "Missing generic type"
            );
        }

        keyClass = (Class<?>) pt.getActualTypeArguments()[0];
    }

    private Object convertValue(String value) {

        if (keyClass == String.class) {
            return value;
        }

        if (keyClass == Integer.class) {
            return Integer.parseInt(value);
        }

        if (keyClass == Long.class) {
            return Long.parseLong(value);
        }

        if (keyClass == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (keyClass.isEnum()) {

            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue =
                    Enum.valueOf(
                            (Class<? extends Enum>) keyClass,
                            value
                    );

            return enumValue;
        }

        throw new IllegalStateException(
                "Unsupported key type: "
                        + keyClass.getName()
        );
    }

    public V get(K key) {

        V value = registry.get(key);

        if (value == null) {
            throw new IllegalArgumentException(
                    "No implementation found: " + key
            );
        }

        return value;
    }
}