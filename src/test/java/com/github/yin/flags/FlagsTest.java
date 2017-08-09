package com.github.yin.flags;

import com.github.yin.flags.testclasses.TestFlagDesc;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FlagsTest {
    public static final String TESTFLAGS_PACKAGE = TestFlagDesc.class.getPackage().getName();

    @BeforeClass
    public static void setupClass() {
        //TODO yin: instantiate a flags instance for the future
    }

    @Test
    public void onlyFlag() throws Exception {
        List<String> nonFlags = Flags.parse(new String[]{"--dummy", "value"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "value", TestFlagDesc.getDummy());
        assertEquals(nonFlags.size(), 0);
    }

    @Test
    public void onlyArgs() throws Exception {
        List<String> nonFlags = Flags.parse(new String[]{"just", "args"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "", TestFlagDesc.getDummy());
        assertArrayEquals("Arguments should be: just, args",
                nonFlags.toArray(new String[0]), new String[] { "just", "args" });
    }

    @Test
    public void flagsThenArgs() throws Exception {
        List<String> nonFlags = Flags.parse(new String[]{"--dummy", "value", "just", "args"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "value", TestFlagDesc.getDummy());
        assertArrayEquals("Arguments should be: just, args",
                nonFlags.toArray(new String[0]), new String[] { "just", "args" });
    }

    @Test
    public void argsThenFlags() throws Exception {
        List<String> nonFlags = Flags.parse(new String[]{"just", "args", "--dummy", "value"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "value", TestFlagDesc.getDummy());
        assertArrayEquals("Arguments should be: just, args",
                nonFlags.toArray(new String[0]), new String[] { "just", "args" });

    }

    @Test
    public void validator_valid() throws Exception {
        List<String> nonFlags = Flags.parse(new String[]{"--withValidator", "valid"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "valid", TestFlagDesc.getWithValidator());
    }

    @Test
    public void validator_invalid() throws Exception {
        try {
            List<String> nonFlags = Flags.parse(new String[]{"--withValidator", TestFlagDesc.INVALID_VALUE},
                    Arrays.asList(TESTFLAGS_PACKAGE));
            fail("Should have thrown ParseException");
        } catch(Flags.ParseException ex) {
            assertFalse(ex.getMessage().trim().isEmpty());
            // success
        }
    }

    @Test
    public void printUsage() throws Exception {
        PrintStream stdout = System.out;
        ByteArrayOutputStream catchStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(catchStream));

        Flags.printUsage(TESTFLAGS_PACKAGE);
        String result = new String(catchStream.toByteArray());

        System.setOut(stdout);
        System.out.print(result);

        assertTrue("should print class description", result.contains("#classDocumentation"));
        assertTrue("should print flag description", result.contains("#flagDocumentation"));
        assertTrue("should print flag name", result.contains("dummy"));
    }
}