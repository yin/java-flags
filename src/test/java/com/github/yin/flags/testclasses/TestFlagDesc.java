package com.github.yin.flags.testclasses;

import com.github.yin.flags.Flag;
import com.github.yin.flags.Flags;
import com.github.yin.flags.annotations.FlagDesc;
import com.google.common.annotations.VisibleForTesting;

@FlagDesc("This is a dummy class for testing @FlagDesc annotation #classDocumentation")
public class TestFlagDesc {
    public static final String INVALID_VALUE = "INVALID";

    // Many tests might exercise Flags.create(), we don't want this static
    @SuppressWarnings("unused")
    @FlagDesc("A field #flagDocumentation")
    private static final Flag<String> dummy = Flags.create("");

    private static final Flag<String> withValidator = Flags.create("")
            .validator((String in) -> {
                if (INVALID_VALUE.equals(in)) {
                    throw new Flags.ParseException("Invalid value found");
                }
            });

    // Many tests might exercise Flags.create(), we don't want this static
    @SuppressWarnings("unused")
    private static final String notAFlag = "";

    @VisibleForTesting
    public static void getScannedByFlags() {
        Flags.create("");
    }

    public static String getDummy() {
        return dummy.get();
    }

    public static String getWithValidator() {
        return withValidator.get();
    }
}
