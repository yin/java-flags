package com.github.yin.flags;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Stores description and other flag attributes.
 * @author Matej 'Yin' Gagyi
 */
@AutoValue
public abstract class FlagMetadata {
    public static <T> FlagMetadata create(String className, String flagName, String alt, String desc, Class<T> type) {
        return new AutoValue_FlagMetadata(FlagID.create(className, flagName), alt, desc, type);
    }

    public abstract FlagID flagID();

    @Nullable
    public abstract String alt();

    @Nullable
    public abstract String desc();

    public abstract Class<?> type();

    public final String fqn() {
        return flagID().fqn();
    }
}
