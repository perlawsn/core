package org.dei.perla.core.utils;

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Static utility methods for checking object status.
 *
 * @author Guido Rota (2014)
 */
public final class Check {

	private Check() {}

	/**
	 * Throws a {@link NullPointerException} if the object passed as
	 * parameter is null.
	 *
	 * @param object object to check
	 * @param <T>    type of the object to check
	 * @return Object passed as parameter
	 * @throws NullPointerException if the object is null
	 */
	public static <T> T notNull(T object) throws NullPointerException {
		if (object == null) {
			throw new NullPointerException();
		}

		return object;
	}

	/**
	 * Throws a {@link NullPointerException} with the specified message if
	 * the object passed as parameter is null.
	 *
	 * @param object  object to check
	 * @param message detail message thrown with the exception.
	 * @param <T>     type of the object to check
	 * @return object passed as parameter
	 * @throws NullPointerException if the object is null
	 */
	public static <T> T notNull(T object, String message)
			throws NullPointerException {
		if (object == null) {
			throw new NullPointerException(message);
		}

		return object;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the condition is not
	 * satisfied.
	 *
	 * @param condition argument condition
	 * @throws IllegalArgumentException if the condition is not satisfied
	 */
	public static void argument(boolean condition)
			throws IllegalArgumentException {
		argument(condition, null, null);
	}

	/**
	 * Throws an {@link IllegalArgumentException} with the specified message if
	 * the condition is not satisfied.
	 *
	 * @param condition argument condition
	 * @param message   error message to be thrown
	 * @throws IllegalArgumentException if the condition is not satisfied
	 */
	public static void argument(boolean condition, String message)
			throws IllegalArgumentException {
		argument(condition, message, null);
	}

	/**
	 * Throws an {@link IllegalArgumentException} with the specified message if
	 * the condition is not satisfied. The error message is logged using the
	 * {@link Logger} provided when the condition does not hold.
	 *
	 * @param condition Argument condition
	 * @param message   Error message to be thrown
	 * @param logger    Logger to be used for logging the error
	 * @throws IllegalArgumentException if the condition is not satisfied
	 */
	public static void argument(
			boolean condition,
			String message,
			Logger logger) throws IllegalArgumentException {
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
	/**
	 * Checks if the {@code String} passed as parameter is either null or empty.
	 *
	 * @param string
	 *            {@code String} to check.
	 * @return true if the {@code String} passed as parameter is either null or
	 *         empty, false otherwise.
	 */
	public static boolean nullOrEmpty(String string) {
		return string == null || string.isEmpty();
	}

	/**
	 * Checks if the {@code Collection} passed as parameter is either null or empty
	 *
	 * @param collection
	 *            {@code Collection} to check
	 * @return true if the {@code Collection} passed as parameter is either null or
	 *         empty, false otherwise.
	 */
	public static boolean nullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Checks if the {@code Array} passed as parameter is either null or empty
	 *
	 * @param collection
	 *            {@code Array} to check
	 * @return true if the {@code Array} passed as parameter is either null or
	 *         empty, false otherwise.
	 */
	public static <T> boolean nullOrEmpty(T[] array) {
		return array == null || array.length == 0;
	}

}
