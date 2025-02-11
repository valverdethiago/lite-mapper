package com.valverdethiago.pocs.litemapper.generators;

import com.valverdethiago.pocs.litemapper.annotations.MapTo;
import com.valverdethiago.pocs.litemapper.converters.RuntimeConverter;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unchecked")
public class ASMClassGenerator implements ClassGenerator {

    private static final Map<String, Class<?>> GENERATED_CLASSES = new ConcurrentHashMap<>();

    @Override
    public <S, D> RuntimeConverter<S, D> generateMapperClass(Class<S> sourceClass, Class<D> destinationClass) {
        try {
            String classname = getClassname(sourceClass, destinationClass);
            String fullClassifiedName = sourceClass.getPackageName() + "." + classname;

            // Check if class already exists in cache
            if (GENERATED_CLASSES.containsKey(fullClassifiedName)) {
                return (RuntimeConverter<S, D>) GENERATED_CLASSES.get(fullClassifiedName).getDeclaredConstructor().newInstance();
            }

            // Generate bytecode for the class
            byte[] bytecode = generateClassBytes(sourceClass, destinationClass, classname, fullClassifiedName);
            Class<?> generatedClazz = defineClass(fullClassifiedName, bytecode);

            // Store in cache
            GENERATED_CLASSES.put(fullClassifiedName, generatedClazz);

            return (RuntimeConverter<S, D>) generatedClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate class using ASM", e);
        }
    }

    private static <S, D> byte[] generateClassBytes(Class<S> sourceClass, Class<D> destinationClass, String classname, String fullClassifiedName) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String classInternalName = fullClassifiedName.replace('.', '/');

        String superClass = "com/valverdethiago/pocs/litemapper/converters/AbstractConverter";
        String interfaceName = "com/valverdethiago/pocs/litemapper/converters/RuntimeConverter";

        // Define class: public class SourceToDestinationMapper extends AbstractConverter implements RuntimeConverter
        classWriter.visit(V17, ACC_PUBLIC + ACC_SUPER, classInternalName, null, superClass.replace('.', '/'), new String[]{interfaceName.replace('.', '/')});

        // Add default constructor
        MethodVisitor constructor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, superClass.replace('.', '/'), "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        // Add convert method
        generateMethodBody(classWriter, sourceClass, destinationClass, classInternalName);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }private static <S, D> void generateMethodBody(ClassWriter classWriter, Class<S> sourceClass, Class<D> destinationClass, String classInternalName) {
        String sourceInternalName = Type.getInternalName(sourceClass);
        String destinationInternalName = Type.getInternalName(destinationClass);

        MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                "convert",
                "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
                "(TS;Ljava/lang/Class<TD;>;)TD;",
                null
        );
        methodVisitor.visitCode();

        // if (source == null) return null;
        Label nonNullLabel = new Label();
        methodVisitor.visitVarInsn(ALOAD, 1); // Load parameter 'source'
        methodVisitor.visitJumpInsn(IFNONNULL, nonNullLabel);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitLabel(nonNullLabel);

        // Cast Object to Source
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST, sourceInternalName);
        methodVisitor.visitVarInsn(ASTORE, 3); // Store in local variable 3 (source)

        // Destination target = new Destination();
        methodVisitor.visitTypeInsn(NEW, destinationInternalName);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, destinationInternalName, "<init>", "()V", false);
        methodVisitor.visitVarInsn(ASTORE, 2); // Store in local variable 2 (target)

        for (Field sourceField : sourceClass.getDeclaredFields()) {
            String sourceFieldName = sourceField.getName();
            String getterName = "get" + Character.toUpperCase(sourceFieldName.charAt(0)) + sourceFieldName.substring(1);

            MapTo annotation = sourceField.getAnnotation(MapTo.class);
            String destinationFieldName = (annotation != null) ? annotation.targetField() : sourceFieldName;
            String setterName = "set" + Character.toUpperCase(destinationFieldName.charAt(0)) + destinationFieldName.substring(1);

            Class<?> destinationFieldType;
            try {
                destinationFieldType = destinationClass.getDeclaredField(destinationFieldName).getType();
            } catch (NoSuchFieldException e) {
                continue; // Skip if the field doesn't exist in the destination class
            }

            // target.setXXX(source.getXXX());
            methodVisitor.visitVarInsn(ALOAD, 2); // Load 'target'
            methodVisitor.visitVarInsn(ALOAD, 3); // Load 'source' (now properly casted)

            // Call getter on source
            if (destinationFieldType == Integer.class) {
                // Special case: source returns String, but destination needs Integer
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, sourceInternalName, getterName, "()Ljava/lang/String;", false);
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false); // Boxing
            } else if (destinationFieldType == int.class) {
                // Source is String, target is int (primitive)
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, sourceInternalName, getterName, "()Ljava/lang/String;", false);
                methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
            } else {
                methodVisitor.visitMethodInsn(INVOKEVIRTUAL, sourceInternalName, getterName, "()" + Type.getDescriptor(sourceField.getType()), false);
            }

            // Call setter on destination
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, destinationInternalName, setterName, "(" + Type.getDescriptor(destinationFieldType) + ")V", false);
        }

        // return target;
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(3, 3);
        methodVisitor.visitEnd();
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
