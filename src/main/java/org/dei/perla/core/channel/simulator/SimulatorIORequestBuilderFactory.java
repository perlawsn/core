package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.core.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.Conditions;

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
