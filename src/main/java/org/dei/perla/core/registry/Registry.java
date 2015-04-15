package org.dei.perla.core.registry;

import org.dei.perla.core.fpc.Fpc;

import java.util.Collection;

public interface Registry {

	public Fpc get(int id);

	public Collection<Fpc> getAll();

	public Collection<Fpc> getByAttribute(Collection<DataTemplate> with,
			Collection<DataTemplate> without);

	public void add(Fpc fpc);

	public void remove(Fpc fpc);

}
