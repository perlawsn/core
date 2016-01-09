package org.dei.perla.core.message;

/**
 * <p>
 * {@link FpcMessage} is a generic interface that specifies a common set of
 * methods for interacting with messages unmarshalled by a {@link Mapper}.
 *
 * <p>
 * This interface allows every {@link org.dei.perla.core.fpc.Fpc} to set and
 * retrieve fields from a message, regardless of the underlying message
 * implementation details.
 *
 * @author Guido Rota (2014)
 */
public interface FpcMessage {

	/**
	 * Returns the identifier of this message
	 *
	 * @return Message identifier
	 */
	public String getId();

	/**
	 * Returns true if the current {@link FpcMessage} contains the field
	 * passed as parameter.
	 *
	 * @param name
	 *            Field name
	 * @return true if the message contains the a field with the specified
	 *         identifier
	 */
	public boolean hasField(String name);

	/**
	 * Retrieves a field from the message.
	 *
	 * @param name
	 *            Name of the field to retrieve
	 * @return Value of the field
	 * @throws IllegalArgumentException
	 *             If the identifier passed as a parameter does not correspond
	 *             to any field in the message
	 */
	public Object getField(String name) throws IllegalArgumentException;

	/**
	 * Sets the value of a message field.
	 *
	 * @param name
	 *            Name of the field to set
	 * @param value
	 *            Value to set in the field
	 * @throws IllegalArgumentException
	 *             If the identifier passed as a parameter does not correspond
	 *             to any field in the message
	 */
	public void setField(String name, Object value)
			throws IllegalArgumentException;

	/**
	 * Appends a new element to a list field
	 *
	 * @param name
	 *            Name of the list field
	 * @param element
	 *            Element to append
	 * @throws IllegalArgumentException
	 *             If the field specified as parameter does not exist
	 */
	public void appendElement(String name, Object element)
			throws IllegalArgumentException;

	/**
	 * Checks if all STATIC message fields are correctly set to the value
	 * indicated inside the Device descriptor
	 *
	 * This method can be employed to verify if the information successfully
	 * unmarshalled by the <code>Mapper</code> conform to the message format
	 * specified in the Device descriptor. If is particularly useful when the
	 * end devices exposes different messages that share the same structure, but
	 * are used for different purposes.
	 *
	 * This method always returns true if no STATIC message fields are indicated
	 * in the Device Descriptor.
	 *
	 * @return true if the message fields set as STATIC correspond to the value
	 *         indicated inside the Device descriptor, false otherwise
	 */
	public boolean validate();

}
