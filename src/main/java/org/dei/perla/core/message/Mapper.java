package org.dei.perla.core.message;

import java.util.Collection;

import org.apache.http.annotation.ThreadSafe;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.fpc.descriptor.FieldDescriptor;

/**
 * <p>
 * A class for marshalling and unmarshalling data. Each <code>Mapper</code>
 * object is tailored to managea single message type.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
@ThreadSafe
public interface Mapper {

	/**
	 * Identifier of the message managed by this <code>Mapper<code>
	 *
	 * @return Message identifier
	 */
	public String getMessageId();

	/**
	 * Returns the {@link FieldDescriptor} of the field passed as parameter
	 *
	 * @param name
	 *            Field name
	 * @return Descriptor of the field
	 */
	public FieldDescriptor getFieldDescriptor(String name);

	/**
	 * Returns the {@code FieldDescriptor}s of all fields of the message managed
	 * by this {@code Mapper}
	 *
	 * @return Descriptors of all fields in the message managed by this
	 *         {@code Mapper}
	 */
	public Collection<FieldDescriptor> getFieldDescriptors();

	/**
	 * Creates a new empty {@link FpcMessage}.
	 *
	 * FpcMessages created using this method are usually marshalled and then
	 * sent to the Channel
	 *
	 * @return A new FpcMessage instance
	 */
	public FpcMessage createMessage();

	/**
	 * Unmarshal data contained in the <code>Payload</code> passed as parameter
	 * into a <code>FpcMessage</code>
	 *
	 * @param payload
	 *            <code>Payload</code> to unmarshal
	 * @return <codee>FpcMessage</code> instance populated with data
	 *         unmarshalled from the payload
	 */
	public FpcMessage unmarshal(Payload payload);

	/**
	 * Marshal the information contained inside the <code>FpcMessage</code> into
	 * a <code>Payload</code>
	 *
	 * @param message
	 *            <code>FpcMessage</code> to marshal
	 * @return <code>Payload</code> containing the data marshalled from the
	 *         <code>FpcMessage</code>
	 */
	public Payload marshal(FpcMessage message);

}
