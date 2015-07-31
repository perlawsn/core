package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.sample.Attribute;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 15/04/15.
 */
public class DataTemplateTest {

    @Test
    public void testTypeWildcardMatch() {
        DataTemplate dt = DataTemplate.create("test", TypeClass.ANY);

        Attribute a = Attribute.create("test", DataType.ID);
        assertTrue(dt.match(a));
        a = Attribute.create("test", DataType.TIMESTAMP);
        assertTrue(dt.match(a));
        a = Attribute.create("test", DataType.INTEGER);
        assertTrue(dt.match(a));
        a = Attribute.create("test", DataType.FLOAT);
        assertTrue(dt.match(a));
        a = Attribute.create("test", DataType.BOOLEAN);
        assertTrue(dt.match(a));
        a = Attribute.create("test", DataType.STRING);
        assertTrue(dt.match(a));

        a = Attribute.create("wrong", DataType.ID);
        assertFalse(dt.match(a));
        a = Attribute.create("wrong", DataType.TIMESTAMP);
        assertFalse(dt.match(a));
        a = Attribute.create("wrong", DataType.INTEGER);
        assertFalse(dt.match(a));
        a = Attribute.create("wrong", DataType.FLOAT);
        assertFalse(dt.match(a));
        a = Attribute.create("wrong", DataType.BOOLEAN);
        assertFalse(dt.match(a));
        a = Attribute.create("wrong", DataType.STRING);
        assertFalse(dt.match(a));
    }

    @Test
    public void testPlainMatch() {
        DataTemplate dt = DataTemplate.create("test", TypeClass.ID);
        Attribute a = Attribute.create("test", DataType.ID);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.ID);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.TIMESTAMP);
        assertFalse(dt.match(a));

        dt = DataTemplate.create("test", TypeClass.TIMESTAMP);
        a = Attribute.create("test", DataType.TIMESTAMP);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.TIMESTAMP);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.ID);
        assertFalse(dt.match(a));

        dt = DataTemplate.create("test", TypeClass.INTEGER);
        a = Attribute.create("test", DataType.INTEGER);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.INTEGER);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.ID);
        assertFalse(dt.match(a));

        dt = DataTemplate.create("test", TypeClass.FLOAT);
        a = Attribute.create("test", DataType.FLOAT);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.FLOAT);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.ID);
        assertFalse(dt.match(a));

        dt = DataTemplate.create("test", TypeClass.STRING);
        a = Attribute.create("test", DataType.STRING);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.STRING);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.ID);
        assertFalse(dt.match(a));

        dt = DataTemplate.create("test", TypeClass.BOOLEAN);
        a = Attribute.create("test", DataType.BOOLEAN);
        assertTrue(dt.match(a));
        a = Attribute.create("wrong", DataType.BOOLEAN);
        assertFalse(dt.match(a));
        a = Attribute.create("test", DataType.ID);
        assertFalse(dt.match(a));
    }

    @Test
    public void testWildcardAnyCompareMatch() {
        DataTemplate dt = DataTemplate.create("t", TypeClass.ANY);

        Attribute a = Attribute.create("t", DataType.INTEGER);
        assertThat(dt.compareMatch(a), equalTo(0));
        a = Attribute.create("a", DataType.FLOAT);
        assertThat(dt.compareMatch(a), greaterThan(0));
        a = Attribute.create("z", DataType.TIMESTAMP);
        assertThat(dt.compareMatch(a), lessThan(0));
    }

    @Test
    public void testWildcardNumericCompareMatch() {
        DataTemplate dt = DataTemplate.create("t", TypeClass.NUMERIC);

        Attribute a = Attribute.create("t", DataType.INTEGER);
        assertThat(dt.compareMatch(a), equalTo(0));
        a = Attribute.create("t", DataType.FLOAT);
        assertThat(dt.compareMatch(a), equalTo(0));
        a = Attribute.create("a", DataType.FLOAT);
        assertThat(dt.compareMatch(a), greaterThan(0));
        a = Attribute.create("z", DataType.TIMESTAMP);
        assertThat(dt.compareMatch(a), lessThan(0));
    }

    @Test
    public void testPlainCompareMatch() {
        DataTemplate dt = DataTemplate.create("t", TypeClass.BOOLEAN);
        Attribute a = Attribute.create("t", DataType.BOOLEAN);
        assertThat(dt.compareMatch(a), equalTo(0));
        a = Attribute.create("t", DataType.TIMESTAMP);
        assertThat(dt.compareMatch(a), lessThan(0));
        a = Attribute.create("t", DataType.FLOAT);
        assertThat(dt.compareMatch(a), greaterThan(0));
        a = Attribute.create("a", DataType.BOOLEAN);
        assertThat(dt.compareMatch(a), greaterThan(0));
        a = Attribute.create("z", DataType.BOOLEAN);
        assertThat(dt.compareMatch(a), lessThan(0));
    }

}
