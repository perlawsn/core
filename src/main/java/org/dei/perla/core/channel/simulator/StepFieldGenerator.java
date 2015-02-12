package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.simulator.GeneratorFieldDescriptor.GeneratorFieldStrategy;

/**
 * Abstract class for generating field values between a minimum and a maximum
 * figures, in increments defined by the user.
 *
 * @author Guido Rota 10/10/14.
 */
public abstract class StepFieldGenerator extends FieldGenerator {

    public StepFieldGenerator(String name) {
        super(name, GeneratorFieldStrategy.STEP);
    }

    public static class StepIntFieldGenerator extends StepFieldGenerator {

        private int min;
        private int max;
        private int increment;

        private int sign;
        private int value;

        public StepIntFieldGenerator(String name, int min, int max, int increment) {
            super(name);
            this.min = min;
            this.max = max;
            this.increment = increment;
            sign = 1;
            value = min;
        }

        @Override
        public Integer generateValue() {
            value += sign * increment;
            if (value > max || value < min) {
                sign = -1 * sign;
                value += sign * increment;
            }
            return value;
        }
    }

    public static class StepFloatFieldGenerator extends StepFieldGenerator {

        private float min;
        private float max;
        private float increment;

        private float sign;
        private float value;

        public StepFloatFieldGenerator(String name, float min, float max, float increment) {
            super(name);
            this.min = min;
            this.max = max;
            this.increment = increment;
            sign = 1;
            value = min;
        }

        @Override
        public Float generateValue() {
            value += sign * increment;
            if (value > max || value < min) {
                sign = -1 * sign;
                value += sign * increment;
            }
            return value;
        }
    }

}
