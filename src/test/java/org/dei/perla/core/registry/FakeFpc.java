package org.dei.perla.core.registry;

import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FakeFpc implements Fpc {

	private final Collection<Attribute> attributes;
	private final int id;

	public FakeFpc(int id, Collection<Attribute> attributes) {
		this.attributes = new ArrayList<>(attributes);
        this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

    @Override
    public String getType() {
        return "FakeFpc";
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
	public void stop(Consumer<Fpc> handler) {
		throw new UnsupportedOperationException();
	}

}
