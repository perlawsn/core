package org.dei.perla.channel.simulator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.message.FpcMessage;

/**
 * A custom <code>FpcMessage</code> implementation designed for the
 * <code>SimulatorChannel</code>.
 *
 * This class is only intended to be used in conjunction with the
 * <code>SimulatorChannel</code> and <code>SimulatorMapper</code> components.
 *
 * @author Guido Rota (2014)
 *
 */
public class SimulatorMessage implements FpcMessage {

	private final String id;
	private final Map<String, ? extends FieldDescriptor> fieldMap;
	private final Map<String, Object> valueMap;
	private final Map<String, String> staticFieldMap;

	protected SimulatorMessage(String id,
			Map<String, ? extends FieldDescriptor> fieldMap,
			Map<String, String> staticFieldMap) {
		this(id, fieldMap, new HashMap<>(), staticFieldMap);
	}

	protected SimulatorMessage(String id,
			Map<String, ? extends FieldDescriptor> fieldMap,
			Map<String, Object> valueMap, Map<String, String> staticFieldMap) {
		this.id = id;
		this.fieldMap = fieldMap;
		this.valueMap = valueMap;
		this.staticFieldMap = staticFieldMap;
	}

	/**
	 * Returns a <code>Map</code> containing the the data stored inside this
	 * <code>SimulatorPayload</code>.
	 *
	 * This method is used by the <code>SimulatorMapper</code> to retrieve the
	 * payload data during the marshal operation.
	 *
	 * @return <code>Map</code> containing the payload data
	 */
	protected Map<String, Object> getValueMap() {
		return Collections.unmodifiableMap(valueMap);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean hasField(String name) {
        return fieldMap.containsKey(name);
	}

	@Override
	public Object getField(String name) throws IllegalArgumentException {
		FieldDescriptor field = fieldMap.get(name);
		if (field == null) {
			throw new IllegalArgumentException("Field '" + name
					+ "' not found in message '" + id + "'.");
		}
		return valueMap.get(name);
	}

	@Override
	public void setField(String name, Object value)
			throws IllegalArgumentException {
		FieldDescriptor field = fieldMap.get(name);
		if (field == null) {
			throw new IllegalArgumentException("Field '" + name
					+ "' not found in message '" + id + "'.");
		}
		if (field.isStatic()) {
			throw new IllegalArgumentException("Static field '" + name
					+ "' in message '" + id + "' cannot be changed.");
		}
		valueMap.put(name, value);
	}

	@Override
	public void appendElement(String name, Object value)
			throws IllegalArgumentException {
		throw new RuntimeException("Simulator messages do not support list type");
	}

	@Override
	public boolean validate() {
		for (String field : staticFieldMap.keySet()) {
			if (!valueMap.containsKey(field)) {
				return false;
			}
			if (!valueMap.get(field).equals(staticFieldMap.get(field))) {
				return false;
			}
		}
		return true;
	}

}
