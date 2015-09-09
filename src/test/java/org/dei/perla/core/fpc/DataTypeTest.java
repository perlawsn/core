package org.dei.perla.core.fpc;

import org.dei.perla.core.fpc.DataType.ConcreteType;
import org.junit.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 08/09/15.
 */
public class DataTypeTest {

    @Test
    public void testParameters() {
        assertThat(DataType.ANY.getId(), equalTo("any"));
        assertThat(DataType.ANY.ordinal(), equalTo(0));
        assertFalse(DataType.ANY.isConcrete());

        assertThat(DataType.NUMERIC.getId(), equalTo("numeric"));
        assertThat(DataType.NUMERIC.ordinal(), equalTo(1));
        assertFalse(DataType.NUMERIC.isConcrete());

        assertThat(DataType.ID.getId(), equalTo("id"));
        assertThat(DataType.ID.ordinal(), equalTo(2));
        assertThat(DataType.ID.getJavaClass(), equalTo(Integer.class));
        assertTrue(DataType.ID.isConcrete());

        assertThat(DataType.INTEGER.getId(), equalTo("integer"));
        assertThat(DataType.INTEGER.ordinal(), equalTo(3));
        assertThat(DataType.INTEGER.getJavaClass(), equalTo(Integer.class));
        assertTrue(DataType.INTEGER.isConcrete());

        assertThat(DataType.FLOAT.getId(), equalTo("float"));
        assertThat(DataType.FLOAT.ordinal(), equalTo(4));
        assertThat(DataType.FLOAT.getJavaClass(), equalTo(Float.class));
        assertTrue(DataType.FLOAT.isConcrete());

        assertThat(DataType.STRING.getId(), equalTo("string"));
        assertThat(DataType.STRING.ordinal(), equalTo(5));
        assertThat(DataType.STRING.getJavaClass(), equalTo(String.class));
        assertTrue(DataType.STRING.isConcrete());

        assertThat(DataType.BOOLEAN.getId(), equalTo("boolean"));
        assertThat(DataType.BOOLEAN.ordinal(), equalTo(6));
        assertThat(DataType.BOOLEAN.getJavaClass(), equalTo(Boolean.class));
        assertTrue(DataType.BOOLEAN.isConcrete());

        assertThat(DataType.TIMESTAMP.getId(), equalTo("timestamp"));
        assertThat(DataType.TIMESTAMP.ordinal(), equalTo(7));
        assertThat(DataType.TIMESTAMP.getJavaClass(), equalTo(Instant.class));
        assertTrue(DataType.TIMESTAMP.isConcrete());
    }

    @Test
    public void testParse() {
        assertThat(ConcreteType.parse("id"), equalTo(DataType.ID));
        assertThat(ConcreteType.parse("integer"), equalTo(DataType.INTEGER));
        assertThat(ConcreteType.parse("float"), equalTo(DataType.FLOAT));
        assertThat(ConcreteType.parse("string"), equalTo(DataType.STRING));
        assertThat(ConcreteType.parse("boolean"), equalTo(DataType.BOOLEAN));
        assertThat(ConcreteType.parse("timestamp"), equalTo(DataType.TIMESTAMP));
    }

    @Test
    public void testValueOf() {
        assertThat(DataType.ID.valueOf("12"), equalTo(12));
        assertThat(DataType.INTEGER.valueOf("45"), equalTo(45));
        assertThat(DataType.FLOAT.valueOf("3.145"), equalTo(3.145f));
        assertThat(DataType.STRING.valueOf("test"), equalTo("test"));
        assertThat(DataType.BOOLEAN.valueOf("true"), equalTo(true));
        assertThat(DataType.BOOLEAN.valueOf("false"), equalTo(false));
    }

    @Test
    public void testIsPrimitive() {
        assertTrue(DataType.isPrimitive("id"));
        assertTrue(DataType.isPrimitive("integer"));
        assertTrue(DataType.isPrimitive("float"));
        assertTrue(DataType.isPrimitive("string"));
        assertTrue(DataType.isPrimitive("boolean"));
        assertTrue(DataType.isPrimitive("timestamp"));
    }

