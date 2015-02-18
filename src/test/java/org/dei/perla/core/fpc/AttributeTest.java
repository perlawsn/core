package org.dei.perla.core.fpc;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.engine.Attribute;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 12/02/15.
 */
public class AttributeTest {

    @Test
    public void AttributeCreationTest() {
        Attribute a1 = Attribute.create("test", DataType.FLOAT);
        assertThat(a1, notNullValue());
        assertThat(a1.getId(), equalTo("test"));
        assertThat(a1.getType(), equalTo(DataType.FLOAT));


        Attribute a2 = Attribute.create("test", DataType.FLOAT);
        assertThat(a2, notNullValue());
        assertThat(a1.getId(), equalTo("test"));
        assertThat(a1.getType(), equalTo(DataType.FLOAT));

        assertThat(a1, equalTo(a2));
        assertTrue(a1 == a2);
    }

}
