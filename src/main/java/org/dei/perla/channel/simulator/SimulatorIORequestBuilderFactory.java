package org.dei.perla.channel.simulator;

import org.dei.perla.channel.IORequestBuilderFactory;
import org.dei.perla.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.utils.Check;
import org.dei.perla.utils.Conditions;

/**
 * <code>RequestBuilderFactory</code> implementation for the
 * <code>SimulatorChannel</code>.
 * 
 * @author Guido Rota (2014)
 *
 */
public class SimulatorIORequestBuilderFactory implements
		IORequestBuilderFactory {

	@Override
	public Class<? extends IORequestDescriptor> acceptedIORequestClass() {
		return SimulatorIORequestDescriptor.class;
	}

	@Override
	public SimulatorIORequestBuilder create(IORequestDescriptor descriptor)
			throws InvalidDeviceDescriptorException {
		Conditions.checkNotNull(descriptor, "descriptor");
		Conditions
				.checkIllegalArgument(descriptor instanceof SimulatorIORequestDescriptor);

		SimulatorIORequestDescriptor simDesc = (SimulatorIORequestDescriptor) descriptor;
		if (Check.nullOrEmpty(simDesc.getGeneratorId())) {
			throw new InvalidDeviceDescriptorException(
					"Missing 'generator' field in '" + simDesc.getId()
							+ "' Simulator request descriptor.");
		}

		return new SimulatorIORequestBuilder(simDesc.getId(),
				simDesc.getGeneratorId());
	}

}
