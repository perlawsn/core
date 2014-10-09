package org.dei.perla.fpc.base;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.engine.Record;

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
	public Collection<? extends Attribute> attributes();

	/**
	 * Function for processing a {@code Record}. New fields must be added to the
	 * {@code Map} passed as parameter.
	 * 
	 * @param original
	 *            Original source {@link Record}
	 * @param recordMap
	 *            {@code Map} where to add new fields created by this
	 *            {@code RecordModifier}
	 */
	public void process(Record source, Map<String, Object> recordMap);

	/**
	 * {@code RecordModifier} for adding a Timestamp field
	 * 
	 * @author Guido Rota (2014)
	 *
	 */
	public static class TimestampAppender implements RecordModifier {

		private static final Set<Attribute> attributeSet;

		static {
			Set<Attribute> tsAttSet = new HashSet<>();
			tsAttSet.add(BaseFpc.TIMESTAMP_ATTRIBUTE);
			attributeSet = Collections.unmodifiableSet(tsAttSet);
		}

		@Override
		public Collection<? extends Attribute> attributes() {
			return attributeSet;
		}

		@Override
		public void process(Record source, Map<String, Object> recordMap) {
			recordMap.put("timestamp", ZonedDateTime.now());
		}

	}

	/**
	 * {@code RecordModifier} for adding fields with static values
	 * 
	 * @author Guido Rota (2014)
	 *
	 */
	public static class StaticAppender implements RecordModifier {

		private final Collection<StaticAttribute> attributes;
		private final Map<String, Object> attributeMap;

		public StaticAppender(StaticAttribute attribute) {
			attributes = new LinkedList<StaticAttribute>();
			attributes.add(attribute);
			attributeMap = new HashMap<>();
			attributeMap.put(attribute.getId(), attribute.getValue());
		}

		public StaticAppender(Collection<StaticAttribute> attributes) {
			this.attributes = attributes;
			attributeMap = new HashMap<>();
			attributes.forEach(a -> attributeMap.put(a.getId(), a.getValue()));
		}

		@Override
		public Collection<? extends Attribute> attributes() {
			return attributes;
		}

		@Override
		public void process(Record source, Map<String, Object> recordMap) {
			recordMap.putAll(attributeMap);
		}

	}

}
