package org.dei.perla.core.message.urlencoded;

import org.dei.perla.core.descriptor.FieldDescriptor;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.DataType.ConcreteType;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.utils.DateUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class UrlEncodedFpcMessage implements FpcMessage {

    private final String id;
    private final Map<String, ? extends FieldDescriptor> fieldMap;
    private final Map<String, ConcreteType> fieldDataType;
    private final Map<String, String> fieldValueMap;
    private final Map<String, DateTimeFormatter> dateFormatterMap;
    private final Map<String, String> staticFieldMap;

    protected UrlEncodedFpcMessage(String id,
            Map<String, ? extends FieldDescriptor> fieldMap,
            Map<String, String> staticFieldMap,
            Map<String, DateTimeFormatter> dateFormatterMap) {
        this(id, fieldMap, new HashMap<>(staticFieldMap),
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

        this.fieldDataType = new HashMap<>();
        fieldMap.forEach((name, field) -> {
            ConcreteType type = ConcreteType.parse(field.getType());
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
        return field != null;
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
        if (type == DataType.INTEGER || type == DataType.FLOAT ||
				type == DataType.BOOLEAN || type == DataType.STRING ||
				type == DataType.ID) {
			return object.toString();
		} else if (type == DataType.TIMESTAMP) {
            DateTimeFormatter fmt = dateFormatterMap.get(field.getName());
            return DateUtils.format(fmt, object);
        } else {
            throw new RuntimeException("Unsupported data '" + type
                    + "' in UrlEncoded message");
        }
    }

    private Object valueOf(FieldDescriptor field, String value) {
        if (value == null) {
            return null;
        }

        ConcreteType type = fieldDataType.get(field.getName());
        if (type == DataType.TIMESTAMP) {
            DateTimeFormatter fmt = dateFormatterMap.get(field.getName());
            return DateUtils.parse(fmt, value);
        } else {
            return type.valueOf(value);
        }
    }

}
