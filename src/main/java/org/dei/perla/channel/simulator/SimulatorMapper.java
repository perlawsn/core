package org.dei.perla.channel.simulator;

import java.util.Map;

import org.dei.perla.channel.Payload;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.message.AbstractMapper;
import org.dei.perla.message.FpcMessage;

/**
 * A custom <code>Mapper</code> implementation designed for the
 * <code>SimulatorChannel</code>.
 * 
 * This class performs marshalling and unmarshalling operations between
 * <code>SimulatorMessage</code> and <code>SimulatorPayload</code> classes. As
 * such any use outside the <code>SimulatorChannel</code> is strictly forbidden.
 * 
 * @author Guido Rota (2014)
 * 
 */
public class SimulatorMapper extends AbstractMapper {

	private final Map<String, String> staticFieldMap;

	public SimulatorMapper(String messageId,
			Map<String, FieldDescriptor> fieldMap,
			Map<String, String> staticFieldMap) {
		super(messageId, fieldMap);
		this.staticFieldMap = staticFieldMap;
	}

	@Override
	public FpcMessage createMessage() {
		return new SimulatorMessage(messageId, fieldMap, staticFieldMap);
	}

	@Override
	public FpcMessage unmarshal(Payload payload) {
		SimulatorPayload simulatorPayload;

		if (!(payload instanceof SimulatorPayload)) {
			throw new RuntimeException(
					"SimulatorMapper.unmarshal can only be used to "
							+ "unmarshal from a SimulatorPayload class");
		}
		simulatorPayload = (SimulatorPayload) payload;
		return new SimulatorMessage(messageId, fieldMap, simulatorPayload.getValueMap(),
				staticFieldMap);
	}

	@Override
	public Payload marshal(FpcMessage message) {
		SimulatorMessage simulatorMessage;

		if (!(message instanceof SimulatorMessage)) {
			throw new RuntimeException(
					"SimulatorMapper.marshal can only be used to "
							+ "marshal data from SimulatorMessage");
		}
		simulatorMessage = (SimulatorMessage) message;
		return new SimulatorPayload(simulatorMessage.getValueMap());
	}

}
