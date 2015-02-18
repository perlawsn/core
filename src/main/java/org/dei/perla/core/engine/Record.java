package org.dei.perla.core.engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A simple {@code Record} object that maps field names with field values.
 *
 * @author Guido Rota (2014)
 *
 */
public class Record {

	/**
	 * Empty immutable record
	 */
	public static final Record EMPTY =
            new Record(Collections.emptyList(), new Object[0]);

    private final List<Attribute> atts;
    private final Object[] fields;

    public Record(List<Attribute> atts, Object[] fields) {
        this.atts = Collections.unmodifiableList(atts);
        this.fields = fields;
    }

    public static Record from(Map<Attribute, Object> entries) {
        Attribute[] atts = new Attribute[entries.size()];
        Object[] values = new Object[entries.size()];

        int i = 0;
        for (Map.Entry<Attribute, Object> e : entries.entrySet()) {
            atts[i] = e.getKey();
            values[i] = e.getValue();
            i++;
        }

        return new Record(Arrays.asList(atts), values);
    }

    private int getIndex(String name) {
        int idx = 0;
        for (Attribute a : atts) {
            if (a.getId().equals(name)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

	/**
	 * Checks whether the {@code Record} contains the specified field
	 *
	 * @param name
	 *            Field name
	 * @return true if the record contains the field, false otherwise
	 */
	public boolean hasField(String name) {
        return getIndex(name) != -1;
    }

    /**
     * Returns the list of {@link Attribute} objects associated with the
     * {@code Record}. The implementation of the PerLa Middleware ensures
     * the consistency between the order of the {@link Attributes} returned
     * in this list and their respective values in the field array.
     */
    public List<Attribute> getAttributes() {
        return atts;
    }

	/**
	 * Returns the array containing all fields in the
	 * {@code Record}
	 *
	 * @return Set of fields in the <code>Record</code>
	 */
	public Object[] getFields() {
        return fields;
    }

	/**
	 * Returns the value of a field contained in the {@code Record}.
     *
	 * @param name
	 *            {@link Attribute} name
	 * @return Field value
	 */
	public Object get(String name) {
        int idx = getIndex(name);
        if (idx == -1) {
            return null;
        }
        return fields[idx];
    }

	/**
	 * Indicates whether the {@code record} is empty (i.e., no fields) or not.
	 *
	 * @return true if the {@code Record} does not contain any field, false
	 *         otherwise
	 */
	public boolean isEmpty() {
        return atts.isEmpty();
    }

}
