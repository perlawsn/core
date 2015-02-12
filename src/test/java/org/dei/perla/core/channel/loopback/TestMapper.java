package org.dei.perla.core.channel.loopback;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.fpc.descriptor.FieldDescriptor;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;

/**
 * <code>Mapper</code> implementation for testing the <code>ScriptEngine</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class TestMapper implements Mapper {

	private final String messageId;
	private final Map<String, FieldDescriptor> fieldMap;

	public TestMapper(String messageId) {
		this.messageId = messageId;
		fieldMap = new HashMap<>();
	}

	public void addField(TestFieldDescriptor field) {
		fieldMap.put(field.getName(), field);
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public FieldDescriptor getFieldDescriptor(String name) {
		return fieldMap.get(name);
	}

	@Override
	public Collection<FieldDescriptor> getFieldDescriptors() {
		return fieldMap.values();
	}

	@Override
	public FpcMessage createMessage() {
		return new TestMessage(messageId);
	}

	@Override
	public FpcMessage unmarshal(Payload payload) {
		if (!(payload instanceof TestPayload)) {
			throw new RuntimeException();
		}
		TestPayload testPld = (TestPayload) payload;
		return new TestMessage(messageId, testPld.getValueMap());
	}

	@Override
	public Payload marshal(FpcMessage message) {
		if (!(message instanceof TestMessage)) {
			throw new RuntimeException();
		}
		TestMessage testMsg = (TestMessage) message;
		return new TestPayload(testMsg.getValueMap());
	}

}
