package com.github.yin.flags;

import com.github.yin.flags.analysis.UsagePrinter;
import com.github.yin.flags.annotations.ClassScanner;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Parses program arguments in a specified format and initializes registered static class fields in different
 * application components. Flags provides static API to the client application and client should not attempt to
 * manipulate own instances of Flags. One exception might be testing. Here, we provide method
 * {@link Flags#initForTesting(Map)}).
 *
 * Example:
 * <pre>{@code
 * @FlagDesc("Processes report in current directory.")
 * public static class ReportMain {
 *
 *     @FlagDesc("if 'true', prints additional information")
 *     static final Flag<String> verbose = Flags.string("verbose");
 *
 *     public static main(String[] args) {
 *         Flags.init(args);
 *         if (foo.get().equals("true")) {
 *             // ...
 *         } else if (foo.get().equals("false")) {
 *             // ...
 *         } else {
 *             Flags.printUsage("com.example");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @author yin
 */
public class Flags {
    private static Flags instance;
    private final ClassScanner classScanner;
    private final ClassMetadataIndex classMetadataIndex;
    private final FlagIndex<Flag<?>> flagIndex;
    private final FlagIndex<FlagMetadata> flagMetadataIndex;
    private final BasicFlag typeConversion;

    /**
     * Initializes flag values from command-line style arguments.
     * @param args command-line arguments to parse values from
     * @param packages list of package roots to scan flags
     */
    public static void parse(String[] args, Iterable<String> packages) {
        instance().scan(packages);
        instance().parseArguments(args);
    }

    /**
     * Returns flag value accessor for <code>String</code> type. See create().
     */
    public static Flag<String> create(String defaultz) {
        return new BasicFlag.StringFlag(defaultz);
    }


    /** Prints user-readable usage help for all flags in a given package */
    public static void printUsage(String packagePrefix) {
        instance().printUsageForPackage(packagePrefix);
    }

    /**
     * Indexes flag values from a <code>Map</code>. This is useful for mocking flag values in
     * integration testing. Please do not misuse this function, there will be better way to inject
     * your logic into flag processing, surely use this only in your tests.
     * @param options Map of flags and their intended values
     */
    @VisibleForTesting
    public static void initForTesting(Map<String, String> options) {
        instance().indexMap(options);
    }

    @VisibleForTesting
    static ClassMetadataIndex classMetadata() {
        return instance().classMetadataIndex;
    }

    @VisibleForTesting
    static FlagIndex flagMetadata() {
        return instance().flagMetadataIndex;
    }

    private void scan(Iterable<String> packages) {
        for (String pkg : packages) {
            classScanner.scanPackage(pkg, flagMetadataIndex, classMetadataIndex);
        }
    }

    private void printUsageForPackage(String packageProfix) {
        synchronized (this) {
            classScanner.scanPackage(packageProfix, flagMetadataIndex, classMetadataIndex);
        }
        new UsagePrinter().printUsage(flagMetadataIndex, classMetadataIndex, System.out);
    }

    private String scanCallerClass() throws ClassNotFoundException {
        String className = getCallerClassName();
        synchronized (this) {
            classScanner.scanClass(className, flagMetadataIndex, classMetadataIndex);
        }
        return className;
    }

    private static Flags instance() {
        synchronized (Flags.class) {
            if (instance == null) {
                instance = new Flags(new ClassScanner(), new ClassMetadataIndex(), new FlagIndex<>(),
                        new FlagIndex<>(), new BasicFlag());
            }
        }
        return instance;
    }

    private Flags(ClassScanner classScanner, ClassMetadataIndex classMetadataIndex, FlagIndex<Flag<?>> flagIndex, FlagIndex<FlagMetadata> flagMetadataIndex, BasicFlag typeConversion) {
        this.classScanner = classScanner;
        this.classMetadataIndex = classMetadataIndex;
        this.flagIndex = flagIndex;
        this.flagMetadataIndex = flagMetadataIndex;
        this.typeConversion = typeConversion;
    }

    private void parseArguments(String[] args) {
        ArgsAcceptor acceptor = new ArgsAcceptor(flagIndex, typeConversion);
        acceptor.start();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                acceptor.key(arg.substring(2), arg);
            } else if (arg.startsWith("-")) {
                acceptor.key(arg.substring(1), arg);
            } else {
                acceptor.value(arg);
            }
        }
        acceptor.end();
    }

    private void indexMap(Map<String, String> options) {
        ArgsAcceptor acceptor = new ArgsAcceptor(flagIndex, typeConversion);
        acceptor.start();
        for (Map.Entry<String, String> option : options.entrySet()) {
            acceptor.key(option.getKey(), option.getKey());
            acceptor.value(option.getValue());
        }
        acceptor.end();
    }

    private String getCallerClassName() {
        StackTraceElement[] stackTrace =  Thread.currentThread().getStackTrace();
        String myType = Flags.class.getCanonicalName();
        String threadType =  Thread.class.getCanonicalName();
        for (StackTraceElement e : stackTrace) {
            if (!e.getClassName().equals(myType) && !e.getClassName().equals(threadType)) {
                return e.getClassName();
            }
        }
        return null;
    }

    static class ArgsAcceptor {
        private static final Logger log = LoggerFactory.getLogger(ArgsAcceptor.class);
        private final List<String> arguments = new ArrayList<String>();
        private final FlagIndex flags;
        private final BasicFlag typeConverison;
        private AcceptorState state;
        private Flag lastFlag;

        enum AcceptorState {KEY_EXPECTED, VALUE_EXPECTED}

        public ArgsAcceptor(@Nonnull FlagIndex flags, @Nonnull BasicFlag typeConversion) {
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
                BasicFlag.Conversion<?> conversion = typeConverison.forType(type);
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
            log.error("Flag {} resolves in multiple classes: {}" , flag, flagsByName);
        }

        private void errorFlagHasNoValue() {
            log.error("Option {} has no value", lastFlag);
        }
    }
}
