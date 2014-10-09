package org.dei.perla.channel.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dei.perla.channel.Channel;
import org.dei.perla.channel.ChannelFactory;
import org.dei.perla.channel.simulator.Generator.BooleanFieldGenerator;
import org.dei.perla.channel.simulator.Generator.FieldGenerator;
import org.dei.perla.channel.simulator.Generator.FloatFieldGenerator;
import org.dei.perla.channel.simulator.Generator.IntFieldGenerator;
import org.dei.perla.channel.simulator.Generator.StaticFieldGenerator;
import org.dei.perla.channel.simulator.Generator.StringFieldGenerator;
import org.dei.perla.channel.simulator.Generator.TimestampFieldGenerator;
import org.dei.perla.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.utils.Check;
import org.dei.perla.utils.Conditions;
import org.dei.perla.utils.Errors;

/**
 * <code>ChannelFactory</code> implementation for generating
 * <code>SimulatorChannel</code>s.
 * 
 * 
 * @author Guido Rota (2014)
 * 
 */
public class SimulatorChannelFactory implements ChannelFactory {

	@Override
	public Class<? extends ChannelDescriptor> acceptedChannelDescriptorClass() {
		return SimulatorChannelDescriptor.class;
	}

	@Override
	public Channel createChannel(ChannelDescriptor descriptor)
			throws InvalidDeviceDescriptorException {
		Errors err = new Errors("Simulator channel '" + descriptor.getId()
				+ "'");
		Generator[] generatorArray;

		Conditions.checkNotNull(descriptor, "descriptor");
		Conditions.checkIllegalArgument(
				descriptor instanceof SimulatorChannelDescriptor,
				"Cannot create SimulatorChannel: expected "
						+ SimulatorChannelDescriptor.class.getCanonicalName()
						+ " but received "
						+ descriptor.getClass().getCanonicalName() + ". ");

		generatorArray = parseDescriptor(err,
				(SimulatorChannelDescriptor) descriptor);
		if (!err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}

		return new SimulatorChannel(descriptor.getId(), generatorArray);
	}

	private Generator[] parseDescriptor(Errors err,
			SimulatorChannelDescriptor descriptor) {

		// Parse SimulatorResponseDescriptor list
		if (descriptor.getResponseList().isEmpty()) {
			err.addError("No response response generators found");
			return null;
		}

		return parseResponseList(err, descriptor, descriptor.getResponseList());
	}

	private Generator[] parseResponseList(Errors err,
			SimulatorChannelDescriptor descriptor,
			List<GeneratorDescriptor> generatorList) {
		Generator[] generatorArray;
		List<String> responseIdList = new ArrayList<>();

		int i = 0;
		generatorArray = new Generator[generatorList.size()];
		for (GeneratorDescriptor gen : generatorList) {
			if (gen.getId() == null || gen.getId().isEmpty()) {
				err.addError("Missing response ID");
				continue;

			} else if (responseIdList.contains(gen.getId())) {
				err.addError("Duplicate response ID '" + gen.getId() + "'");
				continue;
			}
			responseIdList.add(gen.getId());
			generatorArray[i] = new Generator(gen.getId(), parseResponse(
					err.inContext("Response generator '" + gen.getId() + "'"),
					descriptor, gen));
			i++;
		}

		return generatorArray;
	}

	private FieldGenerator[] parseResponse(Errors err,
			SimulatorChannelDescriptor descriptor, GeneratorDescriptor genDesc) {
		FieldGenerator[] valueGeneratorArray;
		Set<String> attributeList = new HashSet<>();

		if (genDesc.getFieldList().isEmpty()) {
			err.addError("No field generators found");
		}

		int i = 0;
		valueGeneratorArray = new FieldGenerator[genDesc.getFieldList().size()];
		for (GeneratorFieldDescriptor field : genDesc.getFieldList()) {
			String fieldName = field.getName();
			if (Check.nullOrEmpty(fieldName)) {
				err.addError("Missing field name");
				continue;
			}

			if (attributeList.contains(fieldName)) {
				err.addError("Duplicate field name '" + field.getName() + "'");

			}
			attributeList.add(field.getName());
			valueGeneratorArray[i] = parseField(
					err.inContext("Field generator '" + field.getName() + "'"),
					descriptor, genDesc, field);
			i++;
		}

		return valueGeneratorArray;
	}

	private FieldGenerator parseField(Errors err,
			SimulatorChannelDescriptor descriptor, GeneratorDescriptor genDesc,
			GeneratorFieldDescriptor field) {

		switch (field.getQualifier()) {
		case DYNAMIC:
			return createGenerator(err, descriptor, genDesc, field);
		case STATIC:
			if (field.getValue() == null || field.getValue().isEmpty()) {
				err.addError("Missing static value for static-qualified field");
				return null;
			}
			return new StaticFieldGenerator(field.getName(), field.getValue());
		default:
			throw new RuntimeException("Unexpected field qualifier '"
					+ field.getQualifier() + "'.");
		}
	}

	private FieldGenerator createGenerator(Errors err,
			SimulatorChannelDescriptor descriptor, GeneratorDescriptor genDesc,
			GeneratorFieldDescriptor field) {

		if (field.getMin() > field.getMax()) {
			err.addError("Invalid boundaries for value generator: min is greater than max.");
		}

		switch (field.getType()) {
		case INTEGER:
			return new IntFieldGenerator(field.getName(), field.getMin(),
					field.getMax());
		case FLOAT:
			return new FloatFieldGenerator(field.getName(), field.getMin(),
					field.getMax());
		case STRING:
			if (field.getMin() < 0) {
				err.addError("String lenght must be greater than zero");
			}
			return new StringFieldGenerator(field.getName(), field.getMin(),
					field.getMax());
		case BOOLEAN:
			return new BooleanFieldGenerator(field.getName());
		case TIMESTAMP:
			return new TimestampFieldGenerator(field.getName());
		default:
			err.addError("Unsupported field generator type '" + field.getType()
					+ "'");
			return null;
		}
	}

}
