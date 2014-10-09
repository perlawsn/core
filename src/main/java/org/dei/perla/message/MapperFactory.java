package org.dei.perla.message;

import java.util.Map;

import javassist.ClassPool;

import org.apache.http.annotation.ThreadSafe;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.fpc.descriptor.MessageDescriptor;

/**
 * A factory for creating new Mapper classes. Each MapperFactory implementation
 * can create Mappers for a single message type. The accepted MessageDescriptor
 * instance can be retrieved using the provided acceptedMessageDescriptorClass
 * method.
 * 
 * @author Guido Rota (2014)
 * 
 */
@ThreadSafe
public interface MapperFactory {

	/**
	 * Returns the <code>MessageDescriptor</code> class that this
	 * <code>MapperFactory</code> instance can parse to create a new
	 * <code>Mapper</code>
	 * 
	 * @return Message class
	 */
	public Class<? extends MessageDescriptor> acceptedMessageDescriptorClass();

	/**
	 * <p>
	 * Creates a new <code>Mapper</code> instance.
	 * </p>
	 * 
	 * <p>
	 * Each <code>Mapper</code> returned by this method is tailored to marshal
	 * and unmarshal a single message type, whose characteristics are detailed
	 * inside the <Code>MessageDescriptor</code> object.
	 * </p>
	 * 
	 * <p>
	 * The content of the <code>MessageDescriptor</code> is partially validated
	 * by <code>FpcFactory</code>. Consult the <code>FpcFactory</code> javadoc
	 * to check which checks are performed on the <code>MessageDescriptor</code>
	 * so to avoid replicating them.
	 * </p>
	 * 
	 * 
	 * @param deviceName
	 *            Name of the device that will use the <code>Mapper</code>
	 * @param descriptor
	 *            Java description of the message that the <code>Mapper</code>
	 *            will have to marshal and unmarshal
	 * @param mapperMap
	 *            Collection of <code>Mapper</code>s, indexed by message
	 *            identifier. It can be used if the descriptor being parsed
	 *            contains references to other messages.
	 * @param classPool
	 *            Shared <code>ClassPool</code> that can be used to dynamically
	 *            create new Java classes. It is guaranteed that all
	 *            <code>MessageFactory.createMapper</code> invocations will
	 *            share the same <code>ClassPool</code> object.
	 * @return <code>Mapper</code> for marshalling and unmarshalling messages
	 *         that follow the message format indicated in the MessageDescriptor
	 *         argument
	 * @throws InvalidDeviceDescriptorException
	 */
	public Mapper createMapper(MessageDescriptor descriptor,
			Map<String, Mapper> mapperMap, ClassPool classPool)
			throws InvalidDeviceDescriptorException;

}
