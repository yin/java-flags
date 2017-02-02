package com.github.yin.flags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.function.Function;

/**
 *  Stores type conversion functions for {@link Flag<?>} accessors. Type conversion is a function taking
 *  a argument String value and returns its representation in another type. Clients can register own
 *  type conversions and override the default conversions, including {@link String}.
 */
@Deprecated
public class TypeConversion {
    private HashMap<Class<?>, Function<String, ?>> typeConversions = Maps.newHashMap();
    private final ImmutableMap DEFAULT_CONVERSIONS = ImmutableMap.<Class<?>, Conversion<?>>builder()
            .put(String.class, s -> s)
            .put(Integer.class, s -> Integer.parseInt(s))
            .put(Float.class, s -> Float.parseFloat(s))
            .put(Double.class, s -> Double.parseDouble(s))
            .put(BigInteger.class, s -> new BigInteger(s))
            .put(BigDecimal.class, s -> new BigDecimal(s))
            .build();

    /** Returns type-conversion function for given type. */
    public <T> Conversion<T> forType(Class<T> type) {
        return typeConversions.containsKey(type)
                ? (Conversion<T>) typeConversions.get(type)
                : (Conversion<T>) DEFAULT_CONVERSIONS.get(type);
    }

    /** Registers a type conversion. */
    public <T> void register(Class<T> type, Function<String, T> conversion) {
        typeConversions.put(type, conversion);
    }

    /** Unregisters type conversion */
    public <T> void unregister(Class<T> type) {
        typeConversions.remove(type);
    }

    /**
     * Converts {@link String} value of a flag into the desired type. This is requirement for
     * pre-Java 8 clients and @FunctionalInterface can not be used here.
     */
    @FunctionalInterface
    public interface Conversion<T> {
        T apply(String value);
    }
}
