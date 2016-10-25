package com.github.yin.flags.annotations;

import com.github.yin.flags.ClassMetadata;
import com.github.yin.flags.FlagIndex;
import com.github.yin.flags.FlagMetadata;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

/**
 * @author Matej 'Yin' Gagyi
 */
public class ClassScanner {
    private static final Logger log  = LoggerFactory.getLogger(ClassScanner.class);

    // TODO yin: it would be also useful to return the metadata in a list
    public void scanClass(final String className, final FlagIndex<FlagMetadata> index) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        ClassMetadata classInfo = collectClassMetadata(clazz);
        collectFlagInfo(clazz, index);
    }

    private ClassMetadata collectClassMetadata(Class<?> clazz) {
        return ClassMetadata.create(clazz.getName(), null);
    }

    public void collectFlagInfo(final Class<?> clazz, final FlagIndex<FlagMetadata> index) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            FlagDesc[] flags = field.getAnnotationsByType(FlagDesc.class);
            if (flags.length > 1) {
                log.error("Multiple @FlagDesc occurrences on field: {}", field.toString());
            }
            if ((field.getModifiers() & Modifier.STATIC) == 0) {
                log.error("@FlagDesc on non-static field: {}", field.toString());
                continue;
            }
            if ((field.getModifiers() & Modifier.FINAL) != 0) {
                log.error("@FlagDesc on non-final field: {}", field);
                continue;
            }
            if (field.isAccessible()) {
                log.error("@FlagDesc on non-accessible filed: {}", field);
                continue;
            }
            for (FlagDesc flag : flags) {
                String name = flag.name() != null ? flag.name() : field.getName();
                FlagMetadata meta =
                        FlagMetadata.create(clazz.getCanonicalName(), name, flag.alt(), flag.desc(), field.getType());
                index.add(meta.flagID(), meta);
            }
        }
    }

    /*
public class Flags {
    private static final FlagIndex index = new FlagIndex();

    public static <T> FlagMetadata<T> create(Class<T> valueType) {

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
        ImmutableMultimap<String, FlagLink> links = flagIndex.byClass();
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
        static FlagLink create(Class<?> type, Field field, FlagMetadata flag) {
            return new AutoValue_Flags_AnnotationFlagLink(type, field, flag);
        }
        public abstract Class<?> type();
        public abstract Field field();
        public abstract FlagMetadata flag();

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
    public static class FlagMetadata<T> {
        private String className;
        private String flagName;
        private ArgumentIndex index;

        private FlagMetadata(String className, String flagName, ArgumentIndex index) {
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
        FlagMetadata flagDesc();
        int compareTo(FlagLink that);
    }

    static class GetterFlagLink implements FlagLink {
        private final FlagMetadata flag;
        private final FlagMetadata flagDesc;

        public GetterFlagLink(FlagMetadata flag, FlagMetadata flagDesc) {
            this.flag = flag;
            this.flagDesc = flagDesc;
        }

        public FlagMetadata flagDesc() {
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
            FlagMetadata flagDesc = link.flagDesc();
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
        public ImmutableMultimap<String, FlagLink> byClass() {
            return _byType != null ? _byType : (_byType = ImmutableMultimap.copyOf(byClass));
        }
        public Map<String, FlagLink> byFQN() {
            return _byFQN != null ? _byFQN : (_byFQN = ImmutableMap.copyOf(byFQN));
        }
    }

 */
}
