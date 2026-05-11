package com.wis.main.util.core_util.factory.manager;

import com.wis.main.util.core_util.factory.FactoryType;
import com.wis.main.util.core_util.factory.builder.FactoryBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class FactoryManager {

    private final ApplicationContext context;

    private final Map<Class<?>, Map<Object, Object>>
            registry = new ConcurrentHashMap<>();

    public <V> FactoryBuilder<V> build(
            Class<V> clazz
    ) {

        return new FactoryBuilder<>(
                clazz,
                registry,
                this::loadFactory
        );
    }

    private Map<Object, Object> loadFactory(
            Class<?> clazz
    ) {

        Map<Object, Object> map =
                new ConcurrentHashMap<>();

        Map<String, ?> beans =
                context.getBeansOfType(clazz);

        for (Object bean : beans.values()) {

            Class<?> targetClass =
                    AopUtils.getTargetClass(bean);

            FactoryType annotation =
                    targetClass.getAnnotation(
                            FactoryType.class
                    );

            if (annotation == null) {
                continue;
            }

            map.put(
                    convertKey(annotation.value()),
                    bean
            );
        }

        return map;
    }

    private Object convertKey(String value) {

        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
        }

        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
        }

        if ("true".equalsIgnoreCase(value)
                || "false".equalsIgnoreCase(value)) {

            return Boolean.parseBoolean(value);
        }

        return value;
    }
}