package org.dei.perla.registry;

import java.util.Collection;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;

public interface Registry {

	public Fpc get(int id);

	public Collection<Fpc> getAll();

	public Collection<Fpc> getByAttribute(Collection<Attribute> with, Collection<Attribute> without);

	public void add(Fpc fpc);

	public void remove(Fpc fpc);

}
