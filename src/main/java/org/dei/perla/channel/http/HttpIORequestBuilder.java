package org.dei.perla.channel.http;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.entity.ContentType;
import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.IORequestBuilder;
import org.dei.perla.channel.http.HttpIORequest.NamedParameterKey;
import org.dei.perla.channel.http.HttpIORequestDescriptor.HttpMethod;

public class HttpIORequestBuilder implements IORequestBuilder {

	private static final List<IORequestParameter> paramList;

	static {
		IORequestParameter[] paramArray = new IORequestParameter[3];
		paramArray[0] = new IORequestParameter(NamedParameterKey.QUERY, false);
		paramArray[1] = new IORequestParameter(NamedParameterKey.PATH, false);
		paramArray[2] = new IORequestParameter(NamedParameterKey.ENTITY, false);
		paramList = Collections.unmodifiableList(Arrays.asList(paramArray));
	}

	private final URL url;
	private final String requestId;
	private final HttpMethod httpRequestMethod;
	private final ContentType contentType;

	protected HttpIORequestBuilder(URL url, String requestId,
			HttpMethod httpRequestMethod, ContentType contentType) {
		this.url = url;
		this.requestId = requestId;
		this.httpRequestMethod = httpRequestMethod;
		this.contentType = contentType;
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	@Override
	public IORequest create() {
		HttpIORequest httpChReq = new HttpIORequest(requestId,
				httpRequestMethod, url, contentType);
		return httpChReq;
	}

	@Override
	public List<IORequestParameter> getParameterList() {
		return paramList;
	}

}
