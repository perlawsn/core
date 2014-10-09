package org.dei.perla.channel.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.dei.perla.channel.IORequestBuilder;
import org.dei.perla.channel.IORequestBuilderFactory;
import org.dei.perla.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.dei.perla.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.utils.Check;
import org.dei.perla.utils.Errors;

public class HttpIORequestBuilderFactory implements IORequestBuilderFactory {

	private final Logger logger = Logger
			.getLogger(HttpIORequestBuilderFactory.class);

	@Override
	public Class<? extends IORequestDescriptor> acceptedIORequestClass() {
		return HttpIORequestDescriptor.class;
	}

	@Override
	public IORequestBuilder create(IORequestDescriptor descriptor)
			throws InvalidDeviceDescriptorException {
		Errors err = new Errors("Http request descriptor '%s'",
				descriptor.getId());
		HttpIORequestDescriptor httpDesc;
		String requestId;
		HttpIORequestBuilder httpRequestBuilder;

		// Check descriptor class
		if (!(descriptor instanceof HttpIORequestDescriptor)) {
			String message = String.format("Expected "
					+ HttpIORequestDescriptor.class.getCanonicalName()
					+ " but received "
					+ descriptor.getClass().getCanonicalName() + ".");
			logger.error(message);
			throw new InvalidDeviceDescriptorException(message);
		}

		httpDesc = (HttpIORequestDescriptor) descriptor;

		URL url = getUrl(httpDesc, err);
		ContentType contentType = getContentType(httpDesc, err);

		if (!err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}

		requestId = httpDesc.getId();

		httpRequestBuilder = new HttpIORequestBuilder(url, requestId,
				httpDesc.getMethod(), contentType);

		return httpRequestBuilder;
	}

	private URL getUrl(HttpIORequestDescriptor desc, Errors err) {
		URL url = null;

		if (Check.nullOrEmpty(desc.getHost())) {
			err.addError("Missing or empty 'host' attribute");
			return null;
		}

		// Validate host http
		try {

			url = new URL(desc.getHost());
			String protocol = url.getProtocol();
			// Check host uses HTTP protocol
			if (protocol == null || !"http".equals(protocol.toLowerCase())) {
				err.addError("'host' must use HTTP protocol. Use absolute URL.");
			}

		} catch (MalformedURLException e) {
			err.addError("syntax error in 'host' attribute. Insert absolute URL of service request.");
		}

		return url;
	}

	private ContentType getContentType(HttpIORequestDescriptor desc, Errors err) {
		if ((desc.getMethod() == HttpMethod.GET || desc.getMethod() == HttpMethod.DELETE)
				&& !Check.nullOrEmpty(desc.getContentType())) {
			err.addError("Invalid 'content-type' attribute for GET and DELETE requests");
			return null;
		}

		if (desc.getMethod() == HttpMethod.GET
				|| desc.getMethod() == HttpMethod.DELETE) {
			return null;
		}

		// Check content-type
		if (Check.nullOrEmpty(desc.getContentType())) {
			return ContentType.WILDCARD;
		} else {
			return ContentType.parse(desc.getContentType());
		}
	}
}
