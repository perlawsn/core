package org.dei.perla.core.record;

import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.DataType;

import java.util.HashMap;
import java.util.Map;

public final class Attribute implements Comparable<Attribute> {

    private static Map<String, Attribute> cache = new HashMap<>();

	private final String id;
	private final DataType type;

    public static Attribute ID = Attribute.create("id", DataType.ID);
    public static Attribute TIMESTAMP =
            Attribute.create("timestamp", DataType.TIMESTAMP);

    private Attribute(String id, DataType type) {
        this.id = id;
        this.type = type;
    }

    public static Attribute create(AttributeDescriptor d) {
       return create(d.getId(), d.getType());
    }

    public static Attribute create(String id, DataType type) {
        String cid = id + type.toString();
        Attribute a = cache.get(cid);
        if (a == null) {
            a = new Attribute(id, type);
            cache.put(cid, a);
        }
        return a;
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

        // Direct reference comparison. This can be performed since all
        // attributes are cached and interned.
        return this == (Attribute) obj;
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
