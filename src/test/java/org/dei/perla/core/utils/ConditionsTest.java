package org.dei.perla.core.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ConditionsTest {

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
				Logger.getLogger(ConditionsTest.class));
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
		Logger logger = Logger.getLogger(ConditionsTest.class);
		logger.setLevel(Level.OFF);
		Check.argument(false, "test", logger);
	}

}
