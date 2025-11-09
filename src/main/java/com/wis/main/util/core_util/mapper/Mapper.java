package com.wis.main.util.core_util.mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Mapper {

    <F, T> T mapViaString(F from, Class<T> pojoType);

    <F, T> T mapViaString(F from, Class<T> pojoType, MappingStrategy strategy);

    <T> T deepClone(T object, Class<T> clazz);

    <T> T deepClone(T object, Class<T> clazz, MappingStrategy strategy);

    <T> T mapTo(Map<String, Object> mapData, Class<T> pojoType);

    <T> T mapTo(Map<String, Object> mapData, Class<T> pojoType, MappingStrategy strategy);

    <F, T> T mapTo(F from, Class<T> type);

    <F, T> T mapTo(F from, Class<T> type, MappingStrategy strategy);

    <F, T> Function<F, T> mapTo(Class<T> type);

    <F, T> Function<F, T> mapTo(Class<T> type, MappingStrategy strategy);

    <F, T> List<T> mapToList(F from, Class<T> type);

    <F, T> List<T> mapToList(F from, Class<T> type, MappingStrategy strategy);

    <F, T> T updateValue(T to, F from);

    <F, T> T updateValue(T to, F from, MappingStrategy strategy);

    @SuppressWarnings("unchecked")
    <T> void switchValue(T left, T right);

    <T> T read(String json, Class<T> clazz);

    String write(Object object);
}
