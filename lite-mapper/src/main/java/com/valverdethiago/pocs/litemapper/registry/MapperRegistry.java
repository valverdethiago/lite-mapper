package com.valverdethiago.pocs.litemapper.registry;

import com.valverdethiago.pocs.litemapper.converters.CustomMapper;

import java.util.HashMap;
import java.util.Map;

public class MapperRegistry {
    private static final Map<Class<?>, CustomMapper> mappers = new HashMap<>();

    public static void register(Class<?> sourceType, CustomMapper mapper) {
        mappers.put(sourceType, mapper);
    }

    public static CustomMapper getMapper(Class<?> sourceType) {
        return mappers.get(sourceType);
    }
}
