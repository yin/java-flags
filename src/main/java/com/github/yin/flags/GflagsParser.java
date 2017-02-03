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
class GflagsParser {
    private static final Logger log = LoggerFactory.getLogger(GflagsParser.class);
    private final List<String> arguments = new ArrayList<String>();
    private final FlagIndex flags;
    private final TypeConversion typeConverison;
    private AcceptorState state;
    private Flag lastFlag;

    enum AcceptorState {KEY_EXPECTED, VALUE_EXPECTED}

    public GflagsParser(@Nonnull FlagIndex flags, @Nonnull TypeConversion typeConversion) {
        this.flags = flags;
        this.typeConverison = typeConversion;
    }

    public List<String> parse(String[] args) {
        start();
        for (String arg : args) {
            next(arg);
        }
        return end();
    }

    protected void start() {
        state = AcceptorState.KEY_EXPECTED;
    }

    protected void next(String arg) {
        if (arg.startsWith("--")) {
            flag(arg.substring(2), arg);
        } else if (arg.startsWith("-")) {
            flag(arg.substring(1), arg);
        } else {
            handleValue(arg);
        }
    }

    protected void flag(String flag, String orig) {
        int i = flag.indexOf('=');
        if (i == -1) {
            key(flag, orig);
        } else {
            String key = flag.substring(0, i);
            String value = flag.substring(i + 1, flag.length() - i - 1);
            keyAndValue(key, value, orig);
        }
    }

    protected void key(String key, String original) {
        if (state != AcceptorState.KEY_EXPECTED) {
            errorFlagHasNoValue();
        }
        Collection<Flag<?>> flagsByName = flags.byName().get(key);
        if (flagsByName.isEmpty() && key.startsWith("no")) {
            flagsByName = flags.byName().get(key.substring(2));
            if (flagsByName.size() == 1) {
                handleFalseFlag(flagsByName.iterator().next(), original);
            } else if (flagsByName.isEmpty()) {
                errorUnknownFlag(original);
            } else {
                errorAmbigousFlag(original, flagsByName);
            }
        }
        if (flagsByName.size() == 1) {
            handleFlag(flagsByName.iterator().next());
        } else if (flagsByName.isEmpty()) {
            errorUnknownFlag(original);
        } else {
            errorAmbigousFlag(original, flagsByName);
        }
    }

    protected void keyAndValue(String key, String value, String original) {
        if (state != AcceptorState.KEY_EXPECTED) {
            errorFlagHasNoValue();
        }
        Collection<Flag<?>> flagsByName = flags.byName().get(key);
        if (flagsByName.size() == 1) {
            handleFlag(flagsByName.iterator().next());
            handleValue(value);
        } else if (flagsByName.isEmpty()) {
            errorUnknownFlag(original);
        } else {
            errorAmbigousFlag(original, flagsByName);
        }
    }

    protected void handleFlag(Flag<?> flag) {
        Class<?> flagtype = flag.type();
        if (flagtype == Boolean.class) {
            ((Flag<Boolean>) flag).set(true);
        } else {
            this.lastFlag = flag;
            state = AcceptorState.VALUE_EXPECTED;
        }
    }

    protected void handleFalseFlag(Flag<?> flag, String orig) {
        Class<?> flagtype = flag.getClass();
        if (flagtype == Boolean.class) {
            ((Flag<Boolean>) flag).set(false);
        } else {
            errorUnknownFlag(orig);
        }
    }

    //TODO yin: Add support for multi value options and positional arguments
    protected void handleValue(String value) {
        //TODO yin: Add support for collections
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

    protected List<String> end() {
        if (state != AcceptorState.KEY_EXPECTED) {
            errorFlagHasNoValue();
        }
        return arguments;
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
