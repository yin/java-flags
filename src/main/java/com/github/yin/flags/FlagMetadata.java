package com.github.yin.flags;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Stores description and other flag attributes.
 * @author Matej 'Yin' Gagyi
 */
@AutoValue
public abstract class FlagMetadata implements Comparable<FlagMetadata> {
    public static <T> FlagMetadata create(String className, String flagName, String alt, String desc, Class<T> type) {
        return new AutoValue_FlagMetadata(FlagID.create(className, flagName), alt, desc, type);
    }

    public abstract FlagID flagID();

    @Nullable
    public abstract String alt();

    @Nullable
    public abstract String desc();

    public abstract Class<?> type();

    public final int compareTo(FlagMetadata that) {
        return this.flagID().compareTo(that.flagID());
    }
}
