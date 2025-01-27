package com.valverdethiago.pocs.litemapper.converters;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import com.valverdethiago.pocs.litemapper.registry.MapperRegistry;

import java.lang.reflect.Field;

public class ReflectionConverter<S, T> implements RuntimeConverter<S, T> {

    @Override
    public T convert(S source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();

            for (Field sourceField : source.getClass().getDeclaredFields()) {
                sourceField.setAccessible(true);

                MapTo mapTo = sourceField.getAnnotation(MapTo.class);
                if (mapTo != null) {
                    Field targetField = targetClass.getDeclaredField(mapTo.targetField());
                    targetField.setAccessible(true);

                    Object sourceValue = sourceField.get(source);

                    Object valueToSet = convertValue(sourceField, sourceValue, targetField);

                    targetField.set(target, valueToSet);
                }
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Conversion failed", e);
        }
    }

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

