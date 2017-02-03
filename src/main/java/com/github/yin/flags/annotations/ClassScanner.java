package com.github.yin.flags.annotations;

import com.github.yin.flags.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Scans classpath for {@Flag<?>} fields and classes putting them into index.
 */
public class ClassScanner {
    private static final Logger log = LoggerFactory.getLogger(ClassScanner.class);

    public void scanPackage(String packagePrefix, FlagIndex<FlagMetadata> index, ClassMetadataIndex classMetaIndex) {
        new Scanner(new Reflections(packagePrefix))
                .scanPackage(index, classMetaIndex);
    }

    private class Scanner {
        private Reflections reflections;

        public Scanner(Reflections reflections) {
            this.reflections = reflections;
        }

        public Scanner scanClass(final Class<?> clazz, final FlagIndex<FlagMetadata> index, final ClassMetadataIndex classMetadataIndex) {
            collectClassMetadata(clazz, classMetadataIndex);
            collectFields(clazz, index);
            return this;
        }

        public Scanner scanPackage(FlagIndex<FlagMetadata> index, ClassMetadataIndex classMetaIndex) {
            Set<Class<?>> classDescs = reflections.getTypesAnnotatedWith(FlagDesc.class);
            for (Class<?> clazz : classDescs) {
                scanClass(clazz, index, classMetaIndex);
            }
            return this;
        }

        private void collectClassMetadata(Class<?> clazz, ClassMetadataIndex classMetadataIndex) {
            FlagDesc[] flagDescs = clazz.getAnnotationsByType(FlagDesc.class);
            if (flagDescs.length > 1) {
                log.error("Multiple @FlagDesc occurrences on class: {}", clazz);
                return;
            }
            for (FlagDesc desc : flagDescs) {
                ClassMetadata classInfo = ClassMetadata.create(clazz.getCanonicalName(), desc.value());
                classMetadataIndex.classes().put(clazz.getCanonicalName(), classInfo);
            }
        }

        public void collectFields(final Class<?> clazz, final FlagIndex<FlagMetadata> index) {
            Field[] fields = clazz.getDeclaredFields();
            fields:
            for (Field field : fields) {
                FlagDesc[] flagDescs = field.getAnnotationsByType(FlagDesc.class);
                if (flagDescs.length > 1) {
                    log.error("Multiple @FlagDesc occurrences on field: {}", field.toString());
                    continue;
                }
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    log.error("@FlagDesc on non-static field: {}", field.toString());
                    continue;
                }
                if ((field.getModifiers() & Modifier.FINAL) == 0) {
                    log.error("@FlagDesc on non-final field: {}", field);
                    continue;
                }
                if (field.isAccessible()) {
                    log.error("@FlagDesc on non-accessible filed: {}", field);
                    continue;
                }
                for (FlagDesc desc : flagDescs) {
                    collectField(clazz, index, field, desc);
                }
            }
        }

        private void collectField(Class<?> clazz, FlagIndex<FlagMetadata> index, Field field, FlagDesc desc) {
            String name = desc.name() != null && !desc.name().isEmpty() ? desc.name() : field.getName();
            try {
                field.setAccessible(true);
                Object value = field.get(clazz);
                field.setAccessible(false);
                if (value == null || !Flag.class.isAssignableFrom(value.getClass())) {
                    log.error("@FlagDesc on non-Flag type field: {}", field);
                    return;
                }
                FlagMetadata meta =
                        FlagMetadata.create(clazz.getCanonicalName(), name, desc.value(), (Flag<?>) value);
                index.add(meta.flagID(), meta);
            } catch (IllegalAccessException e) {
                // happens only if a bug crawls in - useful
                log.error("Flag field inaccessible: {}", field);
            } catch (Throwable t) {
                log.error("eroor", t);
            }
        }
    }
}
