package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.simulator.DynamicFieldGenerator.*;
import org.dei.perla.core.channel.simulator.FieldGenerator.StaticFieldGenerator;
import org.dei.perla.core.channel.simulator.StepFieldGenerator.StepFloatFieldGenerator;
import org.dei.perla.core.channel.simulator.StepFieldGenerator.StepIntFieldGenerator;
import org.dei.perla.core.descriptor.ChannelDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.Errors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>ChannelFactory</code> implementation for generating
 * <code>SimulatorChannel</code>s.
 *
 * @author Guido Rota (2014)
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

        Check.notNull(descriptor, "descriptor");
        Check.argument(descriptor instanceof SimulatorChannelDescriptor,
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

        return parseResponseList(err, descriptor.getResponseList());
    }

    private Generator[] parseResponseList(Errors err,
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
                    err.inContext("Response generator '" + gen.getId() + "'"), gen));
            i++;
        }

        return generatorArray;
    }

    private FieldGenerator[] parseResponse(Errors err,
            GeneratorDescriptor genDesc) {
        FieldGenerator[] valueGeneratorArray;
        Set<String> attributeList = new HashSet<>();

        if (genDesc.getFieldList().isEmpty()) {
            err.addError("No f generators found");
        }

        int i = 0;
        valueGeneratorArray = new FieldGenerator[genDesc.getFieldList().size()];
        for (GeneratorFieldDescriptor field : genDesc.getFieldList()) {
            String fieldName = field.getName();
            if (Check.nullOrEmpty(fieldName)) {
                err.addError("Missing field name");
                continue;
            }
            if (field.getStrategy() == null) {
                err.addError("Missing generation strategy");
                continue;
            }
            if (attributeList.contains(fieldName)) {
                err.addError("Duplicate field name '" + field.getName() + "'");
                continue;
            }
            attributeList.add(field.getName());
            valueGeneratorArray[i] = parseField(
                    err.inContext("Field generator '" + field.getName() + "'"),
                    field);
            i++;
        }

        return valueGeneratorArray;
    }

    private FieldGenerator parseField(Errors err,
            GeneratorFieldDescriptor field) {

        switch (field.getStrategy()) {
            case DYNAMIC:
                return createRandomGenerator(err, field);
            case STEP:
                return createStepGenerator(err, field);
            case STATIC:
                if (field.getValue() == null || field.getValue().isEmpty()) {
                    err.addError("Missing static value for static-qualified field");
                    return null;
                }
                return new StaticFieldGenerator(field.getName(), field.getValue());
            default:
                throw new RuntimeException("Unexpected field qualifier '"
                        + field.getStrategy() + "'.");
        }
    }

    private FieldGenerator createRandomGenerator(Errors e,
            GeneratorFieldDescriptor f) {

        if (f.getMin() > f.getMax()) {
            e.addError(INVALID_BOUNDARY_GENERATOR);
        }

        switch (f.getType().toLowerCase()) {
            case "integer":
                return new RandomIntFieldGenerator(f.getName(), f.getMin(),
                        f.getMax());
            case "float":
                return new DynamicFloatFieldGenerator(f.getName(), f.getMin(),
                        f.getMax());
            case "string":
                if (f.getMin() < 0) {
                    e.addError("String lenght must be greater than zero");
                }
                return new DynamicStringFieldGenerator(f.getName(), f.getMin(),
                        f.getMax());
            case "boolean":
                return new DynamicBooleanFieldGenerator(f.getName());
            case "timestamp":
                return new DynamicTimestampFieldGenerator(f.getName());
            default:
                e.addError("Unsupported dynamic field generator for type '" + f.getType()
                        + "'");
                return null;
        }
    }

    private FieldGenerator createStepGenerator(Errors e, GeneratorFieldDescriptor f) {

        if (f.getMin() > f.getMax()) {
            e.addError(INVALID_BOUNDARY_GENERATOR);
        }

        try {
            switch (f.getType().toLowerCase()) {
                case "integer":
                    int iInc = Integer.parseInt(f.getIncrement());
                    return new StepIntFieldGenerator(f.getName(), f.getMin(), f.getMax(), iInc);
                case "float":
                    float fInc = Float.parseFloat(f.getIncrement());
                    return new StepFloatFieldGenerator(f.getName(), f.getMin(), f.getMax(), fInc);
                default:
                    e.addError("Unsupported step field generator for type '" + f.getType() + "'");
                    return null;
            }
        } catch (NumberFormatException exc) {
            e.addError("Invalid 'increment' value", exc);
            return null;
        }
    }

    private static final String INVALID_BOUNDARY_GENERATOR = "Invalid boundaries for value generator: min is greater than max.";

}
