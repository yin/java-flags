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
    static <T> Flag create(FlagID flagID, Class<T> type, ArgumentProvider index) {
        return new AutoValue_Flag(flagID, type, index);
    }

    abstract FlagID flagID();

    abstract Class<T> type();

    abstract ArgumentProvider flags();

    public T get() {
        if (type().isAssignableFrom(String.class)) {
            String value = flags().arguments().single(flagID().className(), flagID().flagName());
            return (T) value;
        } else {
            throw new UnsupportedOperationException(
                    String.format("Type conversion for Flag<%s>s is not implemented, Flag: %s",
                            type().getCanonicalName(), flagID().toString()));
        }
    }

}
