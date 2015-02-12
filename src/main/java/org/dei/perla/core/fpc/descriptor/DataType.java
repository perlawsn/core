package org.dei.perla.core.fpc.descriptor;

import java.time.ZonedDateTime;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.dei.perla.core.message.FpcMessage;

@XmlEnum
public enum DataType {

	@XmlEnumValue("float")
	FLOAT("float"),

	@XmlEnumValue("integer")
	INTEGER("integer"),

	@XmlEnumValue("boolean")
	BOOLEAN("boolean"),

	@XmlEnumValue("string")
	STRING("string"),

	@XmlEnumValue("id")
	ID("id"),

	@XmlEnumValue("timestamp")
	TIMESTAMP("timestamp");

	// String identifier
	private final String id;

	private DataType(String id) {
		this.id = id;
	}

	/**
	 * Returns the String identifier of the enum value
	 *
	 * @return String identifier of the enum value
	 */
	public String getId() {
		return id;
	}

	/**
	 * Indicates if the enum identifier corresponds to its string representation
	 *
	 * @param type
	 *            String type identifier
	 * @return true if the the string corresponds to the enum identifier
	 */
	public boolean is(String type) {
		return type.toLowerCase().equals(this.id);
	}

	/**
	 * <p>
	 * Indicates if the String passed as parameter correspond to a primitive
	 * type (integer, float, boolean, string, id and timestamp).
	 * </p>
	 *
	 * <p>
	 * Non primitive types are lists and other complex types (e.g., other user
	 * declared messages).
	 * </p>
	 *
	 * @param type
	 *            Data type identifier
	 * @return true if the type is primitive, false otherwise
	 */
	public static boolean isPrimitive(String type) {
		type = type.toLowerCase();
		if (type.equals(INTEGER.id) || type.equals(FLOAT.id)
				|| type.equals(BOOLEAN.id) || type.equals(STRING.id)
				|| type.equals(ID.id) || type.equals(TIMESTAMP.id)) {
			return true;
		}
		return false;
	}

	/**
	 * Indicates if the String passed as parameter correspond to a complex
	 * message type (everything that is not primitive or list).
	 *
	 * @param type
	 *            Data type identifier
	 * @return true if the type is a message, false otherwise
	 */
	public static boolean isComplex(String type) {
		return !isPrimitive(type);
	}

	/**
	 * Returns the Java class most akin to the PerLa {@code DataType}.
	 *
	 * @return Java class corresponding to the PerLa attribute type passed as
	 *         parameter
	 */
	public Class<?> getJavaClass() {
		return DataType.getClass(this);
	}

	/**
	 * <p>
	 * Returns the Java class most akin to the PerLa attribute type passed as
	 * parameter. If the type identifier passed as parameter is a value of the
	 * DataType enum, this function assumes it is an FpcMessage.
	 * </p>
	 *
	 * @param type
	 *            PerLa attribute type identifier
	 * @return Java class corresponding to the PerLa attribute type passed as
	 *         parameter
	 */
	public static Class<?> getJavaClass(String type) {
		switch (type.toLowerCase()) {
		case "integer":
			return Integer.class;
		case "float":
			return Float.class;
		case "boolean":
			return Boolean.class;
		case "string":
			return String.class;
		case "id":
			return Integer.class;
		case "timestamp":
			return ZonedDateTime.class;
		default:
			return FpcMessage.class;
		}
	}

	/**
	 * Returns the Java class most akin to the PerLa attribute type passed as
	 * parameter
	 *
	 * @param type
	 *            PerLa attribute type
	 * @return Java class corresponding to the PerLa attribute type passed as
	 *         parameter
	 */
	public static Class<?> getClass(DataType type)
			throws IllegalArgumentException {
		switch (type) {
		case INTEGER:
			return Integer.class;
		case FLOAT:
			return Float.class;
		case BOOLEAN:
			return Boolean.class;
		case STRING:
			return String.class;
		case ID:
			return Integer.class;
		case TIMESTAMP:
			return ZonedDateTime.class;
		default:
			throw new IllegalArgumentException("Unexpected PerLa type " + type);
		}
	}

	/**
	 * Parse a String value into the PerLa {@link DataType} corresponding to the
	 * identifier passed as parameter
	 *
	 * @param type
	 *            Output PerLa {@link DataType} identifier
	 * @param value
	 *            String value to convert
	 * @return Input value converted to the desired PerLa {@link DataType}
	 * @throws IllegalArgumentException
	 *             If the value cannot be converted to the desired
	 *             {@link DataType}
	 */
	public static Object parse(String type, String value)
			throws IllegalArgumentException {
		switch (type.toLowerCase()) {
		case "integer":
			return Integer.valueOf(value);
		case "float":
			return Float.valueOf(value);
		case "boolean":
			return Boolean.valueOf(value);
		case "string":
			return value;
		case "id":
			return Integer.valueOf(value);
		case "timestamp":
			throw new IllegalArgumentException(
					"Cannot parse TIMESTAMP values from string. Use DateUtils instead.");
		default:
			throw new IllegalArgumentException("Unexpected PerLa type " + type);
		}
	}

	/**
	 * Parse a String value into the PerLa {@link DataType} passed as parameter
	 *
	 * @param type
	 *            Output PerLa {@link DataType}
	 * @param value
	 *            String value to convert
	 * @return Input value converted to the desired PerLa {@link DataType}
	 * @throws IllegalArgumentException
	 *             If the value cannot be converted to the desired
	 *             {@link DataType}
	 */
	public static Object parse(DataType type, String value)
			throws IllegalArgumentException {
		switch (type) {
		case INTEGER:
			return Integer.valueOf(value);
		case FLOAT:
			return Float.valueOf(value);
		case BOOLEAN:
			return Boolean.valueOf(value);
		case STRING:
			return value;
		case ID:
			return Integer.valueOf(value);
		case TIMESTAMP:
			throw new IllegalArgumentException(
					"Cannot parse TIMESTAMP values from string. Use DateUtils instead.");
		default:
			throw new IllegalArgumentException("Unexpected PerLa type " + type);
		}
	}

}
