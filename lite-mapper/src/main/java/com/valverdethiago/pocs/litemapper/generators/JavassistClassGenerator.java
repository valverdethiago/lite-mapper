package com.valverdethiago.pocs.litemapper.generators;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import com.valverdethiago.pocs.litemapper.converters.RuntimeConverter;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class JavassistClassGenerator implements ClassGenerator {

    private static final Map<String, Class<?>> GENERATED_CLASSES = new ConcurrentHashMap<>();


    @Override
    public <S, D> RuntimeConverter<S, D> generateMapperClass(Class<S> sourceClass, Class<D> destinationClass) {
        try {
            ClassPool pool = ClassPool.getDefault();
            String classname = getClassname(sourceClass, destinationClass);
            String fullClassifiedName = sourceClass.getPackageName() + "." + classname;
            // Check if class already exists in cache
            if (GENERATED_CLASSES.containsKey(fullClassifiedName)) {
                return (RuntimeConverter<S, D>) GENERATED_CLASSES.get(fullClassifiedName).getDeclaredConstructor().newInstance();
            }

            CtClass generatedClass = pool.makeClass(fullClassifiedName);

            CtClass abstractConverterClass = pool.get("com.valverdethiago.pocs.litemapper.converters.AbstractConverter");

            generatedClass.setSuperclass(abstractConverterClass);

            // Create convert method using generics S and D correctly
            CtMethod convertMethod = new CtMethod(
                    pool.get("java.lang.Object"), // Return type must be Object because of type erasure at runtime
                    "convert",
                    new CtClass[]{ pool.get("java.lang.Object"), pool.get("java.lang.Class") },
                    generatedClass
            );

            // Ensure generic signature uses S and D
            convertMethod.setGenericSignature("(TS;Ljava/lang/Class<TD;>;)TD;");
            convertMethod.setModifiers(Modifier.PUBLIC);


            // Create a mapping of source field names to destination field names based on @MapTo annotation
            Map<String, String> fieldMappings = new HashMap<>();
            Map<String, Class<?>> destinationFieldTypes = new HashMap<>();

            for (Field destinationField : destinationClass.getDeclaredFields()) {
                destinationFieldTypes.put(destinationField.getName(), destinationField.getType());
            }

            for (Field sourceField : sourceClass.getDeclaredFields()) {
                MapTo annotation = sourceField.getAnnotation(MapTo.class);
                if (annotation != null) {
                    fieldMappings.put(sourceField.getName(), annotation.targetField());
                } else {
                    fieldMappings.put(sourceField.getName(), sourceField.getName());
                }
            }

            StringBuilder methodBody = generateMethodBody(sourceClass, destinationClass, fieldMappings, destinationFieldTypes);

            methodBody.append(" return target; }");
            convertMethod.setBody(methodBody.toString());

            generatedClass.addMethod(convertMethod);
            generatedClass.defrost();

            // Convert CtClass to bytecode
            byte[] bytecode = generatedClass.toBytecode();
            Class<?> generatedClazz = defineClass(fullClassifiedName, bytecode);// Convert CtClass to bytecode

            // Store in cache
            GENERATED_CLASSES.put(fullClassifiedName, generatedClazz);

            return (RuntimeConverter<S, D>) generatedClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate class using Javassist", e);
        }
    }

    private static <S, D> StringBuilder generateMethodBody(Class<S> sourceClass, Class<D> destinationClass, Map<String, String> fieldMappings, Map<String, Class<?>> destinationFieldTypes) {
        // Generate method body dynamically
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{ if ($1 == null) return null; ");
        methodBody.append(sourceClass.getName()).append(" source = (").append(sourceClass.getName()).append(") $1; ");
        methodBody.append(destinationClass.getName()).append(" target = new ").append(destinationClass.getName()).append("(); ");

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String sourceFieldName = entry.getKey();
            String destFieldName = entry.getValue();

            String getterName = "get" + Character.toUpperCase(sourceFieldName.charAt(0)) + sourceFieldName.substring(1);
            String setterName = "set" + Character.toUpperCase(destFieldName.charAt(0)) + destFieldName.substring(1);

            Class<?> destinationFieldType = destinationFieldTypes.get(destFieldName);

            if (destinationFieldType != null) {
                methodBody.append("target.").append(setterName).append("(")
                        .append("( ").append(destinationFieldType.getName()).append(") ")
                        .append("convertValue(")
                        .append(sourceClass.getName()).append(".class.getDeclaredField(\"").append(sourceFieldName).append("\"), ")
                        .append("source.").append(getterName).append("(), ")
                        .append(destinationClass.getName()).append(".class.getDeclaredField(\"").append(destFieldName).append("\"))");
                methodBody.append("); ");
            }
        }
        return methodBody;
    }

    private static <S, D> String getClassname(Class<S> sourceClass, Class<D> destinationClass) {
        return sourceClass.getSimpleName() + "To" + destinationClass.getSimpleName() + "Mapper";
    }

    private Class<?> defineClass(String fullClassifiedName, byte[] bytecode) {
        return new ClassLoader(Thread.currentThread().getContextClassLoader()) {
            public Class<?> defineClass() {
                return defineClass(fullClassifiedName, bytecode, 0, bytecode.length);
            }
        }.defineClass();
    }
}
