package com.github.yin.flags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by yin on 21.10.16.
 */
/*
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Flag {
    String name();
    String alt();
    String desc();
}
*/
/*
public class Flags {
    private static final FlagIndex index = new FlagIndex();

    public static <T> Flag<T> create(Class<T> valueType) {

    }

    public static void registerClassWithAnnotations(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            Flag[] flags = field.getAnnotationsByType(Flag.class);
            if (flags.length > 0) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    emitError("Field must be static to be used with a @Flag", type, field);
                    continue;
                }
                if ((field.getModifiers() & Modifier.FINAL) != 0) {
                    emitError("Field can not be final to be used with a @Flag", type, field);
                    continue;
                }
                if ((field.isAccessible()) {
                    emitError("Field muist be publicly accessible to be used with a @Flag", type, field);
                    continue;
                }
            }
            for (Flag flag : flags) {
                index.addFlag(type, field, flag);
            }
        }
    }

    private static void setFlagValues(ImmutableMultimap<String, String> arguments) {
        ImmutableMap<String, Collection<String>> valueCollections = arguments.asMap();
        for(String key : arguments.keySet()) {
            Collection<String> values = valueCollections.get(key);
            String firstValue = values.iterator().next();
            for (FlagLink link : flagIndex.byName().get(key)) {
                System.out.println("class:"+key + " arg:"+link.flagDesc().flagName());
                try {
                    link.set(firstValue);
                } catch (Throwable ex) {
                    emitError("Could not set a flag value", link.flagDesc(), ex);
                }
            }
        }
    }

    public static void printUsage(PrintStream out) {
        ImmutableMultimap<String, FlagLink> links = flagIndex.byType();
        for (String className: links.keySet()) {
            Set<FlagLink> classFlags = Sets.newTreeSet(links.get(className));
            out.println(className + ':');
            for (FlagLink link : classFlags) {
                out.println("\t" + link.flagDesc().flagName() + "\t" + link.flagDesc().desc());
                if (link.flagDesc().alt() != null) {
                    out.println(link.flagDesc().alt());
                }
            }
            out.println();
        }
    }

*/

/*
    public abstract static class AnnotationFlagLink implements FlagLink, Comparable<FlagLink> {
        static FlagLink create(Class<?> type, Field field, Flag flag) {
            return new AutoValue_Flags_AnnotationFlagLink(type, field, flag);
        }
        public abstract Class<?> type();
        public abstract Field field();
        public abstract Flag flag();

        private String flagName() {
            return annotation().name() != null ? annotation().name() : field().getName();
        }

        private String flagAlt() {
            return annotation().alt();
        }

        @Override
        public set(String value) {

        }
    }
*/


/*
    public static class Flag<T> {
        private String className;
        private String flagName;
        private ArgumentIndex index;

        private Flag(String className, String flagName, ArgumentIndex index) {
            this.className = className;
            this.flagName = flagName;
            this.index = index;
        }

        public T get() {
            if (index.single(className, flagName);
        }

        void set(T value) {
            this.value = value;
        }
    }

    public interface FlagLink extends Comparable<FlagLink> {
        FlagDesc flagDesc();
        int compareTo(FlagLink that);
    }

    static class GetterFlagLink implements FlagLink {
        private final Flag flag;
        private final FlagDesc flagDesc;

        public GetterFlagLink(Flag flag, FlagDesc flagDesc) {
            this.flag = flag;
            this.flagDesc = flagDesc;
        }

        public FlagDesc flagDesc() {
            return flagDesc;
        }

        public void set(String value) {
            if (flagDesc.type().isAssignableFrom(value.getClass())) {
                flag.set(value);
            } else {
                emitError("Can't convert argument value to target type", this);
            }
        }

        public int compareTo(FlagLink that) {
            return this.flagDesc().compareTo(that.flagDesc());
        }
    }


    private static final class FlagIndex {
        private final Multimap<String, FlagLink> byName = TreeMultimap.create();
        private final Multimap<String, FlagLink> byClass = HashMultimap.create();
        private final Map<String, FlagLink> byFQN = Maps.newTreeMap();
        private ImmutableMultimap<String, FlagLink> _byName;
        private ImmutableMultimap<String, FlagLink> _byType;
        private ImmutableMap<String, FlagLink> _byFQN;

        public void addFlag(FlagLink link) {
            FlagDesc flagDesc = link.flagDesc();
            String clazz = flagDesc.className();
            String name = flagDesc.flagName();
            String alt = flagDesc.alt();
            String fqn = flagDesc.fqn();
            byName.put(name, link);
            if (alt != null) {
                byName.put(alt, link);
            }
            byClass.put(clazz, link);
            byFQN.put(fqn, link);
            _byName = null;
            _byType = null;
            _byFQN = null;
        }

        public Multimap<String, FlagLink> byName() {
            return _byName != null ? _byName : (_byName = ImmutableMultimap.copyOf(byName));
        }
        public ImmutableMultimap<String, FlagLink> byType() {
            return _byType != null ? _byType : (_byType = ImmutableMultimap.copyOf(byClass));
        }
        public Map<String, FlagLink> byFQN() {
            return _byFQN != null ? _byFQN : (_byFQN = ImmutableMap.copyOf(byFQN));
        }
    }

 */