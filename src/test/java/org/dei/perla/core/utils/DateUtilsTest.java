package org.dei.perla.core.utils;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DateUtilsTest {

	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm");

	@Test
	public void testParse() {
		String value = "3 Jun 2014 14:35";

		Instant d = DateUtils.parse(fmt, value);
        assertThat(d, notNullValue());
        ZonedDateTime zd = d.atZone(ZoneId.systemDefault());
		assertThat(zd.get(ChronoField.YEAR), equalTo(2014));
		assertThat(zd.get(ChronoField.MONTH_OF_YEAR), equalTo(6));
		assertThat(zd.get(ChronoField.DAY_OF_MONTH), equalTo(3));
		assertThat(zd.get(ChronoField.HOUR_OF_DAY), equalTo(14));
		assertThat(zd.get(ChronoField.MINUTE_OF_HOUR), equalTo(35));
		assertThat(zd.get(ChronoField.SECOND_OF_MINUTE), equalTo(0));
		assertThat(zd.get(ChronoField.NANO_OF_SECOND), equalTo(0));
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
