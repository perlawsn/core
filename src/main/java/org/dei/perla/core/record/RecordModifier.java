package org.dei.perla.core.record;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * A class implementing a single processing operation to be performed on a
 * {@code Record}. {@code RecordModifier} are usually employed to add new fields
 * to an existing {@link Record}.
 *
 * @author Guido Rota (2014)
 *
 */
public interface RecordModifier {

	/**
	 * Returns a collection of {@link Attribute}s that this
	 * {@code RecordModifier} will add to the {@link Record}s
	 *
	 * @return Collection of {@link Attribute}s
	 */
	public List<Attribute> getAttributes();

	/**
	 * Function for processing a {@code Record}. New fields must be added to the
	 * {@code Map} passed as parameter.
	 *
	 * @param record
	 *            record to modify
	 */
	public void process(Object[] record, int idx);

	/**
	 * {@code RecordModifier} for adding a Timestamp field
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class TimestampAppender implements RecordModifier {

		private static final List<Attribute> atts;

		static {
            Attribute[] a = new Attribute[]{Attribute.TIMESTAMP_ATTRIBUTE};
            atts = Arrays.asList(a);
		}

		@Override
		public List<Attribute> getAttributes() {
			return atts;
		}

		@Override
		public void process(Object[] record, int idx) {
            record[idx] = ZonedDateTime.now();
		}

	}

	/**
	 * {@code RecordModifier} for adding fields with static values
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class StaticAppender implements RecordModifier {

		private final List<Attribute> atts;
        private final Object[] values;

		public StaticAppender(LinkedHashMap<Attribute, Object> am) {
			atts = new ArrayList<>(am.size());
            values = new Object[am.size()];
            int i = 0;
            for (Map.Entry<Attribute, Object> e : am.entrySet()) {
                atts.add(e.getKey());
                values[i] = e.getValue();
                i++;
            }
		}

		@Override
		public List<Attribute> getAttributes() {
			return atts;
		}

		@Override
		public void process(Object[] record, int idx) {
            for (int i = 0; i < values.length; i++) {
                record[idx + i] = values[i];
            }
		}

	}

}
