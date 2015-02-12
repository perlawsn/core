package org.dei.perla.fpc.engine;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple {@code Record} object that maps field names with field values.
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class Record {

	/**
	 * Empty immutable record
	 */
	public static final Record EMPTY = new EmptyRecord();

	/**
	 * Creates a new {@code Record} instance from a map of field values
	 *
	 * @param fieldMap
	 *            Map containing field values and names
	 * @return New record instance
	 */
	public static Record from(Map<String, Object> fieldMap) {
		if (fieldMap.isEmpty()) {
			return EMPTY;
		}
		return new MapRecord(fieldMap);
	}

	/**
	 * Checks whether the {@code Record} contains the specified field
	 *
	 * @param name
	 *            Field name
	 * @return true if the record contains the field, false otherwise
	 */
	public abstract boolean hasField(String name);

	/**
	 * Returns a <code>Set</code> with all field names in the
	 * <code>Record</code>
	 *
	 * @return Set of fields in the <code>Record</code>
	 */
	public abstract Collection<Field> fields();

	/**
	 * Returns the value of a field contained in the <code>Record</code>
	 *
	 * @param field
	 *            Field name
	 * @return Field value
	 */
	public abstract Object get(String field);

	/**
	 * Indicates whether the {@code record} is empty (i.e., no fields) or not.
	 *
	 * @return true if the {@code Record} does not contain any field, false
	 *         otherwise
	 */
	public abstract boolean isEmpty();

	/**
	 * <p>
	 * Creates a new {@code Record} containing all field mappings from the
	 * current record and the record passed as parameter. This method does not
	 * alter the content of the source records.
	 * </p>
	 *
	 * <p>
	 * In case of field name collisions, the resulting object will contain the
	 * field value present in the record passed as parameter.
	 * </p>
	 *
	 * @param source
	 *            {@code Record} to be merged
	 * @return New record instance resulting from the merge of the current
	 *         record and the record passed as parameter
	 */
	public Record merge(Record source) {
		return Record.merge(this, source);
	}

	/**
	 * <p>
	 * Creates a new {@code Record} containing all field mappings from two
	 * records passed as parameter. This method does not alter the content of
	 * the source records.
	 * </p>
	 *
	 * <p>
	 * In case of field name collisions, the resulting object will contain the
	 * field value present in the second record (r2).
	 * </p>
	 *
	 * @param r1
	 *            First record to be merged
	 * @param r2
	 *            Second record to be merged
	 * @return New record instance resulting from the merge of the two record
	 *         passed as parameter
	 */
	public static Record merge(Record r1, Record r2) {
		if (r1.isEmpty()) {
			return r2;
		} else if (r2.isEmpty()) {
			return r1;
		}

		List<Field> mergedList = new ArrayList<>();
		r1.fields().forEach(mergedList::add);
		r2.fields().forEach(mergedList::add);
		return new MapRecord(mergedList);
	}

	/**
	 * An empty {@code Record}
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class EmptyRecord extends Record {

		private EmptyRecord() {
		}

		@Override
		public boolean hasField(String name) {
			return false;
		}

		@Override
		public Collection<Field> fields() {
			return Collections.emptyList();
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Object get(String field) {
			return null;
		}

	}

	/**
	 * A {@code Record} backed by a Map
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class MapRecord extends Record {

		private final Map<String, Field> fieldMap;

		private MapRecord(List<Field> fieldList) {
			this.fieldMap = new HashMap<>();
			fieldList.stream().forEach(f -> fieldMap.put(f.name, f));
		}

		private MapRecord(Map<String, Object> fieldMap) {
			this.fieldMap = new HashMap<>();
			for (Entry<String, Object> e : fieldMap.entrySet()) {
				Field f = new Field(e.getKey(), e.getValue());
				this.fieldMap.put(f.name, f);
			}
		}

		@Override
		public boolean hasField(String name) {
			return fieldMap.containsKey(name);
		}

		@Override
		public Collection<Field> fields() {
			return Collections.unmodifiableCollection(fieldMap.values());
		}

		@Override
		public boolean isEmpty() {
			return fieldMap.isEmpty();
		}

		@Override
		public Object get(String field) {
			Field f = fieldMap.get(field);
			return f == null ? null : f.value;
		}

	}

	/**
	 * Class modelling a single {@code Field} of a {@link Record}
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class Field {

		private final String name;
		private final Object value;

		private Field(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Name of the {@code Field}
		 *
		 * @return Field name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Value of the {@code Field}
		 *
		 * @return Field value
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Value of the {@code Field} cast to {@link Integer}
		 *
		 * @return Field value cast to {@link Integer}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type {@link Integer}
		 */
		public Integer getIntValue() {
			if (!(value instanceof Integer)) {
				throw new ClassCastException();
			}
			return (Integer) value;
		}

		/**
		 * Value of the {@code Field} cast to {@link Float}
		 *
		 * @return Field value cast to {@link Float}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type {@link Float}
		 */
		public Float getFloatValue() {
			if (!(value instanceof Float)) {
				throw new ClassCastException();
			}
			return (Float) value;
		}

		/**
		 * Value of the {@code Field} cast to {@link Boolean}
		 *
		 * @return Field value cast to {@link Boolean}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type {@link Boolean}
		 */
		public Boolean getBooleanValue() {
			if (!(value instanceof Boolean)) {
				throw new ClassCastException();
			}
			return (Boolean) value;
		}

		/**
		 * Value of the {@code Field} cast to {@link String}
		 *
		 * @return Field value cast to {@link String}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type {@link String}
		 */
		public String getStringValue() {
			if (!(value instanceof String)) {
				throw new ClassCastException();
			}
			return (String) value;
		}

		/**
		 * Value of the {@code Field} cast to {@link Integer} (Java backing type
		 * for PerLa ID)
		 *
		 * @return Field value cast to {@link Integer}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type {@link Integer}
		 */
		public Integer getIdValue() {
			if (!(value instanceof Integer)) {
				throw new ClassCastException();
			}
			return (Integer) value;
		}

		/**
		 * Value of the {@code Field} cast to {@link ZonedDateTime} (Java
		 * backing type for PerLa TIMESTAMP)
		 *
		 * @return Field value cast to {@link ZonedDateTime}
		 * @throws ClassCastException
		 *             If the {@code Field} value is not of type
		 *             {@link ZonedDateTime}
		 */
		public ZonedDateTime getTimestampValue() {
			if (!(value instanceof ZonedDateTime)) {
				throw new ClassCastException();
			}
			return (ZonedDateTime) value;
		}

	}

}
