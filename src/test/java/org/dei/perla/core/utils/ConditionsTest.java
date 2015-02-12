package org.dei.perla.core.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

public class ConditionsTest {

	@Test
	public void checkNotNullSuccessTest() {
		Object object = new Object();

		assertThat(object, notNullValue());
		Object returned = Conditions.checkNotNull(object);
		assertThat(returned, equalTo(object));
	}

	@Test(expected = NullPointerException.class)
	public void checkNotNullFailureTest() {
		Conditions.checkNotNull(null);
	}

	@Test
	public void checkIllegalArgumentSuccess() {
		Conditions.checkIllegalArgument(true);
		Conditions.checkIllegalArgument(true, "test");
		Conditions.checkIllegalArgument(true, "test",
				Logger.getLogger(ConditionsTest.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure1() {
		Conditions.checkIllegalArgument(false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure2() {
		Conditions.checkIllegalArgument(false, "test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkIllegalArgumentFailure3() {
		Logger logger = Logger.getLogger(ConditionsTest.class);
		logger.setLevel(Level.OFF);
		Conditions.checkIllegalArgument(false, "test", logger);
	}

}
