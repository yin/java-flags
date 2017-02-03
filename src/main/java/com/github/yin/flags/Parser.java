package com.github.yin.flags;

import java.util.List;
import java.util.Map;

/**
 * Serves as interface for different flag parser implementations.
 */
@FunctionalInterface
public interface Parser<T> {
    List<String> parse(T args);
}
