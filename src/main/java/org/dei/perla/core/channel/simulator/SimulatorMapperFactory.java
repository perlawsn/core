package org.dei.perla.core.channel.simulator;

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;

import org.dei.perla.core.fpc.descriptor.FieldDescriptor;
import org.dei.perla.core.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.fpc.descriptor.MessageDescriptor;
import org.dei.perla.core.message.AbstractMapperFactory;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.Conditions;
import org.dei.perla.core.utils.Errors;

/**
 * A custom <code>MapperFactory</code> implementation designed for the
 * <code>SimulatorChannel</code>. As such any use outside the
 * <code>SimulatorChannel</code> is strictly forbidden.
 *
 * @author Guido Rota (2014)
 *
 */
public class SimulatorMapperFactory extends AbstractMapperFactory {

	public SimulatorMapperFactory() {
		super(SimulatorMessageDescriptor.class);
	}

	@Override
	public Mapper createMapper(MessageDescriptor descriptor,
			Map<String, Mapper> mapperMap, ClassPool classPool)
			throws InvalidDeviceDescriptorException {
		Errors err = new Errors("Message '" + descriptor.getId() + "'");
		Map<String, String> staticFieldMap = new HashMap<>();
		Map<String, FieldDescriptor> msgFieldMap = new HashMap<>();

		Conditions.checkNotNull(descriptor, "descriptor");
		Conditions
				.checkIllegalArgument(descriptor instanceof SimulatorMessageDescriptor);

		for (FieldDescriptor field : descriptor.getFieldList()) {
			msgFieldMap.put(field.getName(), field);
			if (field.isStatic()) {
				staticFieldMap.put(field.getName(), field.getValue());
			}
		}

		if (!err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}

		return new SimulatorMapper(descriptor.getId(), msgFieldMap,
				staticFieldMap);
	}
}
