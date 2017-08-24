package com.github.yin.flags;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class GFlagsParserTest {
    @Mock
    FlagIndex<FlagMetadata> mockFlagIndex;
    
    @Mock
    GflagsParser mockGflagsParser;

	private MockForFlags parser;
    	
    @Before
    public void setup() {
    	parser = new MockForFlags(mockFlagIndex, mockGflagsParser);
    }

    @Test
    public void flag_key() throws Exception {
        parser.flag("myArg", "--myArg");
        ArgumentCaptor<String> c_key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> c_original = ArgumentCaptor.forClass(String.class);
        
        verify(mockGflagsParser).key(c_key.capture(), c_original.capture());
        verifyNoMoreInteractions(mockGflagsParser);
        verifyZeroInteractions(mockFlagIndex);
        
        assertEquals("myArg", c_key.getValue());
        assertEquals("--myArg", c_original.getValue());
    }
    
    @Test
    public void flag_keyValue_shortKey() throws Exception {
        parser.flag("key=value", "--key=value");
        ArgumentCaptor<String> c_key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> c_value = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> c_original = ArgumentCaptor.forClass(String.class);
        
        verify(mockGflagsParser).keyAndValue(c_key.capture(), c_value.capture(), c_original.capture());
        verifyNoMoreInteractions(mockGflagsParser);
        verifyZeroInteractions(mockFlagIndex);
    }
    
    @Test
    public void flag_keyValue_longKey() throws Exception {
        parser.flag("alongkey=value", "--alongkey=value");
        ArgumentCaptor<String> c_key = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> c_value = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> c_original = ArgumentCaptor.forClass(String.class);
        
        verify(mockGflagsParser).keyAndValue(c_key.capture(), c_value.capture(), c_original.capture());
        verifyNoMoreInteractions(mockGflagsParser);
        verifyZeroInteractions(mockFlagIndex);
    }
    
    static class MockForFlags extends GflagsParser {
		private GflagsParser mock;

		public MockForFlags(FlagIndex<FlagMetadata> flags, GflagsParser mock) {
			super(flags);
			this.mock = mock;
		}
    	
		@Override protected void key(String key, String original) {
			mock.key(key, original);
		}
    	
		@Override protected void keyAndValue(String key, String value, String original) {
			mock.keyAndValue(key, value, original);
		}
    }
}