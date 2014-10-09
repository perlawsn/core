package org.dei.perla.fpc.base;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributeAccessType;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.utils.Conditions;

public class StaticAttribute extends Attribute {

	private final Object value;

	public StaticAttribute(AttributeDescriptor descriptor) {
		super(descriptor);
		Conditions.checkIllegalArgument(
				descriptor.getAccess() == AttributeAccessType.STATIC,
				NON_STATIC_ERROR);

		DataType type = descriptor.getType();
		this.value = DataType.parse(type, descriptor.getValue());
	}

	public StaticAttribute(String id, DataType type, Object value) {
		super(id, type);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	private static final String NON_STATIC_ERROR = "Cannot create StaticAttribute class from non-static attribute descriptor";
	
}
