package org.dei.perla.fpc.engine;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dei.perla.fpc.engine.Record.Field;
import org.junit.Test;

public class RecordTest {

	@Test
	public void recordCreationTest() {
		Record empty = Record.EMPTY;
		assertThat(empty, notNullValue());
		assertTrue(empty.isEmpty());
		assertTrue(empty.fields().isEmpty());

		Record fromEmpty = Record.from(Collections.emptyMap());
		assertThat(fromEmpty, notNullValue());
		assertTrue(fromEmpty.isEmpty());
		assertTrue(fromEmpty.fields().isEmpty());

		Map<String, Object> fieldMap = new HashMap<>();
		fieldMap.put("field1", "value1");
		fieldMap.put("field2", "value2");
		Record fromMap = Record.from(fieldMap);
		assertThat(fromMap, notNullValue());
		assertFalse(fromMap.isEmpty());
		assertFalse(fromMap.fields().isEmpty());
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
	public void recordMergeTest() {
		Map<String, Object> map1 = new HashMap<>();
		map1.put("fieldA", "A");
		map1.put("fieldB", "B");
		Record r1 = Record.from(map1);

		Map<String, Object> map2 = new HashMap<>();
		map2.put("fieldB", "BB");
		map2.put("fieldC", "C");
		Record r2 = Record.from(map2);

		Record merged = Record.merge(r1, r2);
		assertThat(merged, notNullValue());
		assertTrue(merged.hasField("fieldA"));
		assertTrue(merged.hasField("fieldB"));
		assertTrue(merged.hasField("fieldC"));

		assertThat(merged.get("fieldA"), equalTo(r1.get("fieldA")));
		assertThat(merged.get("fieldB"), equalTo(r2.get("fieldB")));
		assertThat(merged.get("fieldC"), equalTo(r2.get("fieldC")));
	}

	@Test
	public void fieldEnumerationTest() {
		Map<String, Object> map = new HashMap<>();
		map.put("integer", 1);
		map.put("float", 5f);
		map.put("boolean", false);
		map.put("string", "test");
		map.put("id", 9);
		map.put("timestamp", ZonedDateTime.now());
		Record r = Record.from(map);

		for (Field f : r.fields()) {
			assertThat(f, notNullValue());
			assertThat(f.getValue(), notNullValue());
			switch (f.getName()) {
			case "integer":
				assertTrue(f.getValue() instanceof Integer);
				assertThat(f.getIntValue(), equalTo(1));
				break;
			case "float":
				assertTrue(f.getValue() instanceof Float);
				assertThat(f.getFloatValue(), equalTo(5f));
				break;
			case "boolean":
				assertTrue(f.getValue() instanceof Boolean);
				assertThat(f.getBooleanValue(), equalTo(false));
				break;
			case "string":
				assertTrue(f.getValue() instanceof String);
				assertThat(f.getStringValue(), equalTo("test"));
				break;
			case "id":
				assertTrue(f.getValue() instanceof Integer);
				assertThat(f.getIdValue(), equalTo(9));
				break;
			case "timestamp":
				assertTrue(f.getValue() instanceof ZonedDateTime);
				assertThat(f.getTimestampValue(), notNullValue());
				break;
			default:
				throw new RuntimeException("Unexpected field " + f.getName());
			}
		}
	}

}
