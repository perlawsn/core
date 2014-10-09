package org.dei.perla.fpc;

import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.DataType;

public class Attribute implements Comparable<Attribute> {

	private final String id;
	private final DataType type;
	
	public Attribute(AttributeDescriptor descriptor) {
		this(descriptor.getId(), descriptor.getType());
	}

	public Attribute(String id, DataType type) {
		this.id = id;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public DataType getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute)) {
			return false;
		}

		Attribute att = (Attribute) obj;
		if (!id.equals(att.id) || type != att.type) {
			return false;
		}

		return true;
	}
	
	@Override
	public int hashCode() {
        return (id + type.toString()).hashCode();
    }

	@Override
	public String toString() {
		return "Attribute[id: " + id + ", type: " + type + "]";
	}

	@Override
	public int compareTo(Attribute o) {
		int idComparison = id.compareTo(o.id);
		if (idComparison == 0) {
			type.compareTo(o.type);
		}
		
		return idComparison;
	}
	
}
