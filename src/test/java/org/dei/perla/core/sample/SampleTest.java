package org.dei.perla.core.sample;

import org.dei.perla.core.fpc.DataType;
import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class SampleTest {

	public static final Sample EMPTY_SAMPLE =
			new Sample(Collections.emptyList(), new Object[0]);

	@Test
	public void sampleCreationTest() {
		Sample empty = EMPTY_SAMPLE;
		assertThat(empty, notNullValue());
		assertTrue(empty.isEmpty());
		assertTrue(empty.fields().isEmpty());
        assertThat(empty.values().length, equalTo(0));

		Sample fromEmpty = Sample.from(Collections.emptyMap());
		assertThat(fromEmpty, notNullValue());
		assertTrue(fromEmpty.isEmpty());
		assertTrue(fromEmpty.fields().isEmpty());
        assertThat(fromEmpty.values().length, equalTo(0));

		Map<Attribute, Object> fieldMap = new HashMap<>();
		fieldMap.put(Attribute.create("field1", DataType.STRING), "value1");
        fieldMap.put(Attribute.create("field2", DataType.STRING), "value2");
		Sample fromMap = Sample.from(fieldMap);
		assertThat(fromMap, notNullValue());
		assertFalse(fromMap.isEmpty());
		assertFalse(fromMap.fields().isEmpty());
		assertTrue(fromMap.hasField("field1"));
		assertThat(fromMap.getValue("field1"), notNullValue());
		assertTrue(fromMap.getValue("field1") instanceof String);
		assertThat(fromMap.getValue("field1"), equalTo("value1"));
		assertTrue(fromMap.hasField("field2"));
		assertThat(fromMap.getValue("field2"), notNullValue());
		assertTrue(fromMap.getValue("field2") instanceof String);
		assertThat(fromMap.getValue("field2"), equalTo("value2"));
	}

	@Test
	public void fieldEnumerationTest() {
		Map<Attribute, Object> map = new HashMap<>();
		map.put(Attribute.create("integer", DataType.INTEGER), 1);
		map.put(Attribute.create("float", DataType.FLOAT), 5f);
		map.put(Attribute.create("boolean", DataType.BOOLEAN), false);
		map.put(Attribute.create("string", DataType.STRING), "test");
		map.put(Attribute.create("id", DataType.ID), 9);
		map.put(Attribute.create("timestamp", DataType.TIMESTAMP),
                Instant.now());
		Sample r = Sample.from(map);

		for (Attribute a : r.fields()) {
            Object f = r.getValue(a.getId());
			assertThat(f, notNullValue());
			switch (a.getId()) {
			case "integer":
                assertThat(a.getType(), equalTo(DataType.INTEGER));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(1));
				break;
			case "float":
                assertThat(a.getType(), equalTo(DataType.FLOAT));
				assertTrue(f instanceof Float);
				assertThat(f, equalTo(5f));
				break;
			case "boolean":
                assertThat(a.getType(), equalTo(DataType.BOOLEAN));
				assertTrue(f instanceof Boolean);
				assertThat(f, equalTo(false));
				break;
			case "string":
                assertThat(a.getType(), equalTo(DataType.STRING));
				assertTrue(f instanceof String);
				assertThat(f, equalTo("test"));
				break;
			case "id":
                assertThat(a.getType(), equalTo(DataType.ID));
				assertTrue(f instanceof Integer);
				assertThat(f, equalTo(9));
				break;
			case "timestamp":
                assertThat(a.getType(), equalTo(DataType.TIMESTAMP));
				assertTrue(f instanceof Instant);
				assertThat(f, notNullValue());
				break;
			default:
				throw new RuntimeException("Unexpected field " + a.getId());
			}
		}
	}

}
