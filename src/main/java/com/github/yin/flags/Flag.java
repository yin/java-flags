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
    static <T> Flag create(FlagID flagID, Class<T> type, ArgumentProvider index, TypeConversion typeConversion) {
        return new AutoValue_Flag(flagID, type, index, typeConversion);
    }

    abstract FlagID flagID();

    abstract Class<T> type();

    protected abstract ArgumentProvider flags();

    protected abstract TypeConversion typeConversion();

    public T get() {
        TypeConversion.Conversion<T> conversion = typeConversion().forType(type());
        if (conversion != null) {
            String value = flags().arguments().single(flagID());
            return conversion.apply(value);
        } else {
            throw new UnsupportedOperationException(
                    String.format("Type conversion for Flag<%s>s is registered, Flag: %s",
                            type().getCanonicalName(), flagID().toString()));
        }
    }
}
