package org.dei.perla.core.record;

import org.dei.perla.core.descriptor.DataType;

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

    private final List<Attribute> fields;
    private final Object[] values;

    public Record(List<Attribute> fields, Object[] values) {
        this.fields = Collections.unmodifiableList(fields);
        this.values = values;
    }

    public static Record from(Map<Attribute, Object> entries) {
        Attribute[] fields = new Attribute[entries.size()];
        Object[] values = new Object[entries.size()];

        int i = 0;
        for (Map.Entry<Attribute, Object> e : entries.entrySet()) {
            fields[i] = e.getKey();
            values[i] = e.getValue();
            i++;
        }

        return new Record(Arrays.asList(fields), values);
    }

    private int getIndex(String name) {
        int idx = 0;
        for (Attribute a : fields) {
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
     * Returns the list of fields contained within the {@code Record}.
     *
     * The implementation of the PerLa Middleware ensures the consistency
     * between the order of the {@link Attributes} returned in this list and
     * their respective values in the field array.
     */
    public List<Attribute> fields() {
        return fields;
    }

	/**
	 * Returns the array containing all fields in the
	 * {@code Record}
	 *
	 * @return Set of fields in the <code>Record</code>
	 */
	public Object[] values() {
        return values;
    }

	/**
	 * Returns the value of a field contained in the {@code Record}.
     *
	 * @param name field name
	 * @return Field value
	 */
	public Object getValue(String name) {
        int idx = getIndex(name);
        if (idx == -1) {
            return null;
        }
        return values[idx];
    }

    public DataType getType(String name) {
        int idx = getIndex(name);
        if (idx == -1) {
            return null;
        }
        return fields.get(idx).getType();
    }

	/**
	 * Indicates whether the {@code record} is empty (i.e., no fields) or not.
	 *
	 * @return true if the {@code Record} does not contain any field, false
	 *         otherwise
	 */
	public boolean isEmpty() {
        return fields.isEmpty();
    }

}