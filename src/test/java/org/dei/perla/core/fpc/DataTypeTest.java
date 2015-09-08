package org.dei.perla.core.fpc;

import org.dei.perla.core.fpc.DataType.ConcreteType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

import java.time.Instant;

/**
 * @author Guido Rota 08/09/15.
 */
public class DataTypeTest {

    @Test
    public void testParameters() {
        assertThat(DataType.ANY.getId(), equalTo("any"));
        assertThat(DataType.ANY.getOrder(), equalTo(0));

        assertThat(DataType.NUMERIC.getId(), equalTo("numeric"));
        assertThat(DataType.NUMERIC.getOrder(), equalTo(1));

        assertThat(DataType.ID.getId(), equalTo("id"));
        assertThat(DataType.ID.getOrder(), equalTo(2));
        assertThat(DataType.ID.getJavaClass(), equalTo(Integer.class));

        assertThat(DataType.INTEGER.getId(), equalTo("integer"));
        assertThat(DataType.INTEGER.getOrder(), equalTo(3));
        assertThat(DataType.ID.getJavaClass(), equalTo(Integer.class));

        assertThat(DataType.FLOAT.getId(), equalTo("float"));
        assertThat(DataType.FLOAT.getOrder(), equalTo(4));
        assertThat(DataType.FLOAT.getJavaClass(), equalTo(Float.class));

        assertThat(DataType.STRING.getId(), equalTo("string"));
        assertThat(DataType.STRING.getOrder(), equalTo(5));
        assertThat(DataType.STRING.getJavaClass(), equalTo(String.class));

        assertThat(DataType.BOOLEAN.getId(), equalTo("boolean"));
        assertThat(DataType.BOOLEAN.getOrder(), equalTo(6));
        assertThat(DataType.BOOLEAN.getJavaClass(), equalTo(Boolean.class));

        assertThat(DataType.TIMESTAMP.getId(), equalTo("timestamp"));
        assertThat(DataType.TIMESTAMP.getOrder(), equalTo(7));
        assertThat(DataType.TIMESTAMP.getJavaClass(), equalTo(Instant.class));
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

}
