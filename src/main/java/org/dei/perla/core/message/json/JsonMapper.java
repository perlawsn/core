package org.dei.perla.core.message.json;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.dei.perla.core.channel.ByteArrayPayload;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.fpc.descriptor.FieldDescriptor;
import org.dei.perla.core.message.AbstractMapper;
import org.dei.perla.core.message.FpcMessage;

public class JsonMapper extends AbstractMapper {

	private final Logger logger = Logger.getLogger(JsonMapper.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private final Class<FpcMessage> messageClass;

	static {
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	protected JsonMapper(String messageId,
			Class<FpcMessage> messageClass,
			Map<String, FieldDescriptor> fieldMap) {
		super(messageId, fieldMap);
		this.messageClass = messageClass;
	}

	@Override
	public FpcMessage createMessage() {
		try {
			return messageClass.newInstance();
		} catch (Exception e) {
			String errorMessage = "";
			logger.error(errorMessage);
			throw new RuntimeException(errorMessage, e);
		}

	}

	protected String getMessageClassName() {
		return messageClass.getCanonicalName();
	}

	public FpcMessage unmarshal(Payload data) {
		try {
			return mapper.readValue(data.asInputStream(), messageClass);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Payload marshal(FpcMessage fpcMessage) {
		try {
			String json = mapper.writeValueAsString(fpcMessage);
			return new ByteArrayPayload(json);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
