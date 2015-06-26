package org.dei.perla.core.channel.loopback;

import org.dei.perla.core.message.FpcMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>FpcMessage</code> implementation for testing the
 * <code>ScriptEngine</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class TestMessage implements FpcMessage {

	private final String id;
	private final Map<String, Object> valueMap;

	protected TestMessage(String id) {
		this(id, new HashMap<>());
	}

	protected TestMessage(String id, Map<String, Object> valueMap) {
		this.id = id;
		this.valueMap = valueMap;
	}

	public String getMessageId() {
		return id;
	}

	public Map<String, Object> getValueMap() {
		return valueMap;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean hasField(String id) {
		return true;
	}

	@Override
	public Object getField(String name) throws IllegalArgumentException {
		return valueMap.get(name);
	}

	@Override
	public void setField(String name, Object value)
			throws IllegalArgumentException {
		valueMap.put(name, value);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void appendElement(String name, Object value)
			throws IllegalArgumentException {
		Object field = valueMap.get(name);
		if (field == null) {
			field = new ArrayList<>();
			valueMap.put(name, field);
		}
		if (!(field instanceof ArrayList)) {
			throw new IllegalArgumentException("Field '" + name + "' is not a list");
		}
		ArrayList list = (ArrayList) field;
		list.add(value);
	}

	@Override
	public boolean validate() {
		return true;
	}

}
