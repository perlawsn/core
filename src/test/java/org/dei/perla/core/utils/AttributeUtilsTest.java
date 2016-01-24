package org.dei.perla.core.utils;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class AttributeUtilsTest {

    private static final List<Attribute> atts = Arrays.asList(
            Attribute.create("integer", DataType.INTEGER),
            Attribute.create("float", DataType.FLOAT),
            Attribute.create("string", DataType.STRING)
    );

    @Test
    public void testFound() {
        int idx = AttributeUtils.indexOf(atts, "integer");
        assertThat(idx, equalTo(0));

        idx = AttributeUtils.indexOf(atts, "float");
        assertThat(idx, equalTo(1));

        idx = AttributeUtils.indexOf(atts, "string");
        assertThat(idx, equalTo(2));
    }

    @Test
    public void testNotFound() {
        int idx = AttributeUtils.indexOf(atts, "not_present");
        assertThat(idx, equalTo(-1));

        idx = AttributeUtils.indexOf(atts, "cannot_be_found");
        assertThat(idx, equalTo(-1));
    }

    @Test
    public void testEmptyList() {
        int idx = AttributeUtils.indexOf(Collections.emptyList(), "integer");
        assertThat(idx, equalTo(-1));
    }

    @Test(expected = NullPointerException.class)
    public void testNullList() {
        AttributeUtils.indexOf(null, "integer");
    }

    @Test(expected = NullPointerException.class)
    public void testNullId() {
        AttributeUtils.indexOf(atts, null);
    }

}