    @Test
    public void testMatch() {
        assertTrue(DataType.ANY.match(DataType.ID));
        assertTrue(DataType.ANY.match(DataType.INTEGER));
        assertTrue(DataType.ANY.match(DataType.FLOAT));
        assertTrue(DataType.ANY.match(DataType.STRING));
        assertTrue(DataType.ANY.match(DataType.BOOLEAN));
        assertTrue(DataType.ANY.match(DataType.TIMESTAMP));

        assertTrue(DataType.NUMERIC.match(DataType.INTEGER));
        assertTrue(DataType.NUMERIC.match(DataType.FLOAT));
        assertFalse(DataType.NUMERIC.match(DataType.ID));
        assertFalse(DataType.NUMERIC.match(DataType.STRING));
        assertFalse(DataType.NUMERIC.match(DataType.BOOLEAN));
        assertFalse(DataType.NUMERIC.match(DataType.TIMESTAMP));

        assertTrue(DataType.ID.match(DataType.ID));
        assertFalse(DataType.ID.match(DataType.INTEGER));
        assertFalse(DataType.ID.match(DataType.FLOAT));
        assertFalse(DataType.ID.match(DataType.STRING));
        assertFalse(DataType.ID.match(DataType.BOOLEAN));
        assertFalse(DataType.ID.match(DataType.TIMESTAMP));

        assertFalse(DataType.INTEGER.match(DataType.ID));
        assertTrue(DataType.INTEGER.match(DataType.INTEGER));
        assertFalse(DataType.INTEGER.match(DataType.FLOAT));
        assertFalse(DataType.INTEGER.match(DataType.STRING));
        assertFalse(DataType.INTEGER.match(DataType.BOOLEAN));
        assertFalse(DataType.INTEGER.match(DataType.TIMESTAMP));

        assertFalse(DataType.FLOAT.match(DataType.ID));
        assertFalse(DataType.FLOAT.match(DataType.INTEGER));
        assertTrue(DataType.FLOAT.match(DataType.FLOAT));
        assertFalse(DataType.FLOAT.match(DataType.STRING));
        assertFalse(DataType.FLOAT.match(DataType.BOOLEAN));
        assertFalse(DataType.FLOAT.match(DataType.TIMESTAMP));

        assertFalse(DataType.STRING.match(DataType.ID));
        assertFalse(DataType.STRING.match(DataType.INTEGER));
        assertFalse(DataType.STRING.match(DataType.FLOAT));
        assertTrue(DataType.STRING.match(DataType.STRING));
        assertFalse(DataType.STRING.match(DataType.BOOLEAN));
        assertFalse(DataType.STRING.match(DataType.TIMESTAMP));

        assertFalse(DataType.BOOLEAN.match(DataType.ID));
        assertFalse(DataType.BOOLEAN.match(DataType.INTEGER));
        assertFalse(DataType.BOOLEAN.match(DataType.FLOAT));
        assertFalse(DataType.BOOLEAN.match(DataType.STRING));
        assertTrue(DataType.BOOLEAN.match(DataType.BOOLEAN));
        assertFalse(DataType.BOOLEAN.match(DataType.TIMESTAMP));

        assertFalse(DataType.TIMESTAMP.match(DataType.ID));
        assertFalse(DataType.TIMESTAMP.match(DataType.INTEGER));
        assertFalse(DataType.TIMESTAMP.match(DataType.FLOAT));
        assertFalse(DataType.TIMESTAMP.match(DataType.STRING));
        assertFalse(DataType.TIMESTAMP.match(DataType.BOOLEAN));
        assertTrue(DataType.TIMESTAMP.match(DataType.TIMESTAMP));
    }

    @Test
    public void testCompareMatch() {
        assertThat(DataType.ANY.compareMatch(DataType.ID), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.INTEGER), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.FLOAT), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.STRING), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.BOOLEAN), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.TIMESTAMP), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.NUMERIC), equalTo(0));
        assertThat(DataType.ANY.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.NUMERIC.compareMatch(DataType.ID), lessThan(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.INTEGER), equalTo(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.FLOAT), equalTo(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.STRING), lessThan(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.BOOLEAN), lessThan(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.NUMERIC), equalTo(0));
        assertThat(DataType.NUMERIC.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.ID.compareMatch(DataType.ID), equalTo(0));
        assertThat(DataType.ID.compareMatch(DataType.INTEGER), lessThan(0));
        assertThat(DataType.ID.compareMatch(DataType.FLOAT), lessThan(0));
        assertThat(DataType.ID.compareMatch(DataType.STRING), lessThan(0));
        assertThat(DataType.ID.compareMatch(DataType.BOOLEAN), lessThan(0));
        assertThat(DataType.ID.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.ID.compareMatch(DataType.NUMERIC), greaterThan(0));
        assertThat(DataType.ID.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.INTEGER.compareMatch(DataType.ID), greaterThan(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.INTEGER), equalTo(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.FLOAT), lessThan(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.STRING), lessThan(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.BOOLEAN), lessThan(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.NUMERIC), equalTo(0));
        assertThat(DataType.INTEGER.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.FLOAT.compareMatch(DataType.ID), greaterThan(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.INTEGER), greaterThan(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.FLOAT), equalTo(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.STRING), lessThan(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.BOOLEAN), lessThan(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.NUMERIC), equalTo(0));
        assertThat(DataType.FLOAT.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.STRING.compareMatch(DataType.ID), greaterThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.INTEGER), greaterThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.FLOAT), greaterThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.STRING), equalTo(0));
        assertThat(DataType.STRING.compareMatch(DataType.BOOLEAN), lessThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.NUMERIC), greaterThan(0));
        assertThat(DataType.STRING.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.BOOLEAN.compareMatch(DataType.ID), greaterThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.INTEGER), greaterThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.FLOAT), greaterThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.STRING), greaterThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.BOOLEAN), equalTo(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.TIMESTAMP), lessThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.NUMERIC), greaterThan(0));
        assertThat(DataType.BOOLEAN.compareMatch(DataType.ANY), equalTo(0));

        assertThat(DataType.TIMESTAMP.compareMatch(DataType.ID), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.INTEGER), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.FLOAT), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.STRING), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.BOOLEAN), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.TIMESTAMP), equalTo(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.NUMERIC), greaterThan(0));
        assertThat(DataType.TIMESTAMP.compareMatch(DataType.ANY), equalTo(0));
    }

}
