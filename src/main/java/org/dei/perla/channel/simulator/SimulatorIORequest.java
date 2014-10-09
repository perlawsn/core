package org.dei.perla.channel.simulator;

import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.Payload;

/**
 * A <code>Request</code> implementation to be used with the
 * <code>SimulatorChannel</code>.
 * 
 * The <code>generatorId</code> is used for selecting which generator has to be
 * used by the <code>SimulatorChannel</code> (see <code>SimulatorChannel</code>
 * for further information).
 * 
 * @author Guido Rota (2014)
 * 
 */
public class SimulatorIORequest implements IORequest {

	private final String id;
	private final String generatorId;
	private Payload period;

	protected SimulatorIORequest(String id, String generatorId) {
		this.id = id;
		this.generatorId = generatorId;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getGeneratorId() {
		return generatorId;
	}

	public Payload getPeriod() {
		return period;
	}

	@Override
	public void setParameter(String name, Payload payload) {
		if (name.equals("period")) {
			period = payload;
		}
	}

}
