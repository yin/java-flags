package com.github.yin.flags;

import com.github.yin.flags.annotations.FlagDesc;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * @author yin
 */
public class FlagsTest {
    @BeforeClass
    public static void setupClass() {
        Flags.init(new String[]{"--input", "filename.ext"});
    }

    @AfterClass
    public static void teardownClass() {
        Flags.clear();
    }

    @Test
    public void printUsage() throws Exception {
        PrintStream stdout = System.out;
        ByteArrayOutputStream catchStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(catchStream));

        Flags.printUsage(TestFlagDesc.class.getPackage().getName());
        String result = new String(catchStream.toByteArray());

        System.setOut(stdout);
        System.out.print(result);

        assertTrue("should print class description", result.contains("#classDocumentation"));
        assertTrue("should print flag description", result.contains("#flagDocumentation"));
        assertTrue("should print flag name", result.contains("dummy"));
    }

    @FlagDesc("This is a dummy class for testing @FlagDesc annotation #classDocumentation")
    public static class TestFlagDesc {
        // Many tests might exercise Flags.create(), we don't want this static
        @SuppressWarnings("unused")
        @FlagDesc("A dummy field #flagDocumentation")
        private static final Flag<String> dummy = Flags.string("dummy");
    }
}