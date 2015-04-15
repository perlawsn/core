package org.dei.perla.core.registry;

import org.dei.perla.core.descriptor.DataType;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 15/04/15.
 */
public class TypeClassTest {

    @Test
    public void testMatchID() {
        assertTrue(TypeClass.ID.match(DataType.ID));
        assertFalse(TypeClass.ID.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.ID.match(DataType.BOOLEAN));
        assertFalse(TypeClass.ID.match(DataType.STRING));
        assertFalse(TypeClass.ID.match(DataType.INTEGER));
        assertFalse(TypeClass.ID.match(DataType.FLOAT));
    }

    @Test
    public void testMatchTimestamp() {
        assertFalse(TypeClass.TIMESTAMP.match(DataType.ID));
        assertTrue(TypeClass.TIMESTAMP.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.BOOLEAN));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.STRING));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.INTEGER));
        assertFalse(TypeClass.TIMESTAMP.match(DataType.FLOAT));
    }

    @Test
    public void testMatchBoolean() {
        assertFalse(TypeClass.BOOLEAN.match(DataType.ID));
        assertFalse(TypeClass.BOOLEAN.match(DataType.TIMESTAMP));
        assertTrue(TypeClass.BOOLEAN.match(DataType.BOOLEAN));
        assertFalse(TypeClass.BOOLEAN.match(DataType.STRING));
        assertFalse(TypeClass.BOOLEAN.match(DataType.INTEGER));
        assertFalse(TypeClass.BOOLEAN.match(DataType.FLOAT));
    }

    @Test
    public void testMatchString() {
        assertFalse(TypeClass.STRING.match(DataType.ID));
        assertFalse(TypeClass.STRING.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.STRING.match(DataType.BOOLEAN));
        assertTrue(TypeClass.STRING.match(DataType.STRING));
        assertFalse(TypeClass.STRING.match(DataType.INTEGER));
        assertFalse(TypeClass.STRING.match(DataType.FLOAT));
    }

    @Test
    public void testMatchInteger() {
        assertFalse(TypeClass.INTEGER.match(DataType.ID));
        assertFalse(TypeClass.INTEGER.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.INTEGER.match(DataType.BOOLEAN));
        assertFalse(TypeClass.INTEGER.match(DataType.STRING));
        assertTrue(TypeClass.INTEGER.match(DataType.INTEGER));
        assertFalse(TypeClass.INTEGER.match(DataType.FLOAT));
    }

    @Test
    public void testMatchFloat() {
        assertFalse(TypeClass.FLOAT.match(DataType.ID));
        assertFalse(TypeClass.FLOAT.match(DataType.TIMESTAMP));
        assertFalse(TypeClass.FLOAT.match(DataType.BOOLEAN));
        assertFalse(TypeClass.FLOAT.match(DataType.STRING));
        assertFalse(TypeClass.FLOAT.match(DataType.INTEGER));
        assertTrue(TypeClass.FLOAT.match(DataType.FLOAT));
    }

    @Test
    public void testMatchWildcard() {
        assertTrue(TypeClass.ANY.match(DataType.ID));
        assertTrue(TypeClass.ANY.match(DataType.TIMESTAMP));
        assertTrue(TypeClass.ANY.match(DataType.BOOLEAN));
        assertTrue(TypeClass.ANY.match(DataType.STRING));
        assertTrue(TypeClass.ANY.match(DataType.INTEGER));
        assertTrue(TypeClass.ANY.match(DataType.FLOAT));
    }

    @Test
    public void testCompareMatchTimestamp() {
        int res = TypeClass.TIMESTAMP.compareMatch(DataType.FLOAT);
        assertThat(res, greaterThan(0));
        res = TypeClass.TIMESTAMP.compareMatch(DataType.INTEGER);
        assertThat(res, greaterThan(0));
        res = TypeClass.TIMESTAMP.compareMatch(DataType.BOOLEAN);
        assertThat(res, greaterThan(0));
        res = TypeClass.TIMESTAMP.compareMatch(DataType.STRING);
        assertThat(res, greaterThan(0));
        res = TypeClass.TIMESTAMP.compareMatch(DataType.ID);
        assertThat(res, greaterThan(0));
        res = TypeClass.TIMESTAMP.compareMatch(DataType.TIMESTAMP);
        assertThat(res, equalTo(0));
    }

    @Test
    public void testCompareMatchID() {
        int res = TypeClass.ID.compareMatch(DataType.FLOAT);
        assertThat(res, greaterThan(0));
        res = TypeClass.ID.compareMatch(DataType.INTEGER);
        assertThat(res, greaterThan(0));
        res = TypeClass.ID.compareMatch(DataType.BOOLEAN);
        assertThat(res, greaterThan(0));
        res = TypeClass.ID.compareMatch(DataType.STRING);
        assertThat(res, greaterThan(0));
        res = TypeClass.ID.compareMatch(DataType.ID);
        assertThat(res, equalTo(0));
        res = TypeClass.ID.compareMatch(DataType.TIMESTAMP);
        assertThat(res, lessThan(0));
    }

    @Test
    public void testCompareMatchString() {
        int res = TypeClass.STRING.compareMatch(DataType.FLOAT);
        assertThat(res, greaterThan(0));
        res = TypeClass.STRING.compareMatch(DataType.INTEGER);
        assertThat(res, greaterThan(0));
        res = TypeClass.STRING.compareMatch(DataType.BOOLEAN);
        assertThat(res, greaterThan(0));
        res = TypeClass.STRING.compareMatch(DataType.STRING);
        assertThat(res, equalTo(0));
        res = TypeClass.STRING.compareMatch(DataType.ID);
        assertThat(res, lessThan(0));
        res = TypeClass.STRING.compareMatch(DataType.TIMESTAMP);
        assertThat(res, lessThan(0));
    }

    @Test
    public void testCompareMatchBoolean() {
        int res = TypeClass.BOOLEAN.compareMatch(DataType.FLOAT);
        assertThat(res, greaterThan(0));
        res = TypeClass.BOOLEAN.compareMatch(DataType.INTEGER);
        assertThat(res, greaterThan(0));
        res = TypeClass.BOOLEAN.compareMatch(DataType.BOOLEAN);
        assertThat(res, equalTo(0));
        res = TypeClass.BOOLEAN.compareMatch(DataType.STRING);
        assertThat(res, lessThan(0));
        res = TypeClass.BOOLEAN.compareMatch(DataType.ID);
        assertThat(res, lessThan(0));
        res = TypeClass.BOOLEAN.compareMatch(DataType.TIMESTAMP);
        assertThat(res, lessThan(0));
    }

    @Test
    public void testCompareMatchInteger() {
        int res = TypeClass.INTEGER.compareMatch(DataType.FLOAT);
        assertThat(res, greaterThan(0));
        res = TypeClass.INTEGER.compareMatch(DataType.INTEGER);
        assertThat(res, equalTo(0));
        res = TypeClass.INTEGER.compareMatch(DataType.BOOLEAN);
        assertThat(res, lessThan(0));
        res = TypeClass.INTEGER.compareMatch(DataType.STRING);
        assertThat(res, lessThan(0));
        res = TypeClass.INTEGER.compareMatch(DataType.ID);
        assertThat(res, lessThan(0));
        res = TypeClass.INTEGER.compareMatch(DataType.TIMESTAMP);
        assertThat(res, lessThan(0));
    }

    @Test
    public void testCompareMatchFloat() {
        int res = TypeClass.FLOAT.compareMatch(DataType.FLOAT);
        assertThat(res, equalTo(0));
        res = TypeClass.FLOAT.compareMatch(DataType.INTEGER);
        assertThat(res, lessThan(0));
        res = TypeClass.FLOAT.compareMatch(DataType.BOOLEAN);
        assertThat(res, lessThan(0));
        res = TypeClass.FLOAT.compareMatch(DataType.STRING);
        assertThat(res, lessThan(0));
        res = TypeClass.FLOAT.compareMatch(DataType.ID);
        assertThat(res, lessThan(0));
        res = TypeClass.FLOAT.compareMatch(DataType.TIMESTAMP);
        assertThat(res, lessThan(0));
    }

    @Test
    public void testCompareMatchWildcard() {
        int res = TypeClass.ANY.compareMatch(DataType.ID);
        assertThat(res, equalTo(0));
        res = TypeClass.ANY.compareMatch(DataType.TIMESTAMP);
        assertThat(res, equalTo(0));
        res = TypeClass.ANY.compareMatch(DataType.BOOLEAN);
        assertThat(res, equalTo(0));
        res = TypeClass.ANY.compareMatch(DataType.FLOAT);
        assertThat(res, equalTo(0));
        res = TypeClass.ANY.compareMatch(DataType.INTEGER);
        assertThat(res, equalTo(0));
        res = TypeClass.ANY.compareMatch(DataType.STRING);
        assertThat(res, equalTo(0));
    }

}
