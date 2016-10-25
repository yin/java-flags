package com.github.yin.flags;

import com.github.yin.flags.analysis.UsagePrinter;
import com.github.yin.flags.annotations.ClassScanner;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Parses program arguments in a specified format and initializes registered static class fields in different
 * application components. Flags provides static API to the client application and client should not attempt to
 * manipulate own instances of Flags. One exception might be testing. Here, we provide method
 * {@link Flags#initForTesting(Map)}).
 *
 * @author matej.gagyi@gmail.com
 */
public class Flags implements ArgumentProvider {
    private static Flags instance;
    private final ClassScanner classScanner = new ClassScanner();
    private final ClassMetadataIndex classMetadataIndex = new ClassMetadataIndex();
    private final FlagIndex<Flag<?>> flagIndex = new FlagIndex();
    private final FlagIndex<FlagMetadata> flagMetadataIndex = new FlagIndex();
    private ArgumentIndex argumentIndex = ArgumentIndex.EMPTY;

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
            Flag<T> flag = Flag.create(id, type, this);
            flagIndex.add(id, flag);
            return flag;
        } catch (ClassNotFoundException ex) {
            Throwables.propagate(ex);
        }
        return null;
    }

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

    public static void clear() {
        instance().clearArguments();
    }

    public ArgumentIndex arguments() {
        return argumentIndex;
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
                instance = new Flags();
            }
        }
        return instance;
    }

    private void indexArguments(String[] args) {
        Iterator<String> iterator = Iterators.forArray(args);
        ArgsAcceptor acceptor = new ArgsAcceptor();
        acceptor.startArgs();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (arg.startsWith("--")) {
                acceptor.acceptKey(arg.substring(2));
            } else {
                acceptor.acceptValue(arg);
            }
        }
        acceptor.endArgs();
        argumentIndex = acceptor.buildIndex();
    }

    private void indexMap(Map<String, String> options) {
        ArgsAcceptor acceptor = new ArgsAcceptor();
        acceptor.startArgs();
        for (Map.Entry<String, String> option : options.entrySet()) {
            acceptor.acceptKey(option.getKey());
            acceptor.acceptValue(option.getValue());
        }
        acceptor.endArgs();
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

        void startArgs() {
            state = AcceptorState.KEY_EXPECTED;
        }

        void acceptKey(String key) {
            if (state != AcceptorState.KEY_EXPECTED) {
                log.error("Option {} has no value", _key);
            }
            this._key = key;
            state = AcceptorState.VALUE_EXPECTED;
        }

        void acceptValue(String value) {
            //TODO yin: Add support for multi value options and positional arguments
            if (state == AcceptorState.VALUE_EXPECTED) {
                arguments.put(this._key, value);
                state = AcceptorState.KEY_EXPECTED;
            } else {
                log.error("No option before argument '{}' (temporary rule)", value);
            }
        }

        void endArgs() {
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

        public Collection<String> all(String className, String flagName) {
            Collection<String> ret = argumentValues.get(className + '.' + flagName);
            if (ret == null || ret.isEmpty()) {
                ret = argumentValues.get(flagName);
            }
            return ret;
        }

        public String single(String className, String flagName) {
            Collection<String> ret = this.all(className, flagName);
            if (ret != null) {
                Iterator<String> iter = ret.iterator();
                if (iter.hasNext()) {
                    return iter.next();
                }
            }
            return null;
        }
    }

    /**
     * Stores message and parameters of an error emitted by <code>Flags</code>.
     * @author Matej 'Yin' Gagyi
     */
    @AutoValue
    public abstract static class Error {
        public static Error create(String message, Object[] parameters) {
            return new AutoValue_Flags_Error(message, parameters);
        }
        public abstract String message();
        protected abstract Object varargs();
        // TODO yin: AutoValue allows only primitive arrays, Fix the error logging
        public final Object[] parameters() {
            return (Object[]) varargs();
        }
        public final String toString() {
            return String.format(message(), parameters());
        }
    }
}
