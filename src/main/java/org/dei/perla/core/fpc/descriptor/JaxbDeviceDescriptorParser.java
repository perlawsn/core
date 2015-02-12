package org.dei.perla.core.fpc.descriptor;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * Concrete <code>DeviceDescriptorParser</code> class for parsing device
 * descriptors XML format. This particular implementation uses the
 * <code>JAXB</code> library for binding XML attributes and elements into the
 * device descriptor classess.
 *
 * @author Guido Rota (2014)
 *
 */
public class JaxbDeviceDescriptorParser implements DeviceDescriptorParser {

	private final Logger logger = Logger
			.getLogger(JaxbDeviceDescriptorParser.class);
	private final JAXBContext context;

	public JaxbDeviceDescriptorParser(List<String> packageList) {
		StringBuilder contextPath = new StringBuilder();

		Iterator<String> iterator = packageList.iterator();
		while (iterator.hasNext()) {
			contextPath.append(iterator.next());
			if (iterator.hasNext()) {
				contextPath.append(":");
			}
		}

		try {
			context = JAXBContext.newInstance(contextPath.toString());
		} catch (JAXBException e) {
			String message = "Error while creating JaxbDeviceDescriptorParser instance: "
					+ e.getMessage();
			logger.error(message);
			throw new IllegalArgumentException(message, e);
		}
	}

	@Override
	public DeviceDescriptor parse(InputStream is)
			throws DeviceDescriptorParseException {
		try {
			Unmarshaller unmarshaller = context.createUnmarshaller();
			DeviceDescriptor descriptor = unmarshaller.unmarshal(
					new StreamSource(is), DeviceDescriptor.class).getValue();
			return descriptor;
		} catch (JAXBException e) {
			String message = "Error while parsing device descriptor: "
					+ e.getMessage();
			logger.error(message);
			throw new DeviceDescriptorParseException(message, e);
		}
	}

}
