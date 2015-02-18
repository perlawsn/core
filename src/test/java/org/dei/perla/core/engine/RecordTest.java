package org.dei.perla.core.engine;

import org.dei.perla.core.descriptor.DataType;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class RecordTest {

	@Test
	public void recordCreationTest() {
		Record empty = Record.EMPTY;
		assertThat(empty, notNullValue());
		assertTrue(empty.isEmpty());
		assertTrue(empty.getAttributes().isEmpty());
        assertThat(empty.getFields().length, equalTo(0));

		Record fromEmpty = Record.from(Collections.emptyMap());
		assertThat(fromEmpty, notNullValue());
		assertTrue(fromEmpty.isEmpty());
		assertTrue(fromEmpty.getAttributes().isEmpty());
        assertThat(fromEmpty.getFields().length, equalTo(0));

		Map<Attribute, Object> fieldMap = new HashMap<>();
		fieldMap.put(Attribute.create("field1", DataType.STRING), "value1");
        fieldMap.put(Attribute.create("field2", DataType.STRING), "value2");
		Record fromMap = Record.from(fieldMap);
		assertThat(fromMap, notNullValue());
		assertFalse(fromMap.isEmpty());
		assertFalse(fromMap.getAttributes().isEmpty());
		assertTrue(fromMap.hasField("field1"));
		assertThat(fromMap.get("field1"), notNullValue());
		assertTrue(fromMap.get("field1") instanceof String);
		assertThat(fromMap.get("field1"), equalTo("value1"));
		assertTrue(fromMap.hasField("field2"));
		assertThat(fromMap.get("field2"), notNullValue());
		assertTrue(fromMap.get("field2") instanceof String);
		assertThat(fromMap.get("field2"), equalTo("value2"));
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
                ZonedDateTime.now());
		Record r = Record.from(map);

		for (Attribute a : r.getAttributes()) {
            Object f = r.get(a.getId());
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
				assertTrue(f instanceof ZonedDateTime);
				assertThat(f, notNullValue());
				break;
			default:
				throw new RuntimeException("Unexpected field " + a.getId());
			}
		}
	}

}
