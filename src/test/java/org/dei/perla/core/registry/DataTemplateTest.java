package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.sample.Attribute;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Guido Rota 15/04/15.
 */
public class DataTemplateTest {

    @Test
    public void testTypeWildcard() {
        DataTemplate dt = DataTemplate.create("test", TypeClass.WILDCARD);

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
    public void testPlain() {
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

}
