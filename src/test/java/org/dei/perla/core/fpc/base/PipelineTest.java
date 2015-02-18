package org.dei.perla.core.fpc.base;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.engine.Record;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.base.RecordPipeline.PipelineBuilder;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PipelineTest {

    private static final Attribute a1 =
            Attribute.create("a1", DataType.INTEGER);
    private static final Object v1 = 1;

    private static final Attribute a2 =
            Attribute.create("a2", DataType.STRING);
    private static final Object v2 = "test";


	@Test
	public void testTimestampAppender() {
		RecordModifier tsAppend = new RecordModifier.TimestampAppender();
		assertTrue(tsAppend.getAttributes().contains(
				Attribute.TIMESTAMP_ATTRIBUTE));

        Object[] r = new Object[1];
        assertThat(r[0], nullValue());
		tsAppend.process(r, 0);
        assertThat(r[0], notNullValue());
        assertTrue(r[0] instanceof ZonedDateTime);
	}

	@Test
	public void testStaticAppender() {
        LinkedHashMap<Attribute, Object> st = new LinkedHashMap<>();
        st.put(a1, v1);
        st.put(a2, v2);

		// Multiple static attributes appender
		RecordModifier allAppender = new RecordModifier.StaticAppender(st);
		assertTrue(allAppender.getAttributes().contains(a1));
		assertTrue(allAppender.getAttributes().contains(a2));

        Object[] r = new Object[2];
        assertThat(r[0], nullValue());
        assertThat(r[1], nullValue());
		allAppender.process(r, 0);
		assertThat(r[0], equalTo(1));
		assertThat(r[1], equalTo("test"));
	}

	@Test
	public void pipelineTest() {
        LinkedHashMap<Attribute, Object> st = new LinkedHashMap<>();
        st.put(a1, v1);

		PipelineBuilder b = RecordPipeline.newBuilder();
		b.add(new RecordModifier.TimestampAppender());
		b.add(new RecordModifier.StaticAppender(st));

		RecordPipeline p = b.create();
		assertThat(p, notNullValue());
		assertTrue(p.attributes().contains(Attribute.TIMESTAMP_ATTRIBUTE));
		assertTrue(p.attributes().contains(a1));
		assertFalse(p.attributes().contains(a2));

		Map<Attribute, Object> fieldMap = new HashMap<>();
		fieldMap.put(Attribute.create("source1", DataType.STRING), "source1");
        fieldMap.put(Attribute.create("source2", DataType.STRING), "source2");
		Record source = Record.from(fieldMap);
		assertThat(source.get("source1"), notNullValue());
		assertThat(source.get("source2"), notNullValue());
		assertThat(source.get("timestamp"), nullValue());
		assertThat(source.get("a1"), nullValue());
		assertThat(source.get("a2"), nullValue());

		Record output = p.run(source);
		assertThat(output, notNullValue());
		assertThat(output.get("source1"), notNullValue());
		assertThat(output.get("source2"), notNullValue());
		assertThat(output.get("timestamp"), notNullValue());
		assertThat(output.get("a1"), notNullValue());
		assertThat(output.get("a2"), nullValue());
	}

	@Test
	public void emptyPipelineTest() {
		Map<Attribute, Object> fieldMap = new HashMap<>();
        fieldMap.put(Attribute.create("source1", DataType.STRING), "source1");
        fieldMap.put(Attribute.create("source2", DataType.STRING), "source2");
		Record source = Record.from(fieldMap);
		assertThat(source.get("source1"), notNullValue());
		assertThat(source.get("source2"), notNullValue());
		assertThat(source.get("timestamp"), nullValue());
		assertThat(source.get("a1"), nullValue());
		assertThat(source.get("a2"), nullValue());

		assertTrue(RecordPipeline.EMPTY.attributes().isEmpty());
		Record output = RecordPipeline.EMPTY.run(source);
		assertThat(output, notNullValue());
		assertThat(output.get("source1"), notNullValue());
		assertThat(output.get("source2"), notNullValue());
		assertThat(output.get("timestamp"), nullValue());
		assertThat(output.get("a1"), nullValue());
		assertThat(output.get("a2"), nullValue());
	}

}
