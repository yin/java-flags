package com.github.yin.flags;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Stores description and other flag attributes.
 * @author yin
 */
@AutoValue
public abstract class FlagMetadata implements Comparable<FlagMetadata> {
    public static <T> FlagMetadata create(String className, String flagName, String desc, Flag<?> flag) {
        return new AutoValue_FlagMetadata(FlagID.create(className, flagName), desc, flag);
    }
    public abstract FlagID flagID();
    @Nullable public abstract String desc();
    public abstract Flag<?> flag();

    public final int compareTo(@Nonnull FlagMetadata that) {
        return this.flagID().compareTo(that.flagID());
    }
}
