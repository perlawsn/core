package org.dei.perla.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CheckTest {

	@Test
	public void stringNullOrEmptyTest() {
		String nullString = null;
		String emptyString = "";
		String string = "string";
		
		assertThat(nullString, nullValue());
		assertThat(emptyString, notNullValue());
		assertTrue(emptyString.isEmpty());
		assertThat(string, notNullValue());
		assertFalse(string.isEmpty());
		
		assertTrue(Check.nullOrEmpty(nullString));
		assertTrue(Check.nullOrEmpty(emptyString));
		assertFalse(Check.nullOrEmpty(string));
	}
	
	@Test
	public void listNullOrEmptyTest() {
		List<?> nullList = null;
		List<?> emptyList = new ArrayList<>();
		List<Object> list = new ArrayList<>();
		list.add(new Object());
		list.add(new Object());
		
		assertThat(nullList, nullValue());
		assertThat(emptyList, notNullValue());
		assertThat(list, notNullValue());
		assertFalse(list.isEmpty());
		
		assertTrue(Check.nullOrEmpty(nullList));
		assertTrue(Check.nullOrEmpty(emptyList));
		assertFalse(Check.nullOrEmpty(list));
	}
	
}
