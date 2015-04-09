package org.dei.perla.core.registry;

import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.record.Attribute;
import org.dei.perla.core.utils.StopHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    public String getType() {
        return "";
    }

	@Override
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public Task set(Map<Attribute, Object> valueMap, boolean strict,
			TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task get(List<Attribute> attributes, boolean strict,
			TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task get(List<Attribute> attributes, boolean strict, long periodMs,
			TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Task async(List<Attribute> attributes, boolean strict,
			TaskHandler handler) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stop(StopHandler<Fpc> handler) {
		throw new UnsupportedOperationException();
	}

}
