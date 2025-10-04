package com.wis.util.core_util.mapper.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wis.util.core_util.mapper.Mapper;
import com.wis.util.core_util.mapper.MapperStrategy;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class MapperImpl implements Mapper {
    private final ModelMapper modelMapper;

    public <DESTINATION> DESTINATION map(Object source, Type type) {
        if (source == null) return null;
        return map(source, type, MapperStrategy.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public <DESTINATION> DESTINATION map(Object source, Type type, MapperStrategy strategy) {
        if (source == null) {
            return null;
        }

        if (strategy == MapperStrategy.JSON_PROPERTY) {
            return (DESTINATION) mapWithJsonPropertyStrategy(source, (Class<?>) type);
        }

        return modelMapper.map(source, type);
    }

    private Object mapWithJsonPropertyStrategy(Object source, Class<?> destinationType) {
        Object result = modelMapper.map(source, destinationType);

        applyJsonPropertyMapping(source, result);

        return result;
    }

    private void applyJsonPropertyMapping(Object source, Object destination) {
        Class<?> sourceClass = source.getClass();
        Class<?> destClass = destination.getClass();

        Field[] destFields = destClass.getDeclaredFields();

        for (Field destField : destFields) {
            JsonProperty jsonProperty = destField.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                String sourceFieldName = jsonProperty.value();

                try {
                    Field sourceField = sourceClass.getDeclaredField(sourceFieldName);

                    sourceField.setAccessible(true);
                    Object value = sourceField.get(source);

                    destField.setAccessible(true);
                    destField.set(destination, value);

                } catch (Exception e) {
                    try {
                        copyUsingMethods(source, destination, sourceFieldName, destField.getName());
                    } catch (Exception ex) {
                        System.err.println("Cannot map field '" + sourceFieldName +
                                "' to '" + destField.getName() + "': " + ex.getMessage());
                    }
                }
            }
        }
    }

    private void copyUsingMethods(Object source, Object destination, String sourceFieldName, String destFieldName)
            throws Exception {
        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(destFieldName);

        Method getter = source.getClass().getMethod(getterName);
        Object value = getter.invoke(source);

        Method setter = destination.getClass().getMethod(setterName, getter.getReturnType());
        setter.invoke(destination, value);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public <SOURCE> SOURCE selfMap(SOURCE source) {
        if (source == null) return null;
        return map(source, source.getClass());
    }

    public <SOURCE> SOURCE selfMap(SOURCE source, MapperStrategy strategy) {
        if (source == null) {
            return null;
        }
        return map(source, source.getClass(), strategy);
    }

    public <SOURCE, DESTINATION> List<DESTINATION> mapList(
            List<SOURCE> sourceList,
            Class<DESTINATION> destinationType
    ) {
        return mapList(sourceList, destinationType, MapperStrategy.DEFAULT);
    }

    public <SOURCE, DESTINATION> List<DESTINATION> mapList(
            List<SOURCE> sourceList,
            Class<DESTINATION> destinationType,
            MapperStrategy strategy
    ) {
        if (sourceList == null) {
            return Collections.emptyList();
        }

        return sourceList.stream()
                .map(source -> this.<DESTINATION>map(source, destinationType, strategy))
                .toList();
    }

    public <SOURCE> List<SOURCE> selfMapList(List<SOURCE> sourceList) {
        if (sourceList == null) return Collections.emptyList();
        return sourceList.stream()
                .map(this::selfMap)
                .toList();
    }

    public <SOURCE> List<SOURCE> selfMapList(List<SOURCE> sourceList, MapperStrategy strategy) {
        if (sourceList == null) return Collections.emptyList();
        return sourceList.stream()
                .map(source -> selfMap(source, strategy))
                .toList();
    }
}