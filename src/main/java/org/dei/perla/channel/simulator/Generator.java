package org.dei.perla.channel.simulator;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for a generic value-generating component.
 * 
 * Implementations of this class are used by the <code>SimulatorChannel</code>
 * to dynamically create random generated responses.
 * 
 * 
 * @author Guido Rota (2014)
 * 
 */
public class Generator {

	private final String id;
	private final FieldGenerator[] valueGeneratorArray;

	public Generator(String id, FieldGenerator[] valueGeneratorArray) {
		this.id = id;
		this.valueGeneratorArray = valueGeneratorArray;
	}

	public String getId() {
		return id;
	}

	public SimulatorPayload generateResponse() {
		Map<String, Object> resultMap = new HashMap<>();
		for (FieldGenerator valueGen : valueGeneratorArray) {
			resultMap.put(valueGen.getName(), valueGen.generateValue());
		}
		return new SimulatorPayload(resultMap);
	}

	/**
	 * Simple class for dynamically generating a record using a series of
	 * <code>ValueGenerator</code>s.
	 * 
	 * @author Guido Rota (2014)
	 *
	 */
	public abstract static class FieldGenerator {

		private String name;

		public FieldGenerator(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * Generates a new value.
		 * 
		 * @return Newly generated value
		 */
		public abstract Object generateValue();

	}

	/**
	 * Integer field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	protected static final class IntFieldGenerator extends FieldGenerator {

		private final int min;
		private final int max;

		protected IntFieldGenerator(String name, int min, int max) {
			super(name);
			this.min = min;
			this.max = max;
		}

		@Override
		public Integer generateValue() {
			return min + (int) (Math.random() * ((max - min) + 1));
		}

	}

	/**
	 * Float field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	protected static final class FloatFieldGenerator extends FieldGenerator {

		private final float min;
		private final float max;

		protected FloatFieldGenerator(String name, float min, float max) {
			super(name);
			this.min = min;
			this.max = max;
		}

		@Override
		public Float generateValue() {
			return min + (float) (Math.random() * ((max - min) + 1));
		}
	}

	/**
	 * String field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	protected static final class StringFieldGenerator extends FieldGenerator {

		private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		private final int minLength;
		private final int maxLength;

		protected StringFieldGenerator(String name, int minLength, int maxLength) {
			super(name);
			this.minLength = minLength;
			this.maxLength = maxLength;
		}

		@Override
		public String generateValue() {
			StringBuffer string = new StringBuffer();

			int length = minLength
					+ (int) (Math.random() * ((maxLength - minLength) + 1));
			for (int i = 0; i < length; i++) {
				string.append(chars.charAt((int) Math.random() * chars.length()));
			}
			return string.toString();
		}

	}

	/**
	 * Boolean field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	protected static final class BooleanFieldGenerator extends FieldGenerator {

		protected BooleanFieldGenerator(String name) {
			super(name);
		}

		@Override
		public Boolean generateValue() {
			if (Math.random() < 0.5) {
				return false;
			} else
				return true;
		}

	}
	
	/**
	 * Timestamp field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 *
	 */
	protected static final class TimestampFieldGenerator extends FieldGenerator {
		
		public TimestampFieldGenerator(String name) {
			super(name);
		}
		
		@Override
		public ZonedDateTime generateValue() {
			return ZonedDateTime.now();
		}
		
	}

	/**
	 * Static field generator implementation
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	protected static final class StaticFieldGenerator extends FieldGenerator {

		private final String value;

		protected StaticFieldGenerator(String name, String value) {
			super(name);
			this.value = value;
		}

		@Override
		public String generateValue() {
			return value;
		}

	}

}
