package com.github.yin.flags;

import com.google.auto.value.AutoValue;

/**
 * Provides Flag value and type-conversion to program classes. To access the argument value,
 * statically initialize a Flag by calling <code>Flags.create(type, name)</code>. Then use
 * method {@link Flag#get()} to get the value.
 *
 * @author yin
 */
@AutoValue
public abstract class Flag<T> {
    private T value;

    static <T> Flag create(FlagID flagID, Class<T> type) {
        return new AutoValue_Flag(flagID, type);
    }

    abstract FlagID flagID();

    abstract Class<T> type();

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
