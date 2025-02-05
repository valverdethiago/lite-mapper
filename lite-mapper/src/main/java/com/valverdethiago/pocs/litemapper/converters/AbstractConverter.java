package com.valverdethiago.pocs.litemapper.converters;

import com.valverdethiago.pocs.litemapper.registry.MapperRegistry;

import java.lang.reflect.Field;

public abstract class AbstractConverter<S, T> implements RuntimeConverter<S, T> {

    protected Object convertValue(Field sourceField, Object sourceValue, Field targetField) {

        // Check if a custom mapper exists for the source type
        var customMapper = MapperRegistry.getMapper(sourceField.getType());
        Object valueToSet = sourceValue;

        if (customMapper != null) {
            valueToSet = customMapper.map(sourceValue);
        } else if (!targetField.getType().isAssignableFrom(sourceField.getType())) {
            // Handle basic type conversion for common cases
            valueToSet = convertBasicTypes(sourceValue, targetField.getType());
        }
        return valueToSet;
    }

    protected Object convertBasicTypes(Object sourceValue, Class<?> targetType) {
        if (sourceValue == null) {
            return null;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(sourceValue.toString());
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(sourceValue.toString());
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(sourceValue.toString());
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(sourceValue.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(sourceValue.toString());
        }
        if (targetType == String.class) {
            return sourceValue.toString();
        }
        throw new IllegalArgumentException(
                "Cannot convert " + sourceValue.getClass() + " to " + targetType);
    }
}

