package org.dei.perla.core.channel.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.dei.perla.core.fpc.descriptor.IORequestDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request")
public class HttpIORequestDescriptor extends IORequestDescriptor {

	@XmlAttribute(required = true)
	private String host;
	@XmlAttribute
	private HttpMethod method = HttpMethod.GET;
	@XmlAttribute(name = "content-type")
	private String contentType;

	/**
	 * It is mandatory.
	 *
	 * @return The first part of URL Http request.<br />
	 *         ex. http://www.yourhost.com
	 */
	public String getHost() {
		return host;
	}

	public HttpMethod getMethod() {
		return method;
	}

	@XmlEnum
	public enum HttpMethod {
		@XmlEnumValue("get")
		GET,

		@XmlEnumValue("post")
		POST,

		@XmlEnumValue("put")
		PUT,

		@XmlEnumValue("delete")
		DELETE
	}

	public String getContentType() {
		return contentType;
	}

}
