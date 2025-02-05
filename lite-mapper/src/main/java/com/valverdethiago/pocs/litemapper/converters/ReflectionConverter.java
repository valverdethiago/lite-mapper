package com.valverdethiago.pocs.litemapper.converters;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import com.valverdethiago.pocs.litemapper.registry.MapperRegistry;

import java.lang.reflect.Field;

public class ReflectionConverter<S, T> extends AbstractConverter<S, T> {

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
}

