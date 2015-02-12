package org.dei.perla.core.channel.http;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.entity.ContentType;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.channel.http.HttpIORequestDescriptor.HttpMethod;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpRequestTest {

	private static String ROOT_URL = "http://www.google.it%s";
	private static String ROOT_URL_Q = format(ROOT_URL, "?%s");
	private static String ROOT_URL_PQ = format(ROOT_URL, "%s?%s");

	private static URL rootUrl;
	private static URL rootUrlQ;
	private static URL rootUrlPQ;

	@BeforeClass
	public static void init() throws MalformedURLException {
		rootUrl = new URL(format(ROOT_URL, ""));
		rootUrlQ = new URL(format(ROOT_URL_Q, "q=PerLa"));
		rootUrlPQ = new URL(format(ROOT_URL_PQ, "/my/path", "q=PerLa"));
	}

	@Test
	public void formatUriToString() {
		HttpIORequest request = createRequest(rootUrl);
		assertThat(rootUrl.toString(), equalTo(request.getUri().toString()));

		request = createRequest(rootUrlQ);
		assertThat(rootUrlQ.toString(), equalTo(request.getUri().toString()));

		request = createRequest(rootUrlPQ);
		assertThat(rootUrlPQ.toString(), equalTo(request.getUri().toString()));
	}

	@Test
	public void formatUriToStringAddingQuery() {
		HttpIORequest request = createRequest(rootUrl);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		assertThat(format(ROOT_URL_Q, "q=PerLa"), equalTo(request.getUri()
				.toString()));

		request = createRequest(rootUrlQ);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		assertThat(format(ROOT_URL_Q, "q=PerLa&q=PerLa"), equalTo(request
				.getUri().toString()));

		request = createRequest(rootUrlPQ);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		assertThat(format(ROOT_URL_PQ, "/my/path", "q=PerLa&q=PerLa"),
				equalTo(request.getUri().toString()));
	}

	@Test
	public void formatUriToStringAddingPath() {
		HttpIORequest request = createRequest(rootUrl);
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL, "/my/path"), equalTo(request.getUri()
				.toString()));

		request = createRequest(rootUrlQ);
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL_PQ, "/my/path", "q=PerLa"), equalTo(request
				.getUri().toString()));

		request = createRequest(rootUrlPQ);
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL_PQ, "/my/path/my/path", "q=PerLa"),
				equalTo(request.getUri().toString()));
	}

	@Test
	public void formatUriToStringAddingPathAndQuery() {
		HttpIORequest request = createRequest(rootUrl);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL_PQ, "/my/path", "q=PerLa"), equalTo(request
				.getUri().toString()));

		request = createRequest(rootUrlQ);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL_PQ, "/my/path", "q=PerLa&q=PerLa"),
				equalTo(request.getUri().toString()));

		request = createRequest(rootUrlPQ);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));
		assertThat(format(ROOT_URL_PQ, "/my/path/my/path", "q=PerLa&q=PerLa"),
				equalTo(request.getUri().toString()));
	}

	@Test
	public void formatUriToStringOverridingPathAndQuery() {
		HttpIORequest request = createRequest(rootUrl);
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("qq=PerLa"));
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/path/my"));

		request.setParameter(HttpIORequest.NamedParameterKey.QUERY,
				new StringPayload("q=PerLa"));
		request.setParameter(HttpIORequest.NamedParameterKey.PATH,
				new StringPayload("/my/path"));

		assertThat(format(ROOT_URL_PQ, "/my/path", "q=PerLa"), equalTo(request
				.getUri().toString()));
	}

	@Test
	public void checkSettingPayload() {
		HttpIORequest request = createRequest(rootUrl);
		Payload cp = new StringPayload("qq=PerLa");
		request.setParameter(HttpIORequest.NamedParameterKey.QUERY, cp);
		request.setParameter(HttpIORequest.NamedParameterKey.ENTITY, cp);
		request.setParameter(HttpIORequest.NamedParameterKey.PATH, cp);

		assertThat(cp, equalTo(request.getQuery()));
		assertThat(cp, equalTo(request.getEntity()));
		assertThat(cp, equalTo(request.getPath()));
	}

	private HttpIORequest createRequest(URL rootUrl) {
		return new HttpIORequest("foo_request", HttpMethod.GET, rootUrl,
				ContentType.WILDCARD);
	}
}
