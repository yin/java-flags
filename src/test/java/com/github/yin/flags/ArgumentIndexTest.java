package com.github.yin.flags;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class ArgumentIndexTest {

    final Flags.ArgumentIndex index = new Flags.ArgumentIndex(ImmutableMultimap.of(
            "many", "first",
            "many", "second"
    ));

    @Test
    public void all() throws Exception {
        Collection<String> all = index.all(FlagID.create("FakeClass", "many"));
        assertEquals(2, all.size());
        assertTrue(all.contains("first"));
        assertTrue(all.contains("second"));
    }

    @Test
    public void all_notFound() throws Exception {
        Collection<String> all = index.all(FlagID.create("FakeClass", "non-existent"));
        assertEquals(0, all.size());
    }

    @Test
    public void single() throws Exception {
        String single = index.single(FlagID.create("FakeClass", "many"));
        assertEquals(single, "first");
    }

    @Test
    public void single_notFound() throws Exception {
        String single = index.single(FlagID.create("FakeClass", "non-existent"));
        assertNull(single);
    }
}