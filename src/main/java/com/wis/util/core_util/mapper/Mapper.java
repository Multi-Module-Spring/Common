package com.wis.util.core_util.mapper;

import java.lang.reflect.Type;
import java.util.List;

public interface Mapper {
    <DESTINATION> DESTINATION map(Object source, Type type);
    <DESTINATION> DESTINATION map(Object source, Type type, MapperStrategy mapperStrategy);
    <DESTINATION> DESTINATION selfMap(DESTINATION source);
    <DESTINATION> DESTINATION selfMap(DESTINATION source, MapperStrategy mapperStrategy);
    <SOURCE, DESTINATION> List<DESTINATION> mapList(List<SOURCE> sourceList, Class<DESTINATION> destinationType);
    <SOURCE, DESTINATION> List<DESTINATION> mapList(List<SOURCE> sourceList, Class<DESTINATION> destinationType, MapperStrategy mapperStrategy);
    <SOURCE> List<SOURCE> selfMapList(List<SOURCE> sourceList);
    <SOURCE> List<SOURCE> selfMapList(List<SOURCE> sourceList, MapperStrategy mapperStrategy);
}
