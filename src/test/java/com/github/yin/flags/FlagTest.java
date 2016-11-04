package com.github.yin.flags;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.Object;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * @author yin
 */
@RunWith(MockitoJUnitRunner.class)
public class FlagTest {
    private static final Class<?> UNSUPPORTED_TYPE = Object.class;
    @Mock ArgumentProvider mockArgumentProvider;
    @Mock Flags.ArgumentIndex mockArgumentIndex;
    TypeConversion typeConversion = new TypeConversion();

    @Before
    public void setUp() {
        doReturn(mockArgumentIndex).when(mockArgumentProvider).arguments();
        doReturn("test.log").when(mockArgumentIndex).single(FlagID.create("FakeClass", "str"));
        doReturn("123").when(mockArgumentIndex).single(FlagID.create("FakeClass", "int"));
        doReturn("12345.6789").when(mockArgumentIndex).single(FlagID.create("FakeClass", "float"));
        doReturn("1234567890.0123456789").when(mockArgumentIndex).single(FlagID.create("FakeClass", "double"));
        doReturn("9876543210").when(mockArgumentIndex).single(FlagID.create("FakeClass", "bigint"));
        doReturn("9876543210.0123456789").when(mockArgumentIndex).single(FlagID.create("FakeClass", "bigdecimal"));
    }

    @Test
    public void get_String() throws Exception {
        FlagID flagID = FlagID.create("FakeClass", "str");
        assertEquals("should return String arguments",
                "test.log", Flag.create(flagID, String.class, mockArgumentProvider, typeConversion).get());
        // Do we need to test this interaction?
        verify(mockArgumentIndex).single(flagID);
    }

    @Test
    public void get_int() throws Exception {
        try {
            Flag flag = Flag.create(FlagID.create("FakeClass", "int"), Integer.class, mockArgumentProvider,
                    typeConversion);
            assertEquals("should return int arguments", 123, flag.get());
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }

    @Test
    public void get_int_NFE() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "str"), Integer.class, mockArgumentProvider, typeConversion).get();
            fail("should throw NFE for malformed int arguments");
        } catch(NumberFormatException ex) {
            // pass
        }
    }

    @Test
    public void get_float() throws Exception {
        try {
            Flag flag = Flag.create(FlagID.create("FakeClass", "float"), Float.class, mockArgumentProvider,
                    typeConversion);
            assertEquals("should return int arguments", 12345.6789f, flag.get());
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }

    @Test
    public void get_float_NFE() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "str"), Float.class, mockArgumentProvider,
                    typeConversion).get();
            fail("should throw NFE for malformed int arguments");
        } catch(NumberFormatException ex) {
            // pass
        }
    }

    @Test
    public void get_double() throws Exception {
        try {
            Flag flag = Flag.create(FlagID.create("FakeClass", "double"), Double.class, mockArgumentProvider,
                    typeConversion);
            assertEquals("should return int arguments", 1234567890.0123456789d, flag.get());
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }

    @Test
    public void get_double_NFE() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "str"), Double.class, mockArgumentProvider,
                    typeConversion).get();
            fail("should throw NFE for malformed int arguments");
        } catch(NumberFormatException ex) {
            // pass
        }
    }

    @Test
    public void get_bigint() throws Exception {
        try {
            Flag flag = Flag.create(FlagID.create("FakeClass", "bigint"), BigInteger.class, mockArgumentProvider,
                    typeConversion);
            assertEquals("should return int arguments", new BigInteger("9876543210"), flag.get());
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }

    @Test
    public void get_bigint_NFE() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "str"), BigInteger.class, mockArgumentProvider,
                    typeConversion).get();
            fail("should throw NFE for malformed int arguments");
        } catch(NumberFormatException ex) {
            // pass
        }
    }

    @Test
    public void get_bigdecimal() throws Exception {
        try {
            Flag flag = Flag.create(FlagID.create("FakeClass", "bigdecimal"), BigDecimal.class, mockArgumentProvider,
                    typeConversion);
            assertEquals("should return int arguments", new BigDecimal("9876543210.0123456789"), flag.get());
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }

    @Test
    public void get_bigdecimal_NFE() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "str"), BigDecimal.class, mockArgumentProvider,
                    typeConversion).get();
            fail("should throw NFE for malformed int arguments");
        } catch(NumberFormatException ex) {
            // pass
        }
    }

    @Test
    public void get_unsupported() throws Exception {
        try {
            Flag.create(FlagID.create("FakeClass", "input"), UNSUPPORTED_TYPE, mockArgumentProvider,
                    typeConversion).get();
            fail("should throw UOE when type conversion for target type is missing");
        } catch(UnsupportedOperationException ex) {
            // pass
        }
    }
}