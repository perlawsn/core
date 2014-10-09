package org.dei.perla.message.urlencoded;

import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.dei.perla.channel.ByteArrayPayload;
import org.dei.perla.channel.Payload;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.message.AbstractMapper;
import org.dei.perla.message.FpcMessage;

public class UrlEncodedMapper extends AbstractMapper {

	private final static Charset charset = Charset.forName("UTF-8");
	private final String messageId;
	private final Map<String, String> staticFieldMap;
	private final Map<String, DateTimeFormatter> dateFormatterMap;

	protected UrlEncodedMapper(String messageId,
			Map<String, ? extends FieldDescriptor> fieldMap,
			Map<String, String> staticFieldMap,
			Map<String, DateTimeFormatter> dateFormatterMap) {
		super(messageId, fieldMap);
		this.messageId = messageId;
		this.staticFieldMap = staticFieldMap;
		this.dateFormatterMap = dateFormatterMap;
	}

	@Override
	public FpcMessage createMessage() {
		return new UrlEncodedFpcMessage(messageId, fieldMap, staticFieldMap,
				dateFormatterMap);
	}

	@Override
	public FpcMessage unmarshal(Payload data) {
		Map<String, String> fieldValueMap = new HashMap<>();
		List<? extends NameValuePair> fieldValueList;

		fieldValueList = URLEncodedUtils.parse(data.asString(),
				data.getCharset());
		for (NameValuePair fieldValue : fieldValueList) {
			fieldValueMap.put(fieldValue.getName(), fieldValue.getValue());
		}
		return new UrlEncodedFpcMessage(messageId, fieldMap,
				fieldValueMap, staticFieldMap, dateFormatterMap);
	}

	@Override
	public Payload marshal(FpcMessage message) {
		Map<String, String> fieldValueMap;
		List<BasicNameValuePair> fieldValueList;
		UrlEncodedFpcMessage httpQueryMessage;

		httpQueryMessage = (UrlEncodedFpcMessage) message;
		fieldValueList = new ArrayList<>();
		fieldValueMap = httpQueryMessage.getFieldValueMap();
		for (String key : fieldValueMap.keySet()) {
			fieldValueList
					.add(new BasicNameValuePair(key, fieldValueMap.get(key)));
		}
		return new ByteArrayPayload(URLEncodedUtils.format(fieldValueList,
				charset), charset);
	}

}
