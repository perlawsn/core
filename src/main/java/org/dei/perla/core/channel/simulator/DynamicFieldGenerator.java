package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.simulator.GeneratorFieldDescriptor.GeneratorFieldStrategy;

import java.time.ZonedDateTime;

/**
 * Abstract class for generating random field values
 *
 * Guido Rota 10/10/14.
 */
public abstract class DynamicFieldGenerator extends FieldGenerator {

    public DynamicFieldGenerator(String name) {
        super(name, GeneratorFieldStrategy.DYNAMIC);
    }

    /**
     * Integer field generator implementation
     *
     * @author Guido Rota (2014)
     */
    protected static final class RandomIntFieldGenerator extends DynamicFieldGenerator {
        private final int min;
        private final int max;

        protected RandomIntFieldGenerator(String name, int min, int max) {
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
     */
    protected static final class DynamicFloatFieldGenerator extends DynamicFieldGenerator {

        private final float min;
        private final float max;

        protected DynamicFloatFieldGenerator(String name, float min, float max) {
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
     */
    protected static final class DynamicStringFieldGenerator extends DynamicFieldGenerator {

        private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        private final int minLength;
        private final int maxLength;

        protected DynamicStringFieldGenerator(String name, int minLength, int maxLength) {
            super(name);
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        @Override
        public String generateValue() {
            StringBuilder string = new StringBuilder();

            int length = minLength
                    + (int) (Math.random() * ((maxLength - minLength) + 1));
            for (int i = 0; i < length; i++) {
                string.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            return string.toString();
        }

    }

    /**
     * Boolean field generator implementation
     *
     * @author Guido Rota (2014)
     */
    protected static final class DynamicBooleanFieldGenerator extends DynamicFieldGenerator {

        protected DynamicBooleanFieldGenerator(String name) {
            super(name);
        }

        @Override
        public Boolean generateValue() {
            return Math.random() >= 0.5;
        }

    }

    /**
     * Timestamp field generator implementation
     *
     * @author Guido Rota (2014)
     */
    protected static final class DynamicTimestampFieldGenerator extends DynamicFieldGenerator {

        public DynamicTimestampFieldGenerator(String name) {
            super(name);
        }

        @Override
        public ZonedDateTime generateValue() {
            return ZonedDateTime.now();
        }

    }

}
