package com.github.yin.flags;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Handles {@link #get()}, and {@link #validator(Validator)}, leaving {@link #parse(String)}
 * for implementation by clients.
 */
public abstract class BasicFlag<T> implements Flag<T> {
    public static class StringFlag extends BasicFlag<String> {
        public StringFlag(@Nonnull String defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = String.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class BooleanFlag extends BasicFlag<Boolean> {
        public BooleanFlag(@Nonnull Boolean defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = Boolean.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class IntegerFlag extends BasicFlag<Integer> {
        public IntegerFlag(@Nonnull Integer defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = Integer.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class LongFlag extends BasicFlag<Long> {
        public LongFlag(@Nonnull Long defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = Long.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class FloatFlag extends BasicFlag<Float> {
        public FloatFlag(@Nonnull Float defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = Float.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class DoubleFlag extends BasicFlag<Double> {
        public DoubleFlag(@Nonnull Double defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = Double.valueOf(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class BigIntegerFlag extends BasicFlag<BigInteger> {
        public BigIntegerFlag(@Nonnull BigInteger defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = new BigInteger(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    public static class BigDecimalFlag extends BasicFlag<BigDecimal> {
        public BigDecimalFlag(@Nonnull BigDecimal defaultz) {
            super(defaultz);
        }

        @Override
        public void parse(@Nonnull String value) {
            this.value = new BigDecimal(value);
            if (validator != null) {
                validator.validate(this.value);
            }
        }
    }

    protected Validator<T> validator;
    protected T value;

    public BasicFlag(T defaultz) {
        value = defaultz;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Flag<T> validator(Validator<T> validator) {
        this.validator = validator;
        return this;
    }
}
