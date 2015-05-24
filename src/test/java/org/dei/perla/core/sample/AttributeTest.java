package org.dei.perla.core.sample;

import org.dei.perla.core.descriptor.DataType;
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

}
