package org.dei.perla.core.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public final class DateUtils {

	/**
	 * Parses a string into an {@link Instant}. Approximations may be introduced
	 * if the text passed as parameter does not represent an instant in time
	 * down to a nanosecond resolution.
	 *
	 * @param formatter
	 *            Formatter to be used for the parsing operation
	 * @param string
	 *            Source String
	 * @return {@link Instant} object parsed from the string passed as parameter
	 */
	public static Instant parse(DateTimeFormatter formatter, String string) {
		Instant now = null;
		int year = 0;
		int month = 0;
		int dayOfMonth = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		int nanoOfSecond = 0;
		ZoneId zone = null;

		TemporalAccessor a = formatter.parse(string);
		if (a.isSupported(ChronoField.YEAR)) {
			year = a.get(ChronoField.YEAR);
		} else {
			now = Instant.now();
            year = now.get(ChronoField.YEAR);
		}

		if (a.isSupported(ChronoField.MONTH_OF_YEAR)) {
			month = a.get(ChronoField.MONTH_OF_YEAR);
		} else {
			if (now == null) {
				now = Instant.now();
			}
            month = now.get(ChronoField.MONTH_OF_YEAR);
		}

		if (a.isSupported(ChronoField.DAY_OF_MONTH)) {
			dayOfMonth = a.get(ChronoField.DAY_OF_MONTH);
		} else {
			if (now == null) {
				now = Instant.now();
			}
            dayOfMonth = now.get(ChronoField.DAY_OF_MONTH);
		}

		if (a.isSupported(ChronoField.HOUR_OF_DAY)) {
			hour = a.get(ChronoField.HOUR_OF_DAY);
		} else {
			if (now == null) {
				now = Instant.now();
			}
            hour = now.get(ChronoField.HOUR_OF_DAY);
		}

		if (a.isSupported(ChronoField.MINUTE_OF_HOUR)) {
			minute = a.get(ChronoField.MINUTE_OF_HOUR);
		} else {
			if (now == null) {
				now = Instant.now();
			}
            minute = now.get(ChronoField.MINUTE_OF_HOUR);
		}

		if (a.isSupported(ChronoField.SECOND_OF_MINUTE)) {
			second = a.get(ChronoField.SECOND_OF_MINUTE);
		} else {
			second = 0;
		}

		if (a.isSupported(ChronoField.NANO_OF_SECOND)) {
			second = a.get(ChronoField.NANO_OF_SECOND);
		} else {
			nanoOfSecond = 0;
		}

		if (a.isSupported(ChronoField.OFFSET_SECONDS)) {
			ZoneId.ofOffset("UTC", ZoneOffset.from(a));
		} else {
			if (now == null) {
				now = Instant.now();
			}
			zone = ZoneId.systemDefault();
		}

		return ZonedDateTime.of(year, month, dayOfMonth, hour,
				minute, second, nanoOfSecond, zone).toInstant();
	}

	/**
	 * <p>
	 * Formats a {@link TemporalAccessor} object into a {@code String} using the
	 * specified {@link DateTimeFormatter}.
	 * </p>
	 *
	 * <p>
	 * This method was implemented for allowing
	 * {@link org.dei.perla.core.message.Mapper}s to format an unknown object
	 * of PerLa type TIMESTAMP.
	 * </p>
	 *
	 * @param formatter
	 *            {@link DateTimeFormatter} to be used to format the date object
	 * @param object
	 *            {@link Instant} to format
	 * @return String representation of the {@link Instant}
	 */
	public static String format(DateTimeFormatter formatter, Object object) {
		return format(formatter, (TemporalAccessor) object);
	}

	/**
	 * Formats a {@link TemporalAccessor} object into a {@code String} using the
	 * specified {@link DateTimeFormatter}.
	 *
	 * @param formatter
	 *            {@link DateTimeFormatter} to be used to format the date object
	 * @param instant
	 *            {@link Instant} to format
	 * @return String representation of the {@link Instant}
	 */
	public static String format(DateTimeFormatter formatter,
			TemporalAccessor instant) {
		return formatter.format(instant);
	}

}
