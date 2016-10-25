package com.github.yin.flags;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Strores metadata for classes scanned by java-flags. This class gives the client direct access
 * to the underlying {@link Map} for the time being. Accessing an internal collection directly is
 * generally considereed dangerous, so conider that this API is not stable yet.
 *
 * @author Matej 'Yin' Gagyi
 */
public class ClassInfoIndex {
    private final Map<String, ClassMetadata> classes = Maps.newTreeMap();
    // TODO yin: Don't allow direct access to the MAP
    public Map<String, ClassMetadata> classes() {
        return classes;
    }
}
