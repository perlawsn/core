package org.dei.perla.core.channel.simulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.utils.Conditions;

/**
 * <code>RequestBuilder</code> implementation for the
 * <code>SimulatorChannel</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class SimulatorIORequestBuilder implements IORequestBuilder {

	private static final List<IORequestParameter> paramList;

	static {
		IORequestParameter[] paramArray = new IORequestParameter[1];
		paramArray[0] = new IORequestParameter("period", false);
		paramList = Collections.unmodifiableList(Arrays.asList(paramArray));
	}

	private final String requestId;
	private final String generatorId;

	protected SimulatorIORequestBuilder(String requestId, String generatorId) {
		this.requestId = Conditions.checkNotNull(requestId, "requestId");
		this.generatorId = Conditions.checkNotNull(generatorId, "generatorId");
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	@Override
	public SimulatorIORequest create() {
		return new SimulatorIORequest(requestId, generatorId);
	}

	@Override
	public List<IORequestParameter> getParameterList() {
		return paramList;
	}

	public String getGeneratorId() {
		return generatorId;
	}

}
