package com.github.yin.flags;

/**
 * Provides Flag value access to client classes. Flag value is injected into static
 * fields of type {@link Flag<?>} and accessed using {@link #get()}.
 *
 * {@link #parse(String)} is never used by client classes, but should be implemented if a custom
 * return type is desired from {@link #get()}.
 *
 * @author yin
 */
public interface Flag<T> {
    /**
     * Attempts parsing a {@link String} representation of a value and is responsible for calling
     * the validator function afterwards.
     */
    void parse(String value);

    /**
     * Returns value stored in the flag, possibly a default value.
     */
    T get();
}
