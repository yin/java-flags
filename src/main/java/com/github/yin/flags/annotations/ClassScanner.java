package com.github.yin.flags.annotations;

import com.github.yin.flags.*;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Scans classpath for {@Flag<?>} fields and classes putting them into index.
 */
public class ClassScanner {
    public void scanPackage(String packagePrefix, FlagIndex<FlagMetadata> flags, ClassMetadataIndex classMetaIndex) {
        new Scanner(new Reflections(packagePrefix))
                .scanPackage(flags, classMetaIndex);
    }

    private class Scanner {
        private Reflections reflections;

        public Scanner(Reflections reflections) {
            this.reflections = reflections;
        }

        public Scanner scanClass(Class<?> parent, FlagIndex<FlagMetadata> flags, ClassMetadataIndex classMetadataIndex) {
            collectClassMetadata(parent, classMetadataIndex);
            collectFields(parent, flags);
            return this;
        }

        public Scanner scanPackage(FlagIndex<FlagMetadata> flags, ClassMetadataIndex classMetaIndex) {
            Set<Class<?>> classDescs = reflections.getTypesAnnotatedWith(FlagDesc.class);
            for (Class<?> clazz : classDescs) {
                scanClass(clazz, flags, classMetaIndex);
            }
            return this;
        }

        private void collectClassMetadata(Class<?> parent, ClassMetadataIndex classMetadataIndex) {
            FlagDesc[] flagDescs = parent.getAnnotationsByType(FlagDesc.class);
            if (flagDescs.length > 1) {
                throw new Flags.ParseException("Class " + parent.getCanonicalName() + " is annotated multiple times with @FlagDesc");
            }
            for (FlagDesc desc : flagDescs) {
                ClassMetadata classInfo = ClassMetadata.create(parent.getCanonicalName(), desc.value());
                classMetadataIndex.classes().put(parent.getCanonicalName(), classInfo);
            }
        }

        public void collectFields(Class<?> parent, FlagIndex<FlagMetadata> metadata) {
            Field[] fields = parent.getDeclaredFields();
            for (Field field : fields) {
                Class<?> clazz = field.getType();
                if (!Flag.class.isAssignableFrom(clazz)) {
                    continue;
                }
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    throw new Flags.ParseException("Flag " + clazz.getCanonicalName() + "." + field.getName()
                            + " is not a static field");
                }

                Flag<?> flag = getFlag(parent, field);
                FlagMetadata meta = createMeta(parent, field, flag);
                metadata.add(meta.flagID(), meta);
            }
        }

        private FlagMetadata createMeta(Class<?> parent, Field field, Flag<?> flag) {
            FlagDesc[] flagDescs = field.getAnnotationsByType(FlagDesc.class);
            if (flagDescs.length == 0) {
                return FlagMetadata.create(parent.getCanonicalName(), field.getName(), "", flag);
            } else if (flagDescs.length == 1) {
                FlagDesc desc = flagDescs[0];
                String name = desc.name().isEmpty() ? field.getName() : desc.name();
                return FlagMetadata.create(parent.getCanonicalName(), name, desc.value(), flag);
            } else {
                throw new Flags.ParseException("Flag " + parent.getCanonicalName() + "." + field.getName()
                        + " is annotated multiple times with @FlagDesc");
            }
        }

        private Flag<?> getFlag(Class<?> parent, Field field) {
            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object value = field.get(parent);
                field.setAccessible(accessible);
                if (value == null || !Flag.class.isAssignableFrom(value.getClass())) {
                    throw new Flags.ParseException("Flag " + parent.getCanonicalName() + "." + field.getName()
                            + " is not of type Flag<?>");
                }
                return (Flag<?>) value;
            } catch (IllegalAccessException e) {
                // happens only if we fail to make the field accessible
                throw new Flags.ParseException("Flag " + parent.getCanonicalName() + "." + field.getName()
                        + " is not accessible", e);
            }
        }
    }

}
