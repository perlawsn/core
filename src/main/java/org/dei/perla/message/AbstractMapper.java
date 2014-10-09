package org.dei.perla.message;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.dei.perla.fpc.descriptor.FieldDescriptor;

/**
 * A convenience implementation of the Mapper interface.
 * 
 * @author Guido Rota (2014)
 * 
 */
public abstract class AbstractMapper implements Mapper {

	protected final String messageId;
	protected final Map<String, ? extends FieldDescriptor> fieldMap;

	public AbstractMapper(String messageId,
			Map<String, ? extends FieldDescriptor> fieldMap) {
		this.messageId = messageId;
		this.fieldMap = Collections.unmodifiableMap(fieldMap);
	}

	public final String getMessageId() {
		return messageId;
	}

	@Override
	public final FieldDescriptor getFieldDescriptor(String name) {
		return fieldMap.get(name);
	}
	
	@Override
	public final Collection<FieldDescriptor> getFieldDescriptors() {
		return Collections.unmodifiableCollection(fieldMap.values());
	}
	
}
