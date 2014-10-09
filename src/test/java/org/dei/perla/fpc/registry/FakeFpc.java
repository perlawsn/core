package org.dei.perla.fpc.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.Task;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.utils.StopHandler;

public class FakeFpc implements Fpc {

	private final Collection<Attribute> attributes;

	public FakeFpc(Collection<Attribute> attributes) {
		this.attributes = new ArrayList<>(attributes);
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public Task set(Map<Attribute, Object> valueMap, TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task get(Collection<Attribute> attributes, TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task get(Collection<Attribute> attributes, long periodMs,
			TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task async(Collection<Attribute> attributes, TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop(StopHandler<Fpc> handler) {
		throw new UnsupportedOperationException();
	}

}
