package org.dei.perla.core.descriptor;

import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class DataTypeTest {

    @Test
    public void testGetClass() {
        assertTrue(DataType.getClass(DataType.INTEGER) == Integer.class);
        assertTrue(DataType.getClass(DataType.FLOAT) == Float.class);
        assertTrue(DataType.getClass(DataType.BOOLEAN) == Boolean.class);
        assertTrue(DataType.getClass(DataType.STRING) == String.class);
        assertTrue(DataType.getClass(DataType.ID) == Integer.class);
        assertTrue(DataType.getClass(DataType.TIMESTAMP) == Instant.class);

        assertTrue(DataType.getJavaClass(DataType.INTEGER.getId()) == Integer.class);
        assertTrue(DataType.getJavaClass(DataType.FLOAT.getId()) == Float.class);
        assertTrue(DataType.getJavaClass(DataType.BOOLEAN.getId()) == Boolean.class);
        assertTrue(DataType.getJavaClass(DataType.STRING.getId()) == String.class);
        assertTrue(DataType.getJavaClass(DataType.ID.getId()) == Integer.class);
        assertTrue(DataType.getJavaClass(DataType.TIMESTAMP.getId()) == Instant.class);
    }

    @Test
    public void testIsPrimitive() {
        assertTrue(DataType.isPrimitive(DataType.INTEGER.getId()));
        assertTrue(DataType.isPrimitive(DataType.FLOAT.getId()));
        assertTrue(DataType.isPrimitive(DataType.BOOLEAN.getId()));
        assertTrue(DataType.isPrimitive(DataType.STRING.getId()));
        assertTrue(DataType.isPrimitive(DataType.ID.getId()));
        assertTrue(DataType.isPrimitive(DataType.TIMESTAMP.getId()));
        assertFalse(DataType.isPrimitive("test_message"));
    }

    @Test
    public void testIsMessage() {
        assertFalse(DataType.isComplex(DataType.INTEGER.getId()));
        assertFalse(DataType.isComplex(DataType.FLOAT.getId()));
        assertFalse(DataType.isComplex(DataType.BOOLEAN.getId()));
        assertFalse(DataType.isComplex(DataType.STRING.getId()));
        assertFalse(DataType.isComplex(DataType.ID.getId()));
        assertFalse(DataType.isComplex(DataType.TIMESTAMP.getId()));
        assertTrue(DataType.isComplex("test_message"));
    }

    @Test
    public void testParse() {
        assertThat(DataType.ID.parse("43"), equalTo(43));
        assertThat(DataType.INTEGER.parse("12"), equalTo(12));
        assertThat(DataType.INTEGER.parse("-32"), equalTo(-32));
        assertThat(DataType.FLOAT.parse("0.45"), equalTo(0.45f));
        assertThat(DataType.FLOAT.parse("-32.3"), equalTo(-32.3f));
        assertThat(DataType.BOOLEAN.parse("true"), equalTo(true));
        assertThat(DataType.BOOLEAN.parse("false"), equalTo(false));
        assertThat(DataType.STRING.parse("test"), equalTo("test"));
    }

}
