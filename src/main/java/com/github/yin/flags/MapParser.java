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
class MapParser {
    private static final Logger log = LoggerFactory.getLogger(GflagsParser.class);
    private final FlagIndex flags;
    private final TypeConversion typeConversion;

    public MapParser(@Nonnull FlagIndex flags, TypeConversion typeConversion) {
        this.flags = flags;
        this.typeConversion = typeConversion;
    }

    public List<String> parse(Map<String, String> args) {
        for (Map.Entry<String, String> arg : args.entrySet()) {
            next(arg.getKey(), arg.getValue());
        }
        return Collections.EMPTY_LIST;
    }

    protected void next(String key, String value) {
        Collection<Flag<?>> flagsByName = flags.byName().get(key);
        if (flagsByName.size() == 1) {
            handleFlag(flagsByName.iterator().next(), value);
        } else if (flagsByName.isEmpty()) {
            errorUnknownFlag(key);
        } else {
            errorAmbigousFlag(key, flagsByName);
        }
    }

    private void handleFlag(Flag<?> flag, String value) {
            Class<?> type = flag.type();
            TypeConversion.Conversion<?> conversion = typeConversion.forType(type);
            if (conversion != null) {
                ((Flag) flag).set(conversion.apply(value));
            } else {
                throw new UnsupportedOperationException(
                        String.format("Type conversion for Flag<%s>s is registered, Flag: %s",
                                type.getCanonicalName(), flag.flagID().toString()));
            }    }

    protected void errorUnknownFlag(String flag) {
        log.error("Unknown flag: {}", flag);
    }

    protected void errorAmbigousFlag(String flag, Collection<Flag<?>> flagsByName) {
        log.error("Flag {} resolves in multiple classes: {}", flag, flagsByName);
    }
}
