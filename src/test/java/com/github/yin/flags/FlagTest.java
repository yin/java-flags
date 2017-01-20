package com.github.yin.flags;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.Object;

import static org.junit.Assert.assertEquals;

/**
 * @author yin
 */
@RunWith(MockitoJUnitRunner.class)
public class FlagTest {
    @Test
    public void flagId() throws Exception {
        FlagID flagID = FlagID.create("FakeClass", "str");
        assertEquals(Flag.create(flagID, Object.class).flagID(), flagID);
    }

    @Test
    public void set_get() throws Exception {
        Flag flag = Flag.create(FlagID.create("FakeClass", "str"), String.class);
        flag.set("This is a value");
        assertEquals(flag.get(), "This is a value");
    }
}