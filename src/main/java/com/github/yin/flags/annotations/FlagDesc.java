package com.github.yin.flags.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates class and fields with flag descriptions, seen in e.g. usage help. If supplied,
 * {@link FlagDesc#name()} is used to determine flag name, otherwise field name of the
 * annotated field is used.
 *
 * For classpath-scanning to work correctly, each class having a @FlagDesc member must be
 * itself annotated with @FlagDesc.
 *
 * <pre>{@code
 * @FlagDesc("This class uses java-flags")
 * public class TestFlagDesc {
 *     @FlagDesc("This is a java-flag accessor")
 *     private static final Flag<String> arg_inputFile = Flags.string("inputFile");
 * }
 * }</pre>
 *
 * @author Matej 'Yin' Gagyi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE})
public @interface FlagDesc {
    /** Name of flag */
    String name() default "";
    /** Not used */
    String alt() default "";
    /** Flag description */
    String value();
}
