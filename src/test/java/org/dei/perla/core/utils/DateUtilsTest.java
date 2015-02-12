package org.dei.perla.core.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import org.junit.Test;

public class DateUtilsTest {

	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm");

	@Test
	public void testParse() {
		String value = "3 Jun 2014 14:35";

		ZonedDateTime d = DateUtils.parse(fmt, value);
		assertThat(d, notNullValue());
		assertThat(d.get(ChronoField.YEAR), equalTo(2014));
		assertThat(d.get(ChronoField.MONTH_OF_YEAR), equalTo(6));
		assertThat(d.get(ChronoField.DAY_OF_MONTH), equalTo(3));
		assertThat(d.get(ChronoField.HOUR_OF_DAY), equalTo(14));
		assertThat(d.get(ChronoField.MINUTE_OF_HOUR), equalTo(35));
		assertThat(d.get(ChronoField.SECOND_OF_MINUTE), equalTo(0));
		assertThat(d.get(ChronoField.NANO_OF_SECOND), equalTo(0));
		assertThat(d.getZone(), equalTo(ZoneId.systemDefault()));
	}

	@Test
	public void testFormat() {
		ZonedDateTime now = ZonedDateTime.now();
		String value = DateUtils.format(fmt, now);
		String control = now.format(fmt);

		assertThat(value, notNullValue());
		assertThat(control, notNullValue());
		assertThat(value, equalTo(control));
	}

}
