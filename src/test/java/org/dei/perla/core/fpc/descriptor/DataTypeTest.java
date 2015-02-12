package org.dei.perla.core.fpc.descriptor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import org.junit.Test;

public class DataTypeTest {

	@Test
	public void testGetClass() {
		assertTrue(DataType.getClass(DataType.INTEGER) == Integer.class);
		assertTrue(DataType.getClass(DataType.FLOAT) == Float.class);
		assertTrue(DataType.getClass(DataType.BOOLEAN) == Boolean.class);
		assertTrue(DataType.getClass(DataType.STRING) == String.class);
		assertTrue(DataType.getClass(DataType.ID) == Integer.class);
		assertTrue(DataType.getClass(DataType.TIMESTAMP) == ZonedDateTime.class);

		assertTrue(DataType.getJavaClass(DataType.INTEGER.getId()) == Integer.class);
		assertTrue(DataType.getJavaClass(DataType.FLOAT.getId()) == Float.class);
		assertTrue(DataType.getJavaClass(DataType.BOOLEAN.getId()) == Boolean.class);
		assertTrue(DataType.getJavaClass(DataType.STRING.getId()) == String.class);
		assertTrue(DataType.getJavaClass(DataType.ID.getId()) == Integer.class);
		assertTrue(DataType.getJavaClass(DataType.TIMESTAMP.getId()) == ZonedDateTime.class);
	}

	@Test
	public void testParse() {
		assertThat(DataType.parse(DataType.INTEGER, "5"), equalTo(5));
		assertThat(DataType.parse(DataType.FLOAT, "5.1"), equalTo(5.1f));
		assertThat(DataType.parse(DataType.BOOLEAN, "true"), equalTo(true));
		assertThat(DataType.parse(DataType.STRING, "test"), equalTo("test"));
		assertThat(DataType.parse(DataType.ID, "45355"), equalTo(45355));

		assertThat(DataType.parse(DataType.INTEGER.getId(), "5"), equalTo(5));
		assertThat(DataType.parse(DataType.FLOAT.getId(), "5.1"), equalTo(5.1f));
		assertThat(DataType.parse(DataType.BOOLEAN.getId(), "true"),
				equalTo(true));
		assertThat(DataType.parse(DataType.STRING.getId(), "test"),
				equalTo("test"));
		assertThat(DataType.parse(DataType.ID.getId(), "45355"), equalTo(45355));
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

}
