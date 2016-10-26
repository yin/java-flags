package com.github.yin.flags;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * @author yin
 */
@RunWith(MockitoJUnitRunner.class)
public class FlagTest {
    private static final Class<?> UNSUPPORTED_TYPE = Integer.class;
    @Mock ArgumentProvider mockArgumentProvider;
    @Mock Flags.ArgumentIndex mockArgumentIndex;

    @Before
    public void setUp() {
        doReturn(mockArgumentIndex).when(mockArgumentProvider).arguments();
        doReturn("test.log").when(mockArgumentIndex).single(anyString(), eq("input"));
    }

    @Test
    public void get_String() throws Exception {
        assertEquals("should return arguments as String",
                "test.log", Flag.create(FlagID.create("FakeClass", "input"), String.class, mockArgumentProvider).get());
        // Do we need to test this interaction?
        verify(mockArgumentIndex).single("FakeClass", "input");
    }

    @Test
    public void get_unsupported() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "input"), UNSUPPORTED_TYPE, mockArgumentProvider).get();
            fail("should throw UOE when type conversion for target type is missing");
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }
}