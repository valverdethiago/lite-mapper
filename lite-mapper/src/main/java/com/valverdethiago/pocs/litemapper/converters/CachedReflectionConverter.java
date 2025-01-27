package com.valverdethiago.pocs.litemapper.converters;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import com.valverdethiago.pocs.litemapper.registry.MapperRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedReflectionConverter<S, T> extends ReflectionConverter<S, T>{

    // Cache for fields and method handles
    private final Map<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<>();
    private final Map<Field, MethodHandle> getterCache = new ConcurrentHashMap<>();
    private final Map<Field, MethodHandle> setterCache = new ConcurrentHashMap<>();

    @Override
    public T convert(S source, Class<T> targetClass) {
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();

            Map<String, Field> sourceFields = getFields(source.getClass());
            Map<String, Field> targetFields = getFields(targetClass);

            for (Map.Entry<String, Field> entry : sourceFields.entrySet()) {
                Field sourceField = entry.getValue();
                sourceField.setAccessible(true);

                MapTo mapTo = sourceField.getAnnotation(MapTo.class);
                if (mapTo != null) {
                    Field targetField = targetFields.get(mapTo.targetField());
                    if (targetField != null) {
                        targetField.setAccessible(true);

                        // Use MethodHandles to retrieve and set values
                        MethodHandle getter = getGetterHandle(sourceField);
                        MethodHandle setter = getSetterHandle(targetField);

                        Object sourceValue = getter.invoke(source);

                        Object valueToSet = convertValue(sourceField, sourceValue, targetField);

                        setter.invoke(target, valueToSet);
                    }
                }
            }
            return target;
        } catch (Throwable e) {
            throw new RuntimeException("Conversion failed", e);
        }
    }

    // Caches the fields of a class
    private Map<String, Field> getFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, cls -> {
            Map<String, Field> fields = new ConcurrentHashMap<>();
            for (Field field : cls.getDeclaredFields()) {
                fields.put(field.getName(), field);
            }
            return fields;
        });
    }

    // Retrieves or caches the MethodHandle for a field getter
    private MethodHandle getGetterHandle(Field field) {
        return getterCache.computeIfAbsent(field, f -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(f.getDeclaringClass(), MethodHandles.lookup());
                return lookup.findGetter(f.getDeclaringClass(), f.getName(), f.getType());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to create getter MethodHandle for field: " + f.getName(), e);
            }
        });
    }

    // Retrieves or caches the MethodHandle for a field setter
    private MethodHandle getSetterHandle(Field field) {
        return setterCache.computeIfAbsent(field, f -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(f.getDeclaringClass(), MethodHandles.lookup());
                return lookup.findSetter(f.getDeclaringClass(), f.getName(), f.getType());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to create setter MethodHandle for field: " + f.getName(), e);
            }
        });
    }
}
