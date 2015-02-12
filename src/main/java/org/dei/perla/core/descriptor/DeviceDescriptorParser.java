package org.dei.perla.core.descriptor;

import java.io.InputStream;

/**
 * An interface for parsing a <code>DeviceDescriptor</code> from a stream of
 * bytes.
 *
 * This interface allows the PerLa framework to be independent from a particular
 * <code>DeviceDescriptor</code> file format.
 *
 * @author Guido Rota (2014)
 *
 */
public interface DeviceDescriptorParser {

	/**
	 * Parses a stream of bytes into a <code>DeviceDescriptor</code> object.
	 *
	 * @param is
	 *            Stream of bytes from which to parse the device descriptor
	 * @return <code>DeviceDescriptor</code> object
	 * @throws DeviceDescriptorParseException
	 *             when the input data cannot be parsed into a correct device
	 *             descriptor.
	 */
	public DeviceDescriptor parse(InputStream is)
			throws DeviceDescriptorParseException;

}
