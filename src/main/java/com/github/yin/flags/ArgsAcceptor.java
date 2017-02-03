package com.github.yin.flags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Accepts arguments, flags and their values and injects the values into @{Flag}'s
 * using {@link TypeConversion}.
 */
class ArgsAcceptor {
    private static final Logger log = LoggerFactory.getLogger(ArgsAcceptor.class);
    private final List<String> arguments = new ArrayList<String>();
    private final FlagIndex flags;
    private final TypeConversion typeConverison;
    private AcceptorState state;
    private Flag lastFlag;

    enum AcceptorState {KEY_EXPECTED, VALUE_EXPECTED}

    public ArgsAcceptor(@Nonnull FlagIndex flags, @Nonnull TypeConversion typeConversion) {
        this.flags = flags;
        this.typeConverison = typeConversion;
    }

    void start() {
        state = AcceptorState.KEY_EXPECTED;
    }

    void key(String key, String original) {
        if (state != AcceptorState.KEY_EXPECTED) {
            errorFlagHasNoValue();
        }
        Collection<Flag<?>> flagsByName = flags.byName().get(key);
        if (flagsByName.isEmpty() && parseBooleanFlag(key) == false) {
            flagsByName = flags.byName().get(key.substring(2));
        }
        if (flagsByName.size() == 1) {
            flag(flagsByName.iterator().next(), key);
        } else if (flagsByName.isEmpty()) {
            errorUnknownFlag(original);
        } else {
            errorAmbigousFlag(original, flagsByName);
        }
    }

    private void flag(Flag<?> flag, String key) {
        Class<?> flagtype = flag.type();
        if (flagtype == Boolean.class) {
            ((Flag<Boolean>) flag).set(parseBooleanFlag(key));
        } else {
            this.lastFlag = flag;
            state = AcceptorState.VALUE_EXPECTED;
        }
    }

    void value(String value) {
        //TODO yin: Add support for multi value options and positional arguments
        if (state == AcceptorState.VALUE_EXPECTED) {
            Class<?> type = lastFlag.type();
            TypeConversion.Conversion<?> conversion = typeConverison.forType(type);
            if (conversion != null) {
                lastFlag.set(conversion.apply(value));
            } else {
                throw new UnsupportedOperationException(
                        String.format("Type conversion for Flag<%s>s is registered, Flag: %s",
                                type.getCanonicalName(), lastFlag.flagID().toString()));
            }
            state = AcceptorState.KEY_EXPECTED;
        } else {
            arguments.add(value);
        }
    }

    public List<String> end() {
        if (state != AcceptorState.KEY_EXPECTED) {
            errorFlagHasNoValue();
        }
        return arguments;
    }

    private boolean parseBooleanFlag(String key) {
        return key.startsWith("no");
    }

    private void errorUnknownFlag(String flag) {
        log.error("Unknown flag: {}", flag);
    }

    private void errorAmbigousFlag(String flag, Collection<Flag<?>> flagsByName) {
        log.error("Flag {} resolves in multiple classes: {}", flag, flagsByName);
    }

    private void errorFlagHasNoValue() {
        log.error("Option {} has no value", lastFlag);
    }
}
