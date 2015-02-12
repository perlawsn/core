package org.dei.perla.core.utils;

import java.util.Collection;

/**
 * Static utility methods for checking and performing common operations on
 * strings and various collections.
 *
 *
 * @author Guido Rota (2014)
 *
 */
public final class Check {

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
