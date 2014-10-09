package org.dei.perla.channel.loopback;

import java.util.Collections;
import java.util.List;

import org.dei.perla.channel.IORequest;
import org.dei.perla.channel.IORequestBuilder;

public class LoopbackIORequestBuilder implements IORequestBuilder {

	private final String id; 
	
	public LoopbackIORequestBuilder(String id) {
		this.id = id;
	}
	
	@Override
	public String getRequestId() {
		return id;
	}

	@Override
	public IORequest create() {
		return new LoopbackIORequest();
	}

	@Override
	public List<IORequestParameter> getParameterList() {
		return Collections.emptyList();
	}

}
