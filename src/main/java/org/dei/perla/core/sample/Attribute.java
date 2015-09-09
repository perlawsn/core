package org.dei.perla.core.sample;

import org.dei.perla.core.fpc.DataType;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@code Attribute} is an object that describes a data element that can
 * be set or retrieved using an FPC.
 *
 * @author Guido Rota (2015)
 */
public final class Attribute implements Comparable<Attribute> {

    private static final Map<String, Attribute> cache = new HashMap<>();

    public static final Attribute ID =
            Attribute.create("id", DataType.ID);
    public static final Attribute TIMESTAMP =
            Attribute.create("timestamp", DataType.TIMESTAMP);

    private final String id;
    private final DataType type;

    private Attribute(String id, DataType type) {
        this.id = id;
        this.type = type;
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

    /**
     * Returns the {@code Attribute} identifier
     *
     * @return {@code Attribute} identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the {@code Attribute} type
     *
     * @return {@code Attribute} type
     */
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

    /**
     * Matches the current attribute with another one. Matching consists in
     * equalling the ID, and matching the attributes' data types. See the
     * {@code DataType.match()} method for further information.
     *
     * @param a {@code Attribute} to match
     * @return true if the two attributes match, false otherwise
     */
    public boolean match(Attribute a) {
        if (!a.getId().equals(id)) {
            return false;
        }

        if (!type.match(a.getType())) {
            return false;
        }

        return true;
    }

    /**
     * Similar to {@code match}, provides an additional ordering between
     * types. Attribute order is computed using the attributes' ids first, and
     * then compare-matching the attributes' data types.
     *
     * @param o
     * @return 0 if the attributes are equal or match, < 0 if the current
     * object is lower than the other, > 0 otherwise.
     */
    public int compareMatch(Attribute a) {
        int c = id.compareTo(a.getId());
        if (c == 0) {
            c = type.compareMatch(a.getType());
        }
        return c;
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
