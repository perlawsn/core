package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Guido Rota 15/04/15.
 */
public class TypeClassTest {

    @Test
    public void testID() {
        assertTrue(TypeClass.ID.match(DataType.ID));
        assertFalse(TypeClass.ID.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.ID.match(DataType.BOOLEAN));
        assertFalse(TypeClass.ID.match(DataType.STRING));
        assertFalse(TypeClass.ID.match(DataType.INTEGER));
        assertFalse(TypeClass.ID.match(DataType.FLOAT));
    }

    @Test
    public void testTimestamp() {
        assertFalse(TypeClass.TIMESTAMP.match(DataType.ID));
        assertTrue(TypeClass.TIMESTAMP.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.BOOLEAN));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.STRING));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.INTEGER));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.FLOAT));
    }

    @Test
    public void testBoolean() {
        assertFalse(TypeClass.BOOLEAN.match(DataType.ID));
        assertFalse(TypeClass.BOOLEAN.match(DataType.TIMESTAMP));
        assertTrue(TypeClass.BOOLEAN.match(DataType.BOOLEAN));
        assertFalse(TypeClass.BOOLEAN.match(DataType.STRING));
        assertFalse(TypeClass.BOOLEAN.match(DataType.INTEGER));
        assertFalse(TypeClass.BOOLEAN.match(DataType.FLOAT));
    }

    @Test
    public void testString() {
        assertFalse(TypeClass.STRING.match(DataType.ID));
        assertFalse(TypeClass.STRING.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.STRING.match(DataType.BOOLEAN));
        assertTrue(TypeClass.STRING.match(DataType.STRING));
        assertFalse(TypeClass.STRING.match(DataType.INTEGER));
        assertFalse(TypeClass.STRING.match(DataType.FLOAT));
    }

    @Test
    public void testInteger() {
        assertFalse(TypeClass.INTEGER.match(DataType.ID));
        assertFalse(TypeClass.INTEGER.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.INTEGER.match(DataType.BOOLEAN));
        assertFalse(TypeClass.INTEGER.match(DataType.STRING));
        assertTrue(TypeClass.INTEGER.match(DataType.INTEGER));
        assertFalse(TypeClass.INTEGER.match(DataType.FLOAT));
    }

    @Test
    public void testFloat() {
        assertFalse(TypeClass.FLOAT.match(DataType.ID));
        assertFalse(TypeClass.FLOAT.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.FLOAT.match(DataType.BOOLEAN));
        assertFalse(TypeClass.FLOAT.match(DataType.STRING));
        assertFalse(TypeClass.FLOAT.match(DataType.INTEGER));
        assertTrue(TypeClass.FLOAT.match(DataType.FLOAT));
    }

    @Test
    public void testWildcard() {
        assertTrue(TypeClass.WILDCARD.match(DataType.ID));
        assertTrue(TypeClass.WILDCARD.match(DataType.TIMESTAMP));
        assertTrue(TypeClass.WILDCARD.match(DataType.BOOLEAN));
        assertTrue(TypeClass.WILDCARD.match(DataType.STRING));
        assertTrue(TypeClass.WILDCARD.match(DataType.INTEGER));
        assertTrue(TypeClass.WILDCARD.match(DataType.FLOAT));
    }

}
