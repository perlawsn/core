package org.dei.perla.channel.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dei.perla.channel.AbstractChannel;
import org.dei.perla.channel.ByteArrayPayload;
import org.dei.perla.channel.ChannelException;
import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;

public class HttpChannel extends AbstractChannel {

	private Logger logger = Logger.getLogger(HttpChannel.class);
	private CloseableHttpClient client;

	protected HttpChannel(String id) {
		super(id);
		logger.debug("Creating HTTPClient");
		client = HttpClients.createDefault();
	}

	public Payload handleRequest(IORequest request) throws ChannelException,
			InterruptedException {
		HttpIORequest httpRequest;

		if (!(request instanceof HttpIORequest)) {
			String message = "Channel request error: Cannot cast from "
					+ request.getClass().getSimpleName() + " to "
					+ HttpIORequest.class.getSimpleName();
			logger.error(message);
			throw new ClassCastException(message);
		}

		httpRequest = (HttpIORequest) request;

		switch (httpRequest.getMethod()) {
		case GET:
			return handleGetRequest(httpRequest);
		case POST:
			return handlePostRequest(httpRequest);
		case PUT:
			return handlePutRequest(httpRequest);
		case DELETE:
			return handleDeleteRequest(httpRequest);
		default:
			throw new RuntimeException("Unrecognized HTTP method "
					+ httpRequest.getMethod());
		}
	}

	private Payload handleGetRequest(HttpIORequest request)
			throws ChannelException {
		HttpGet getRequest;

		logger.debug("GET " + request.getUri());

		getRequest = new HttpGet(request.getUri());
		return executeHttpRequest(request, getRequest);
	}

	private Payload handlePostRequest(HttpIORequest request)
			throws ChannelException {
		HttpPost postRequest;
		logger.debug("POST " + request.getUri());

		postRequest = new HttpPost(request.getUri());
		Payload entity = request.getEntity();
		if (entity != null)
			postRequest.setEntity(new StringEntity(entity.asString(), request
					.getContentType()));

		return executeHttpRequest(request, postRequest);
	}

	private Payload handlePutRequest(HttpIORequest request) {
		HttpPut putRequest;
		logger.debug("PUT " + request.getUri());

		putRequest = new HttpPut(request.getUri());
		Payload entity = request.getEntity();
		if (entity != null)
			putRequest.setEntity(new StringEntity(entity.asString(), request
					.getContentType()));

		return executeHttpRequest(request, putRequest);
	}

	private Payload handleDeleteRequest(HttpIORequest request) {
		HttpDelete deleteRequest;
		logger.debug("DELETE " + request.getUri());

		deleteRequest = new HttpDelete(request.getUri());
		return executeHttpRequest(request, deleteRequest);
	}

	private Payload executeHttpRequest(HttpIORequest request,
			HttpUriRequest uriRequest) throws ChannelException {
		HttpEntity entity;
		Payload payload = null;

		try {

			CloseableHttpResponse response = client.execute(uriRequest);

			entity = response.getEntity();
			if (entity != null) {
				payload = new ByteArrayPayload(EntityUtils.toByteArray(response
						.getEntity()));
				response.close();
			}

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode < 200 || statusCode > 299) {
				throw new ChannelException("Invalid status code '" + statusCode
						+ "' received while processing HTTP request '"
						+ request.getId());
			}

			return payload;

		} catch (IOException | ChannelException e) {
			throw new ChannelException(e);
		}
	}

	public void close() {
		super.close();
		try {
			client.close();
		} catch (IOException exception) {
			logger.error("Error closing HTTPClient: " + exception.getMessage());
		}
	}

}
