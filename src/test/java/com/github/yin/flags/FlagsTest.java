package com.github.yin.flags;

import com.github.yin.flags.annotations.FlagDesc;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 * @author Matej 'Yin' Gagyi
 */
public class FlagsTest {
    @Test
    public void printUsage() throws Exception {
        PrintStream stdout = System.out;
        ByteArrayOutputStream catchStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(catchStream));

        Flags.init(new String[]{"--input", "filename.ext"});

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
        @SuppressWarnings("unused")
        @FlagDesc("A dummy field #flagDocumentation")
        private static final Flag<String> dummy = Flags.string("dummy");
    }
}