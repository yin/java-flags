package com.github.yin.flags;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Parses program arguments in a specified format and initializes registered static class fields in different
 * application components. Classes must be registered prior to calling Flags.init().
 *
 * @author matej.gagyi@gmail.com
 */
public class Flags {
    private static ArgumentIndex arguments;
    private static final FlagIndex flagIndex = new FlagIndex();
    private static final List<Error> errors = new ArrayList();
    private static boolean collectErrors = false;

    /**
     * Initializes flag values from command-line style arguments.
     * @param args command-line arguments to parse values from
     */
    public static boolean init(String[] args) {
        arguments = indexArguments(args);
        return arguments != null;
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
<pre>
// @flag(desc="Specifies path to input file")
private static final Flag<String></String> flag_inputPath = Flags.create(String.class, "inputPath")
</pre>
     *
     * @param type of the provided flag value
     * @param name of the flag. This is must match the name the field or name attribute in @Flag annotation.
     * @param <T> is the same as flag value type
     * @return Flag accessor for flag value identified by <code>name</code>
     */
    public static <T> Flag<T> create(Class<T> type, String name) {
        String callerClass = getCallerClassName();
        FlagID id = FlagID.create(callerClass, name);
        Flag<T> flag = Flag.create(id, type, arguments);
        flagIndex.add(id, flag);
        return flag;
    }

    /**
     * Indexes flag values from a <code>Map</code>. This is useful for mocking flag values in
     * integration testing. Please do not missuse this function, there will be better way to inject
     * your logic into flag processing, surely use this only in your tests.
     * @param options Map of flags and their intended values
     */
    @VisibleForTesting
    public static void initForTesting(Map<String, String> options) {
        indexMap(options);
    }

    /**
     * Enables or disables error collection.
     */
    public static void enableErrorCollection(boolean collectErrors) {
        Flags.collectErrors = collectErrors;
    }

    /**
     * @returns Errors collected during flag indexing and access
     */
    public static ImmutableList<Error> getErrors() {
        return ImmutableList.copyOf(errors);
    }

    private static String getCallerClassName() {
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

    private static ArgumentIndex indexArguments(String[] args) {
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
        return acceptor.buildIndex();
    }

    private static ArgumentIndex indexMap(Map<String, String> options) {
        ArgsAcceptor acceptor = new ArgsAcceptor();
        acceptor.startArgs();
        for (Map.Entry<String, String> option : options.entrySet()) {
            acceptor.acceptKey(option.getKey());
            acceptor.acceptValue(option.getValue());
        }
        acceptor.endArgs();
        return acceptor.buildIndex();
    }

    static void emitError(String message, Object... args) {
        if (collectErrors) {
            errors.add(Error.create(message, args));
        }
    }

    @AutoValue
    public abstract static class FlagID implements Comparable<FlagID> {
        static <T> FlagID create(String className, String flagName) {
            return new AutoValue_Flags_FlagID(className, flagName);
        }
        abstract String className();
        abstract String flagName();
        final String fqn() {
            return className() + '.' + flagName();
        }
        public int compareTo(FlagID that) {
            return fqn().compareTo(that.fqn());
        }
    }

    @AutoValue
    public abstract static class FlagDesc {
        static <T> FlagDesc create(String className, String flagName, String alt, String desc, Class<T> type) {
            return new AutoValue_Flags_FlagDesc(className, flagName, alt, desc, type);
        }
        abstract String className();
        abstract String flagName();
        @Nullable abstract String alt();
        @Nullable abstract String desc();
        abstract Class<?> type();

        final String fqn() {
            return className() + '.' + flagName();
        }
    }

    private static final class FlagIndex<T> {
        private final Multimap<String, T> byName = HashMultimap.create();
        private final Multimap<String, T> byClass = HashMultimap.create();
        private final Map<String, T> byFQN = Maps.newTreeMap();
        private ImmutableMultimap<String, T> _byName;
        private ImmutableMultimap<String, T> _byType;
        private ImmutableMap<String, T> _byFQN;

        public void add(FlagID flagID, T flag) {
            String clazz = flagID.className();
            String name = flagID.flagName();
            String fqn = flagID.fqn();
            byName.put(name, flag);
            byClass.put(clazz, flag);
            byFQN.put(fqn, flag);
            _byName = null;
            _byType = null;
            _byFQN = null;
        }

        public Multimap<String, T> byName() {
            return _byName != null ? _byName : (_byName = ImmutableMultimap.copyOf(byName));
        }
        public ImmutableMultimap<String, T> byType() {
            return _byType != null ? _byType : (_byType = ImmutableMultimap.copyOf(byClass));
        }
        public Map<String, T> byFQN() {
            return _byFQN != null ? _byFQN : (_byFQN = ImmutableMap.copyOf(byFQN));
        }
    }

    static class ArgsAcceptor {
        private AcceptorState state;
        private String _key;
        private final Multimap<String, String> arguments = ArrayListMultimap.create();

        enum AcceptorState {KEY_EXPECTED, VALUE_EXPECTED; }

        void startArgs() {
            state = AcceptorState.KEY_EXPECTED;
        }
        void acceptKey(String key) {
            if (state != AcceptorState.KEY_EXPECTED) {
                emitError("Each option is a key-value pair, option " + _key + " is followed directly by " + key);
            }
            this._key = key;
            state = AcceptorState.VALUE_EXPECTED;
        }
        void acceptValue(String value) {
            if (state == AcceptorState.VALUE_EXPECTED) {
                arguments.put(this._key, value);
                state = AcceptorState.KEY_EXPECTED;
            } else {
                emitError("Each option is a key value pair, key has been omitted before argument '" + value + "'");
            }
        }
        void endArgs() {
            if (state != AcceptorState.KEY_EXPECTED) {
                emitError("Each option is a key-value pair, option " + _key + " is the last arguments");
            }
        }
        ArgumentIndex buildIndex() {
            return new ArgumentIndex(ImmutableMultimap.copyOf(arguments));
        }
    }

    static class ArgumentIndex  {
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
     * Stores messsage and parameters of an error emitted by <code>Flags</code>.
     * @author Matej 'Yin' Gagyi
     */

    @AutoValue
    public abstract static class Error {
        public static Error create(String message, Object[] parameters) {
            return new AutoValue_Flags_Error(message, (Object) parameters);
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
