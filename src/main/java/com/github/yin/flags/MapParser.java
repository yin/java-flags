package com.github.yin.flags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parses a {@link Map} of flags and {@link String} values.
 */
public class MapParser implements Parser<Map<String, String>> {
    private static final Logger log = LoggerFactory.getLogger(GflagsParser.class);
    private final FlagIndex flags;

    public MapParser(@Nonnull FlagIndex flags) {
        this.flags = flags;
    }

    @Override
    public List<String> parse(Map<String, String> args) {
        for (Map.Entry<String, String> arg : args.entrySet()) {
            next(arg.getKey(), arg.getValue());
        }
        return Collections.EMPTY_LIST;
    }

    protected void next(String key, String value) {
        Collection<Flag<?>> flagsByName = flags.byName().get(key);
        if (flagsByName.size() == 1) {
            flagsByName.iterator().next().parse(value);
        } else if (flagsByName.isEmpty()) {
            errorUnknownFlag(key);
        } else {
            errorAmbigousFlag(key, flagsByName);
        }
    }

    protected void errorUnknownFlag(String flag) {
        log.error("Unknown flag: {}", flag);
    }

    protected void errorAmbigousFlag(String flag, Collection<Flag<?>> flagsByName) {
        log.error("Flag {} resolves in multiple classes: {}", flag, flagsByName);
    }
}
