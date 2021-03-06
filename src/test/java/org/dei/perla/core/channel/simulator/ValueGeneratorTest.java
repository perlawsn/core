package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.simulator.DynamicFieldGenerator.*;
import org.dei.perla.core.channel.simulator.FieldGenerator.StaticFieldGenerator;
import org.dei.perla.core.channel.simulator.StepFieldGenerator.StepFloatFieldGenerator;
import org.dei.perla.core.channel.simulator.StepFieldGenerator.StepIntFieldGenerator;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class ValueGeneratorTest {

    @Test
    public void testIntValueGenerator() {
        final int min = -10;
        final int max = 2000;
        RandomIntFieldGenerator generator = new RandomIntFieldGenerator("integer", min, max);

        for (int i = 0; i < 1000; i++) {
            assertThat(
                    generator.generateValue(),
                    is(both(greaterThanOrEqualTo(min)).and(
                            lessThanOrEqualTo(max))));
        }
    }

    @Test
    public void testFloatValueGenerator() {
        final float min = -80;
        final float max = 20420;
        DynamicFloatFieldGenerator generator = new DynamicFloatFieldGenerator("float", min,
                max);

        // Accounting for errors generated by the variable precision float
        for (int i = 0; i < 1000; i++) {
            assertThat(
                    generator.generateValue(),
                    is(both(greaterThanOrEqualTo(min - 1)).and(
                            lessThanOrEqualTo(max + 1))));
        }
    }

    @Test
    public void testStringValueGenerator() {
        final int minLength = 3;
        final int maxLength = 45;
        DynamicStringFieldGenerator generator = new DynamicStringFieldGenerator("string",
                minLength, maxLength);

        for (int i = 0; i < 1000; i++) {
            assertThat(
                    generator.generateValue().length(),
                    is(both(greaterThanOrEqualTo(minLength)).and(
                            lessThanOrEqualTo(maxLength))));
        }
    }

    @Test
    public void testBooleanValueGenerator() {
        DynamicBooleanFieldGenerator generator = new DynamicBooleanFieldGenerator("boolean");

        for (int i = 0; i < 1000; i++) {
            assertThat(generator.generateValue(),
                    anyOf(equalTo(true), equalTo(false)));
        }
    }

    @Test
    public void testTimestampFieldGenerator() {
        DynamicTimestampFieldGenerator generator = new DynamicTimestampFieldGenerator("timestamp");

        Instant i = generator.generateValue();
        ZonedDateTime gen = ZonedDateTime.ofInstant(i, ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();
        assertThat(gen.getYear(), equalTo(now.getYear()));
        assertThat(gen.getMonth(), equalTo(now.getMonth()));
        assertThat(gen.getDayOfMonth(), equalTo(now.getDayOfMonth()));
    }

    @Test
    public void testStaticValueGenerator() {
        String value = "test_value";
        StaticFieldGenerator generator = new StaticFieldGenerator("static",
                value);

        for (int i = 0; i < 1000; i++) {
            assertThat(generator.generateValue(), equalTo(value));
        }
    }

    @Test
    public void testStepIntValueGenerator() {
        int min = 0;
        int max = 100;
        int increment = 3;
        StepIntFieldGenerator g = new StepIntFieldGenerator("int", min, max, increment);

        int oldVal = min;
        int newVal = g.generateValue();
        do {
            assertThat(newVal, greaterThanOrEqualTo(oldVal));
            oldVal = newVal;
            newVal = g.generateValue();
        } while(newVal < max - increment);

        for (int i = 0; i < 1000; i++) {
            assertThat(g.generateValue(), is(both(greaterThanOrEqualTo(min)).and(lessThanOrEqualTo(max))));
        }
    }

    @Test
    public void testStepFloatValueGenerator() {
        float min = 0;
        float max = 100;
        float increment = 3;
        StepFloatFieldGenerator g = new StepFloatFieldGenerator("float", min, max, increment);

        float oldVal = min;
        float newVal = g.generateValue();
        do {
            assertThat(newVal, greaterThanOrEqualTo(oldVal));
            oldVal = newVal;
            newVal = g.generateValue();
        } while(newVal < max - increment);

        for (int i = 0; i < 1000; i++) {
            assertThat(g.generateValue(), is(both(greaterThanOrEqualTo(min)).and(lessThanOrEqualTo(max))));
        }
    }

}
