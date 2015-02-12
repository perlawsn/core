package org.dei.perla.core.message;

import org.dei.perla.core.descriptor.MessageDescriptor;

public abstract class AbstractMapperFactory implements MapperFactory {

	private Class<? extends MessageDescriptor> messageDescriptorClass;

	public AbstractMapperFactory(Class<? extends MessageDescriptor> descriptorType) {
		this.messageDescriptorClass = descriptorType;
	}

	public Class<? extends MessageDescriptor> acceptedMessageDescriptorClass() {
		return messageDescriptorClass;
	}

}
