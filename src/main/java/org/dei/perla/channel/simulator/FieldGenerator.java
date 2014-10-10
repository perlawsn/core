package org.dei.perla.channel.simulator;

import org.dei.perla.channel.simulator.GeneratorFieldDescriptor.GeneratorFieldStrategy;

/**
 * Abstract class for generating field values
 *
 * @author Guido Rota (2014)
 */
public abstract class FieldGenerator {

    private String name;
    private GeneratorFieldStrategy strategy;

    public FieldGenerator(String name, GeneratorFieldStrategy strategy) {
        this.name = name;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public GeneratorFieldStrategy getStrategy() {
        return strategy;
    }

    /**
     * Generates a new value.
     *
     * @return Newly generated value
     */
    public abstract Object generateValue();

    /**
     * Static field generator implementation
     *
     * @author Guido Rota (2014)
     */
    protected static final class StaticFieldGenerator extends FieldGenerator {

        private final String value;

        protected StaticFieldGenerator(String name, String value) {
            super(name, GeneratorFieldStrategy.STATIC);
            this.value = value;
        }

        @Override
        public String generateValue() {
            return value;
        }

    }

}
