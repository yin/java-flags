package com.github.yin.flags;

import com.github.yin.flags.testclasses.TestFlagDesc;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlagsTest {

    public static final String TESTFLAGS_PACKAGE = TestFlagDesc.class.getPackage().getName();

    @BeforeClass
    public static void setupClass() {
        //TODO yin: instantiate a flags instance for the future
    }

    @Test
    public void injectValues() throws Exception {
        Flags.parse(new String[]{"--dummy", "value"},
                Arrays.asList(TESTFLAGS_PACKAGE));

        assertEquals("should inject flag value", "value", TestFlagDesc.get());
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