package com.github.yin.flags.testclasses;

import com.github.yin.flags.Flag;
import com.github.yin.flags.Flags;
import com.github.yin.flags.annotations.FlagDesc;
import com.google.common.annotations.VisibleForTesting;

@FlagDesc("This is a dummy class for testing @FlagDesc annotation #classDocumentation")
public class TestFlagDesc {
    // Many tests might exercise Flags.create(), we don't want this static
    @SuppressWarnings("unused")
    @FlagDesc("A field #flagDocumentation")
    private static final Flag<String> dummy = Flags.create("");

    // Many tests might exercise Flags.create(), we don't want this static
    @SuppressWarnings("unused")
    private static final String notAFlag = "";

    @VisibleForTesting
    public static void getScannedByFlags() {
        Flags.create("");
    }

    public static String get() {
        return dummy.get();
    }
}
