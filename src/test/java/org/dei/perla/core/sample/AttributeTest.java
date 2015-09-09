package org.dei.perla.core.sample;

import org.dei.perla.core.fpc.DataType;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 12/02/15.
 */
public class AttributeTest {

    @Test
    public void creationTest() {
        Attribute a1 = Attribute.create("test", DataType.FLOAT);
        assertThat(a1, notNullValue());
        assertThat(a1.getId(), equalTo("test"));
        assertThat(a1.getType(), equalTo(DataType.FLOAT));

        Attribute a2 = Attribute.create("test", DataType.FLOAT);
        assertThat(a2, notNullValue());
        assertThat(a1.getId(), equalTo("test"));
        assertThat(a1.getType(), equalTo(DataType.FLOAT));
    }

    @Test
    public void equality() {
        Attribute a1 = Attribute.create("test", DataType.FLOAT);
        Attribute a2 = Attribute.create("test", DataType.FLOAT);
        Attribute a3 = Attribute.create("other", DataType.FLOAT);
        Attribute a4 = Attribute.create("test", DataType.INTEGER);

        assertThat(a1, equalTo(a1));
        assertThat(a2, equalTo(a2));
        assertThat(a3, equalTo(a3));
        assertThat(a4, equalTo(a4));

        assertThat(a1, equalTo(a2));
        assertThat(a2, equalTo(a1));

        assertThat(a1, not(equalTo(a3)));
        assertThat(a2, not(equalTo(a3)));

        assertThat(a1, not(equalTo(a4)));
        assertThat(a2, not(equalTo(a4)));

        assertThat(a3, not(equalTo(a4)));
        assertThat(a4, not(equalTo(a3)));
    }

    @Test
    public void match() {
        Attribute intA = Attribute.create("testA", DataType.INTEGER);
        Attribute intB = Attribute.create("testB", DataType.INTEGER);
        Attribute floatA = Attribute.create("testA", DataType.FLOAT);
        Attribute floatB = Attribute.create("testB", DataType.FLOAT);
        Attribute boolA = Attribute.create("testA", DataType.BOOLEAN);
        Attribute boolB = Attribute.create("testB", DataType.BOOLEAN);
        Attribute numA = Attribute.create("testA", DataType.NUMERIC);
        Attribute anyA = Attribute.create("testA", DataType.ANY);

        assertTrue(numA.match(intA));
        assertFalse(numA.match(intB));
        assertTrue(numA.match(floatA));
        assertFalse(numA.match(floatB));
        assertFalse(numA.match(boolA));
        assertTrue(anyA.match(intA));
        assertFalse(anyA.match(intB));
        assertTrue(anyA.match(floatA));
        assertFalse(anyA.match(floatB));
        assertTrue(anyA.match(boolA));
        assertFalse(anyA.match(boolB));
    }

    @Test
    public void compareMatch() {
        Attribute intA = Attribute.create("testA", DataType.INTEGER);
        Attribute intB = Attribute.create("testB", DataType.INTEGER);
        Attribute floatA = Attribute.create("testA", DataType.FLOAT);
        Attribute floatB = Attribute.create("testB", DataType.FLOAT);
        Attribute boolA = Attribute.create("testA", DataType.BOOLEAN);
        Attribute boolB = Attribute.create("testB", DataType.BOOLEAN);
        Attribute numA = Attribute.create("testA", DataType.NUMERIC);
        Attribute anyA = Attribute.create("testA", DataType.ANY);

        assertThat(numA.compareMatch(intA), equalTo(0));
        assertThat(numA.compareMatch(floatA), equalTo(0));
        assertThat(numA.compareMatch(intB), lessThan(0));
        assertThat(numA.compareMatch(floatB), lessThan(0));
        assertThat(numA.compareMatch(boolA), lessThan(0));
        assertThat(anyA.compareMatch(intA), equalTo(0));
        assertThat(anyA.compareMatch(intB), lessThan(0));
        assertThat(anyA.compareMatch(boolA), equalTo(0));
    }

}
