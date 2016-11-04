package com.github.yin.flags;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.function.Function;

/**
 *  Stores type conversion functions for {@link Flag<T>} accessors. Clients can register own type conversions
 *  and override the default conversions, including {@link String}.
 */
public class TypeConversion {
    private HashMap<Class<?>, Function<String, ?>> typeConversions = Maps.newHashMap();
    private final ImmutableMap DEFAULT_CONVERSIONS = ImmutableMap.<Class<?>, Conversion<?>>builder()
            .put(String.class, new Conversion<String>() {
                @Override
                public String apply(String s) {
                    return s;
                }
            })
            .put(Integer.class, new Conversion<Integer>() {
                @Override
                public Integer apply(String s) {
                    return Integer.parseInt(s);
                }
            })
            .put(Float.class, new Conversion<Float>() {
                @Override
                public Float apply(String s) {
                    return Float.parseFloat(s);
                }
            })
            .put(Double.class, new Conversion<Double>() {
                @Override
                public Double apply(String s) {
                    return Double.parseDouble(s);
                }
            })
            .put(BigInteger.class, new Conversion<BigInteger>() {
                @Override
                public BigInteger apply(String s) {
                    return new BigInteger(s);
                }
            })
            .put(BigDecimal.class, new Conversion<BigDecimal>() {
                @Override
                public BigDecimal apply(String s) {
                    return new BigDecimal(s);
                }
            })
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

    /** Converts {@link String} value of a flag into the desired type. This is pre-Java8 requirement. */
    public interface Conversion<T> {
        T apply(String value);
    }
}
