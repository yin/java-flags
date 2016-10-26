package com.github.yin.flags.annotations;

import com.github.yin.flags.ClassMetadata;
import com.github.yin.flags.ClassMetadataIndex;
import com.github.yin.flags.FlagIndex;
import com.github.yin.flags.FlagMetadata;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author yin
 */
public class ClassScanner {
    private static final Logger log  = LoggerFactory.getLogger(ClassScanner.class);

    // TODO yin: it would be also useful to return the metadata in a list
    public void scanClass(final String className, final FlagIndex<FlagMetadata> index, ClassMetadataIndex classMetaIndex) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        scanClass(clazz, index, classMetaIndex);
    }

    public void scanClass(final Class<?> clazz, final FlagIndex<FlagMetadata> index, final ClassMetadataIndex classMetadataIndex) {
        collectClassMetadata(clazz, classMetadataIndex);
        collectFlagInfo(clazz, index);
    }

    private void collectClassMetadata(Class<?> clazz, ClassMetadataIndex classMetadataIndex) {
        FlagDesc[] flagDescs = clazz.getAnnotationsByType(FlagDesc.class);
        if (flagDescs.length > 1) {
            log.error("Multiple @FlagDesc occurrences on class: {}", clazz);
            return;
        }
        for (FlagDesc desc : flagDescs) {
            ClassMetadata classInfo =  ClassMetadata.create(clazz.getName(), desc.value());
            classMetadataIndex.classes().put(clazz.getCanonicalName(), classInfo);
        }
    }

    public void collectFlagInfo(final Class<?> clazz, final FlagIndex<FlagMetadata> index) {
        Field[] fields = clazz.getDeclaredFields();
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
                String name = desc.name() != null && !desc.name().isEmpty() ? desc.name() : field.getName();
                FlagMetadata meta =
                        FlagMetadata.create(clazz.getCanonicalName(), name, desc.alt(), desc.value(), field.getType());
                index.add(meta.flagID(), meta);
            }
        }
    }

    public void scanPackage(String packagePrefix, FlagIndex<FlagMetadata> index, ClassMetadataIndex classMetaIndex) {
        Reflections reflections = new Reflections(packagePrefix);
        Set<Class<?>> classDescs = reflections.getTypesAnnotatedWith(FlagDesc.class);
        for (Class<?> clazz : classDescs) {
            scanClass(clazz, index, classMetaIndex);
        }
    }
}
