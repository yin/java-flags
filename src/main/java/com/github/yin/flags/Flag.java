package com.github.yin.flags;

import com.google.auto.value.AutoValue;

/**
 * @author Matej 'Yin' Gagyi
 */
@AutoValue
public abstract class Flag<T> {
    static <T> Flag create(Flags.FlagID flagID, Class<T> type, Flags.ArgumentIndex index) {
        return new AutoValue_Flag(flagID, type, index);
    }

    abstract Flags.FlagID flagID();

    abstract Class<T> type();

    abstract Flags.ArgumentIndex index();

    public T get() {
        if (type().isAssignableFrom(String.class)) {
            String value = index().single(flagID().className(), flagID().flagName());
            return (T) value;
        } else {
            Flags.emitError("Flags must be of type String for now", flagID());
        }
        return null;
    }
}
