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
        assertTrue(TypeClass.ID.contains(DataType.ID));
        assertFalse(TypeClass.ID.contains(DataType.TIMESTAMP));
        assertFalse(TypeClass.ID.contains(DataType.BOOLEAN));
        assertFalse(TypeClass.ID.contains(DataType.STRING));
        assertFalse(TypeClass.ID.contains(DataType.INTEGER));
        assertFalse(TypeClass.ID.contains(DataType.FLOAT));
    }

    @Test
    public void testTimestamp() {
        assertFalse(TypeClass.TIMESTAMP.contains(DataType.ID));
        assertTrue(TypeClass.TIMESTAMP.contains(DataType.TIMESTAMP));
        assertFalse(TypeClass.TIMESTAMP.contains(DataType.BOOLEAN));
        assertFalse(TypeClass.TIMESTAMP.contains(DataType.STRING));
        assertFalse(TypeClass.TIMESTAMP.contains(DataType.INTEGER));
        assertFalse(TypeClass.TIMESTAMP.contains(DataType.FLOAT));
    }

    @Test
    public void testBoolean() {
        assertFalse(TypeClass.BOOLEAN.contains(DataType.ID));
        assertFalse(TypeClass.BOOLEAN.contains(DataType.TIMESTAMP));
        assertTrue(TypeClass.BOOLEAN.contains(DataType.BOOLEAN));
        assertFalse(TypeClass.BOOLEAN.contains(DataType.STRING));
        assertFalse(TypeClass.BOOLEAN.contains(DataType.INTEGER));
        assertFalse(TypeClass.BOOLEAN.contains(DataType.FLOAT));
    }

    @Test
    public void testString() {
        assertFalse(TypeClass.STRING.contains(DataType.ID));
        assertFalse(TypeClass.STRING.contains(DataType.TIMESTAMP));
        assertFalse(TypeClass.STRING.contains(DataType.BOOLEAN));
        assertTrue(TypeClass.STRING.contains(DataType.STRING));
        assertFalse(TypeClass.STRING.contains(DataType.INTEGER));
        assertFalse(TypeClass.STRING.contains(DataType.FLOAT));
    }

    @Test
    public void testInteger() {
        assertFalse(TypeClass.INTEGER.contains(DataType.ID));
        assertFalse(TypeClass.INTEGER.contains(DataType.TIMESTAMP));
        assertFalse(TypeClass.INTEGER.contains(DataType.BOOLEAN));
        assertFalse(TypeClass.INTEGER.contains(DataType.STRING));
        assertTrue(TypeClass.INTEGER.contains(DataType.INTEGER));
        assertFalse(TypeClass.INTEGER.contains(DataType.FLOAT));
    }

    @Test
    public void testFloat() {
        assertFalse(TypeClass.FLOAT.contains(DataType.ID));
        assertFalse(TypeClass.FLOAT.contains(DataType.TIMESTAMP));
        assertFalse(TypeClass.FLOAT.contains(DataType.BOOLEAN));
        assertFalse(TypeClass.FLOAT.contains(DataType.STRING));
        assertFalse(TypeClass.FLOAT.contains(DataType.INTEGER));
        assertTrue(TypeClass.FLOAT.contains(DataType.FLOAT));
    }

    @Test
    public void testWildcard() {
        assertTrue(TypeClass.WILDCARD.contains(DataType.ID));
        assertTrue(TypeClass.WILDCARD.contains(DataType.TIMESTAMP));
        assertTrue(TypeClass.WILDCARD.contains(DataType.BOOLEAN));
        assertTrue(TypeClass.WILDCARD.contains(DataType.STRING));
        assertTrue(TypeClass.WILDCARD.contains(DataType.INTEGER));
        assertTrue(TypeClass.WILDCARD.contains(DataType.FLOAT));
    }

}
