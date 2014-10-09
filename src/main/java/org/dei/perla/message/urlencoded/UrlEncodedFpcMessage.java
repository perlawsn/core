package org.dei.perla.message.urlencoded;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.message.FpcMessage;
import org.dei.perla.utils.DateUtils;

public class UrlEncodedFpcMessage implements FpcMessage {

	private final String id;
	private final Map<String, ? extends FieldDescriptor> fieldMap;
	private final Map<String, DataType> fieldDataType;
	private final Map<String, String> fieldValueMap;
	private final Map<String, DateTimeFormatter> dateFormatterMap;
	private final Map<String, String> staticFieldMap;

	protected UrlEncodedFpcMessage(String id,
			Map<String, ? extends FieldDescriptor> fieldMap,
			Map<String, String> staticFieldMap,
			Map<String, DateTimeFormatter> dateFormatterMap) {
		this(id, fieldMap, new HashMap<String, String>(staticFieldMap),
				staticFieldMap, dateFormatterMap);
	}

	protected UrlEncodedFpcMessage(String id,
			Map<String, ? extends FieldDescriptor> fieldMap,
			Map<String, String> fieldValueMap,
			Map<String, String> staticFieldMap,
			Map<String, DateTimeFormatter> dateFormatterMap) {
		this.id = id;
		this.fieldMap = fieldMap;
		this.fieldValueMap = fieldValueMap;
		this.staticFieldMap = staticFieldMap;
		this.dateFormatterMap = dateFormatterMap;

		this.fieldDataType = new HashMap<String, DataType>();
		fieldMap.forEach((name, field) -> {
			DataType type = DataType.valueOf(field.getType().toUpperCase());
			fieldDataType.put(name, type);
		});
	}

	protected Map<String, String> getFieldValueMap() {
		return fieldValueMap;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean hasField(String name) {
		FieldDescriptor field = fieldMap.get(name);
		if (field == null) {
			return false;
		}
		return true;
	}

	@Override
	public Object getField(String name) throws IllegalArgumentException {
		FieldDescriptor field;

		field = fieldMap.get(name);
		if (field == null) {
			throw new IllegalArgumentException("Field '" + name
					+ "' not found in message '" + id + "'.");
		}

		return valueOf(field, fieldValueMap.get(name));
	}

	@Override
	public void setField(String name, Object value)
			throws IllegalArgumentException {
		FieldDescriptor field;

		field = fieldMap.get(name);
		if (field == null) {
			throw new IllegalArgumentException("Field '" + name
					+ "' not found in message '" + id + "'.");
		}
		if (field.isStatic()) {
			throw new IllegalArgumentException("Static field '" + name
					+ "' in message '" + id + "' cannot be changed.");
		}

		fieldValueMap.put(name, asString(field, value));
	}
	
	@Override
	public void appendElement(String name, Object value)
			throws IllegalArgumentException {
		throw new RuntimeException("UrlEncoded messages do not support list type");
	}

	@Override
	public boolean validate() {
		for (String key : staticFieldMap.keySet()) {
			if (!fieldValueMap.containsKey(key)
					|| !staticFieldMap.get(key).equals(fieldValueMap.get(key))) {
				return false;
			}
		}
		return true;
	}

	private String asString(FieldDescriptor field, Object object) {
		if (object == null) {
			return "";
		}

		DataType type = fieldDataType.get(field.getName());
		switch (type) {
		case FLOAT:
		case INTEGER:
		case BOOLEAN:
		case STRING:
		case ID:
			return object.toString();
		case TIMESTAMP:
			DateTimeFormatter fmt = dateFormatterMap.get(field.getName());
			return DateUtils.format(fmt, object);
		default:
			throw new RuntimeException("Unsupported data '" + type
					+ "' in UrlEncoded message");
		}
	}

	private Object valueOf(FieldDescriptor field, String value) {
		if (value == null) {
			return null;
		}

		DataType type = fieldDataType.get(field.getName());
		switch (type) {
		case BOOLEAN:
			return new Boolean(value);
		case FLOAT:
			return new Float(value);
		case INTEGER:
			return new Integer(value);
		case STRING:
			return value;
		case ID:
			return new Integer(value);
		case TIMESTAMP:
			DateTimeFormatter fmt = dateFormatterMap.get(field.getName());
			return DateUtils.parse(fmt, value);
		default:
			throw new RuntimeException("Unsupported data '" + type
					+ "' in UrlEncoded message");
		}
	}

}
