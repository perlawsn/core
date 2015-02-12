package org.dei.perla.core.utils;

import org.apache.log4j.Logger;

/**
 * Static utility methods for performing common sanity checks on method
 * parameters.
 *
 *
 * @author Guido Rota (2014)
 *
 */
public final class Conditions {

	/**
	 * Throws a <code>NullPointerException</code> if the object passed as
	 * parameter is null.
	 *
	 * @param object
	 *            Object to check
	 * @return Object passed as parameter
	 */
	public static <T> T checkNotNull(T object) {
		if (object == null) {
			throw new NullPointerException();
		}

		return object;
	}

	/**
	 * Throws a <code>NullPointerException</code> with the specified message if
	 * the object passed as parameter is null.
	 *
	 * @param object
	 *            Object to check
	 * @param message
	 *            Detail message thrown with the exception.
	 * @return Object passed as parameter
	 */
	public static <T> T checkNotNull(T object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}

		return object;
	}

	/**
	 * Throws an IllegalArgumentException if the condition is not satisfied.
	 *
	 * @param condition
	 *            Argument condition
	 */
	public static void checkIllegalArgument(boolean condition) {
		checkIllegalArgument(condition, null, null);
	}

	/**
	 * Throws an IllegalArgumentException with the specified message if the
	 * condition is not satisfied.
	 *
	 * @param condition
	 *            Argument condition
	 * @param message
	 *            Error message to be thrown
	 */
	public static void checkIllegalArgument(boolean condition, String message) {
		checkIllegalArgument(condition, message, null);
	}

	/**
	 * Throws an IllegalArgumentException with the specified message if the
	 * condition is not satisfied. The error message is logged using the Logger
	 * provided when the condition does not hold.
	 *
	 * @param condition
	 *            Argument condition
	 * @param message
	 *            Error message to be thrown
	 * @param logger
	 *            Logger to be used for logging the error
	 */
	public static void checkIllegalArgument(boolean condition, String message,
			Logger logger) {
		if (condition) {
			return;
		}
		if (logger != null && message != null) {
			logger.error(message);
		}
		if (message != null) {
			throw new IllegalArgumentException(message);
		}
		throw new IllegalArgumentException();
	}

}
