package com.github.yin.flags;

import com.github.yin.flags.analysis.UsagePrinter;
import com.github.yin.flags.annotations.ClassScanner;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Provides static API for creating built-in flags, parsing arguments and
 * injecting flag values into {@link Flag<?>}'s.
 *
 * Client applications should not attempt to parse flags multiple times.
 * Tests are an exception, where we provide method {@link Flags#parse(Map, Iterable)}).
 *
 * Example:
 * <pre>{@link
 * @FlagDesc("Processes some actions from command-line.")
 * public static class ReportMain {
 *
 *     static final String APP_PACKAGE = "com.github.yin.java.flags.example";
 *
 *     @FlagDesc("Print additional information")
 *     static final Flag<Boolean> verbose = Flags.create(false);
 *
 *     public static main(String[] args) {
 *         List<String> arguments = Flags.init(args, Arrays.asList({
 *             APP_PACKAGE
 *         }));
 *         if (arguments.size() > 0) {
 *             if (foo.get() == true) {
 *                 // ...
 *             } else {
 *                 // ...
 *             }
 *         } else {
 *             Flags.printUsage(APP_PACKAGE);
 *         }
 *     }
 * }
 * }</pre>
 */
public class Flags {
    private static Flags instance;
    private final ClassScanner classScanner;
    private final ClassMetadataIndex classMetadataIndex;
    private final FlagIndex<Flag<?>> flagIndex;
    private final FlagIndex<FlagMetadata> flagMetadataIndex;

    /**
     * Initializes flag values from command-line style arguments.
     * @param args command-line arguments to parse values from
     * @param packages list of package roots to scan flags
     */
    @SuppressWarnings("unused")
    public static void parse(String[] args, Iterable<String> packages) {
        instance().scan(packages);
        instance._parse(args);
    }

    /**
     * Creates {@link Flag} accessor for {@link Integer} type.
     */
    public static Flag<Boolean> create(Boolean defaultz) {
        return new BasicFlag.BooleanFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link Integer} type.
     */
    public static Flag<Integer> create(Integer defaultz) {
        return new BasicFlag.IntegerFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link Long} type.
     */
    public static Flag<Long> create(Long defaultz) {
        return new BasicFlag.LongFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link Float} type.
     */
    public static Flag<Float> create(Float defaultz) {
        return new BasicFlag.FloatFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link Double} type.
     */
    public static Flag<Double> create(Double defaultz) {
        return new BasicFlag.DoubleFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link BigInteger} type.
     */
    public static Flag<BigInteger> create(BigInteger defaultz) {
        return new BasicFlag.BigIntegerFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link BigDecimal} type.
     */
    public static Flag<BigDecimal> create(BigDecimal defaultz) {
        return new BasicFlag.BigDecimalFlag(defaultz);
    }

    /**
     * Creates {@link Flag} accessor for {@link String} type.
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
    public static void parse(Map<String, String> options, Iterable<String> packages) {
        instance().scan(packages);
        instance()._parse(options);
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

    private void printUsageForPackage(String packagePrefix) {
        synchronized (this) {
            classScanner.scanPackage(packagePrefix, flagMetadataIndex, classMetadataIndex);
        }
        new UsagePrinter().printUsage(flagMetadataIndex, classMetadataIndex, System.out);
    }


    private static Flags instance() {
        synchronized (Flags.class) {
            if (instance == null) {
                instance = new Flags(new ClassScanner(), new ClassMetadataIndex(), new FlagIndex<>(),
                        new FlagIndex<>());
            }
        }
        return instance;
    }

    private Flags(ClassScanner classScanner, ClassMetadataIndex classMetadataIndex, FlagIndex<Flag<?>> flagIndex,
                  FlagIndex<FlagMetadata> flagMetadataIndex) {
        this.classScanner = classScanner;
        this.classMetadataIndex = classMetadataIndex;
        this.flagIndex = flagIndex;
        this.flagMetadataIndex = flagMetadataIndex;
    }

    private void _parse(String[] args) {
        GflagsParser parser = new GflagsParser(flagIndex);
        parser.parse(args);
    }

    private void _parse(Map<String, String> options) {
        MapParser parser = new MapParser(flagIndex);
        parser.parse(options);
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

}
