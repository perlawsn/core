package org.dei.perla.core.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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

	@Test
	public void checkNotNullSuccessTest() {
		Object object = new Object();

		assertThat(object, notNullValue());
		Object returned = Check.notNull(object);
		assertThat(returned, equalTo(object));
	}

	@Test(expected = NullPointerException.class)
	public void checkNotNullFailureTest() {
		Check.notNull(null);
	}

	@Test
	public void checkIllegalArgumentSuccess() {
		Check.argument(true);
		Check.argument(true, "test");
		Check.argument(true, "test",
				Logger.getLogger(CheckTest.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure1() {
		Check.argument(false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure2() {
		Check.argument(false, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure3() {
		Logger logger = Logger.getLogger(CheckTest.class);
		logger.setLevel(Level.OFF);
		Check.argument(false, "test", logger);
	}

}
