package com.github.yin.flags;

import com.github.yin.flags.annotations.FlagDesc;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlagsTest {
    @BeforeClass
    public static void setupClass() {
        //TODO yin: instantiate a flags instance for the future
        Flags.parse(new String[]{"--input", "filename.ext"}, new ArrayList());
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

    @Test
    public void scanCallerClass() throws Exception {
        //TODO yin: ensure
        String classFQN = TestFlagDesc.class.getCanonicalName();

        TestFlagDesc.getScannedByFlags();
        Map<String, ClassMetadata> classes = Flags.classMetadata().classes();
        Map<String, FlagMetadata> flags = Flags.flagMetadata().byFQN();
        ImmutableMultimap flagsByClass = Flags.flagMetadata().byClass();

        ClassMetadata classMeta = classes.get(classFQN);
        assertEquals(classFQN, classMeta.className());
        assertEquals("This is a dummy class for testing @FlagDesc annotation #classDocumentation", classMeta.desc());
        assertEquals("should have found exact number of flags the test class", 1, flagsByClass.get(classFQN).size());

        FlagMetadata flagMeta = flags.get(classFQN + ".dummy");
        assertEquals(Flag.class, flagMeta.type());
        assertEquals(classFQN, flagMeta.flagID().className());
        assertEquals("dummy", flagMeta.flagID().flagName());
        assertEquals("A field #flagDocumentation", flagMeta.desc());
    }

    @FlagDesc("This is a dummy class for testing @FlagDesc annotation #classDocumentation")
    public static class TestFlagDesc {
        // Many tests might exercise Flags.create(), we don't want this static
        @SuppressWarnings("unused")
        @FlagDesc("A field #flagDocumentation")
        private static final Flag<String> dummy = Flags.string("dummy");

        @VisibleForTesting
        static void getScannedByFlags() {
            Flags.create(String.class, "anyflag");
        }
    }
}