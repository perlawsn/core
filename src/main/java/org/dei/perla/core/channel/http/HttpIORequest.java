package org.dei.perla.core.channel.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.dei.perla.core.utils.Check;

public class HttpIORequest implements IORequest {

	private final Logger logger = Logger.getLogger(HttpIORequest.class);

	private final String id;
	private final HttpMethod method;
	private final URL descriptorUrl;
	private final ContentType contentType;
	private URI uri;
	private Payload query = null;
	private Payload entity = null;

	private Payload path;

	protected HttpIORequest(String id, HttpMethod requestMethod,
			URL descriptorUrl, ContentType contentType) {
		this.id = id;
		this.method = requestMethod;
		this.descriptorUrl = descriptorUrl;
		this.contentType = contentType;
		try {
			this.uri = this.descriptorUrl.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getId() {
		return id;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public URI getUri() {
		return uri;
	}

	public Payload getQuery() {
		return query;
	}

	public Payload getEntity() {
		return entity;
	}

	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public void setParameter(String name, Payload payload) {
		switch (name) {
		case NamedParameterKey.QUERY:
			setQuery(payload);
			break;
		case NamedParameterKey.ENTITY:
			entity = payload;
			break;
		case NamedParameterKey.PATH:
			setPath(payload);
			break;
		}
	}

	private void setPath(Payload payload) {
		try {

			boolean hasDefaultQuery = !Check.nullOrEmpty(descriptorUrl
					.getQuery());
			StringBuilder sb = new StringBuilder(descriptorUrl.toString()
					.split("\\?")[0]);

			// Dynamic path
			sb.append(payload.asString());

			// Query params
			if (hasDefaultQuery)
				sb.append("?" + descriptorUrl.getQuery());

			if (query != null)
				if (hasDefaultQuery)
					sb.append("&" + query.asString());
				else
					sb.append("?" + query.asString());

			uri = new URL(sb.toString()).toURI();
			path = payload;

			logger.debug(String.format(
					"New uri setted for http request '%s': %s", id,
					uri.toString()));
		} catch (MalformedURLException | URISyntaxException e) {
			String message = "Cannot create URI for HTTPRequest '" + id
					+ "'. Error adding 'path' payload.";
			logger.error(message, e);
		}
	}

	private void setQuery(Payload payload) {
		try {

			StringBuilder sb = new StringBuilder(descriptorUrl.toString()
					.split("\\?")[0]);

			// Dynamic path
			if (path != null)
				sb.append(path.asString());

			// Query params
			if (!Check.nullOrEmpty(descriptorUrl.getQuery())) {
				sb.append("?" + descriptorUrl.getQuery());
				sb.append("&" + payload.asString());
			} else {
				sb.append("?" + payload.asString());
			}

			uri = new URL(sb.toString()).toURI();
			query = payload;

			logger.debug(String.format(
					"New uri setted for http request '%s': %s", id,
					uri.toString()));
		} catch (MalformedURLException | URISyntaxException e) {
			String message = "Cannot create URI for HTTPRequest '" + id
					+ "'. Error adding 'query' payload.";
			logger.error(message, e);
		}
	}

	public class NamedParameterKey {
		public final static String QUERY = "query";
		public final static String ENTITY = "entity";
		public final static String PATH = "path";

	}

	public Payload getPath() {
		return path;
	}

}
