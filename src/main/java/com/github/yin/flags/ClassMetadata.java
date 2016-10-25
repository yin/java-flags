package com.github.yin.flags;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Stores description and other flag attributes.
 * @author Matej 'Yin' Gagyi
 */
@AutoValue
public abstract class ClassMetadata {
    public static <T> ClassMetadata create(String className, String desc) {
        return new AutoValue_ClassMetadata(className, desc);
    }
    public abstract String className();
    @Nullable public abstract String desc();
}
