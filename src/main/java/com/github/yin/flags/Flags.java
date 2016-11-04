package com.github.yin.flags;

import com.github.yin.flags.analysis.UsagePrinter;
import com.github.yin.flags.annotations.ClassScanner;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

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
public class Flags implements ArgumentProvider {
    private static Flags instance;
    private final ClassScanner classScanner;
    private final ClassMetadataIndex classMetadataIndex;
    private final FlagIndex<Flag<?>> flagIndex;
    private final FlagIndex<FlagMetadata> flagMetadataIndex;
    private ArgumentIndex argumentIndex = ArgumentIndex.EMPTY;

    private final TypeConversion typeConversion;

    /**
     * Initializes flag values from command-line style arguments.
     * @param args command-line arguments to parse values from
     */
    public static void init(String[] args) {
        instance().indexArguments(args);
    }

    /**
     * Returns flag value accessor for <code>String</code> type. See create().
     */
    public static Flag<String> string(String name) {
        return create(String.class, name);
    }

    /**
     * Returns flag value accessor, which can be used to retrieve the actual flag value supplied
     * by command line arguments or by other mechanism (such as <code>initForTesting()</code>).
     * Preferably the typespecific variants (<code>string()</code>, ...) should be used for
     * readability reasons.
     *
     * <pre>{@code
     * &at;FlagDesc("Specifies path to input file")
     * private static final Flag&lt;String&gt; flag_inputPath = Flags.create(String.class, "inputPath");
     * }</pre>
     *
     * @param type of the provided flag value
     * @param name of the flag. This is must match the name the field or name attribute in @FlagDesc annotation.
     * @param <T> is the same as flag value type
     * @return Flag accessor for flag value identified by <code>name</code>
     */
    public static <T> Flag<T> create(Class<T> type, String name) {
        return instance().createFlag(type, name);
    }

    private <T> Flag<T> createFlag(Class<T> type, String name) {
        try {
            String callerClass = scanCallerClass();
            FlagID id = FlagID.create(callerClass, name);
            Flag<T> flag = Flag.create(id, type, this, typeConversion);
            flagIndex.add(id, flag);
            return flag;
        } catch (ClassNotFoundException ex) {
            Throwables.propagate(ex);
        }
        //Never reachable: Throwables.propagate() always throws, but IDE does not have this knowledge
        return null;
    }

    /** Prints user-readable usage help for all flags in a given package */
    // TODO yin: Subsequent calls will always print previously scanned packages, fix
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

    /** Clears the argument values. */
    public static void clear() {
        instance().clearArguments();
    }

    /** Returns an {@link ArgumentIndex} instance used to query for argument values. */
    public ArgumentIndex arguments() {
        return argumentIndex;
    }

    /** Type conversions used by Flags. */
    public TypeConversion getTypeConversion() {
        return typeConversion;
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
                instance = new Flags(new ClassScanner(), new ClassMetadataIndex(), new FlagIndex<Flag<?>>(),
                        new FlagIndex<FlagMetadata>(), new TypeConversion());
            }
        }
        return instance;
    }

    private Flags(ClassScanner classScanner, ClassMetadataIndex classMetadataIndex, FlagIndex<Flag<?>> flagIndex, FlagIndex<FlagMetadata> flagMetadataIndex, TypeConversion typeConversion) {
        this.classScanner = classScanner;
        this.classMetadataIndex = classMetadataIndex;
        this.flagIndex = flagIndex;
        this.flagMetadataIndex = flagMetadataIndex;
        this.argumentIndex = ArgumentIndex.EMPTY;
        this.typeConversion = typeConversion;
    }

    private void indexArguments(String[] args) {
        Iterator<String> iterator = Iterators.forArray(args);
        ArgsAcceptor acceptor = new ArgsAcceptor();
        acceptor.start();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (arg.startsWith("--")) {
                acceptor.key(arg.substring(2));
            } else {
                acceptor.value(arg);
            }
        }
        acceptor.end();
        argumentIndex = acceptor.buildIndex();
    }

    private void indexMap(Map<String, String> options) {
        ArgsAcceptor acceptor = new ArgsAcceptor();
        acceptor.start();
        for (Map.Entry<String, String> option : options.entrySet()) {
            acceptor.key(option.getKey());
            acceptor.value(option.getValue());
        }
        acceptor.end();
        argumentIndex = acceptor.buildIndex();
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

    private void clearArguments() {
        argumentIndex = ArgumentIndex.EMPTY;
    }

    //TODO yin: Change into ArgumentIndexBuilder and make it look nice
    static class ArgsAcceptor {
        private static final Logger log = LoggerFactory.getLogger(ArgsAcceptor.class);
        private final Multimap<String, String> arguments = ArrayListMultimap.create();
        private AcceptorState state;
        private String _key;

        enum AcceptorState {KEY_EXPECTED, VALUE_EXPECTED}

        void start() {
            state = AcceptorState.KEY_EXPECTED;
        }

        void key(String key) {
            if (state != AcceptorState.KEY_EXPECTED) {
                log.error("Option {} has no value", _key);
            }
            this._key = key;
            state = AcceptorState.VALUE_EXPECTED;
        }

        void value(String value) {
            //TODO yin: Add support for multi value options and positional arguments
            if (state == AcceptorState.VALUE_EXPECTED) {
                arguments.put(this._key, value);
                state = AcceptorState.KEY_EXPECTED;
            } else {
                log.error("No option before argument '{}' (temporary rule)", value);
            }
        }

        void end() {
            if (state != AcceptorState.KEY_EXPECTED) {
                log.error("Option {} has no value", _key);
            }
        }

        ArgumentIndex buildIndex() {
            return new ArgumentIndex(ImmutableMultimap.copyOf(arguments));
        }
    }

    static class ArgumentIndex  {
        public static final ArgumentIndex EMPTY = new ArgumentIndex(ImmutableMultimap.<String, String>of());
        private final ImmutableMultimap<String, String> argumentValues;

        public ArgumentIndex(ImmutableMultimap<String, String> argumentValues) {
            this.argumentValues = argumentValues;
        }


        public Collection<String> all(FlagID flagID) {
            Collection<String> ret = argumentValues.get(flagID.className() + '.' + flagID.flagName());
            if (ret == null || ret.isEmpty()) {
                ret = argumentValues.get(flagID.flagName());
            }
            return ret;
        }

        public String single(FlagID flagID) {
            Collection<String> ret = this.all(flagID);
            if (ret != null) {
                Iterator<String> iter = ret.iterator();
                if (iter.hasNext()) {
                    return iter.next();
                }
            }
            return null;
        }
    }
}
