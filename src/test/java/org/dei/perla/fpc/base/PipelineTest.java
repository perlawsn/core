package org.dei.perla.fpc.base;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dei.perla.fpc.base.RecordPipeline.PipelineBuilder;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.engine.Record;
import org.junit.Test;

public class PipelineTest {

	private static final Record emptyRecord = Record.from(Collections.emptyMap());

	private static final StaticAttribute att1 = new StaticAttribute("att1",
			DataType.INTEGER, 1);
	private static final StaticAttribute att2 = new StaticAttribute("att2",
			DataType.STRING, "test");

	@Test
	public void testTimestampAppender() {
		RecordModifier tsAppend = new RecordModifier.TimestampAppender();
		assertTrue(tsAppend.attributes().contains(
				BaseFpc.TIMESTAMP_ATTRIBUTE));

		Map<String, Object> fieldMap = new HashMap<>();
		tsAppend.process(emptyRecord, fieldMap);
		assertTrue(fieldMap.containsKey("timestamp"));
	}

	@Test
	public void testStaticAppender() {
		Set<StaticAttribute> staticAttSet = new HashSet<>();
		staticAttSet.add(att1);
		staticAttSet.add(att2);

		// Single static attribute appender
		RecordModifier singleAppender = new RecordModifier.StaticAppender(att1);
		assertTrue(singleAppender.attributes().contains(att1));
		assertFalse(singleAppender.attributes().contains(att2));

		Map<String, Object> singleFieldMap = new HashMap<>();
		singleAppender.process(emptyRecord, singleFieldMap);
		assertTrue(singleFieldMap.containsKey("att1"));
		assertThat(singleFieldMap.get("att1"), equalTo(1));

		// Multiple static attributes appender
		RecordModifier allAppender = new RecordModifier.StaticAppender(
				staticAttSet);
		assertTrue(allAppender.attributes().contains(att1));
		assertTrue(allAppender.attributes().contains(att2));
		
		Map<String, Object> allFieldMap = new HashMap<>();
		allAppender.process(emptyRecord, allFieldMap);
		assertTrue(allFieldMap.containsKey("att1"));
		assertThat(allFieldMap.get("att1"), equalTo(1));
		assertTrue(allFieldMap.containsKey("att2"));
		assertThat(allFieldMap.get("att2"), equalTo("test"));
	}

	@Test
	public void pipelineTest() {
		PipelineBuilder builder = RecordPipeline.newBuilder();
		builder.add(new RecordModifier.TimestampAppender());
		builder.add(new RecordModifier.StaticAppender(att1));
		
		RecordPipeline pipeline = builder.create();
		assertThat(pipeline, notNullValue());
		assertTrue(pipeline.attributes().contains(BaseFpc.TIMESTAMP_ATTRIBUTE));
		assertTrue(pipeline.attributes().contains(att1));
		assertFalse(pipeline.attributes().contains(att2));
		
		Map<String, Object> fieldMap = new HashMap<>();
		fieldMap.put("source1", "source1");
		fieldMap.put("source2", "source2");
		Record source = Record.from(fieldMap);
		assertThat(source.get("source1"), notNullValue());
		assertThat(source.get("source2"), notNullValue());
		assertThat(source.get("timestamp"), nullValue());
		assertThat(source.get("att1"), nullValue());
		assertThat(source.get("att2"), nullValue());
		
		Record output = pipeline.run(source);
		assertThat(output, notNullValue());
		assertThat(output.get("source1"), notNullValue());
		assertThat(output.get("source2"), notNullValue());
		assertThat(output.get("timestamp"), notNullValue());
		assertThat(output.get("att1"), notNullValue());
		assertThat(output.get("att2"), nullValue());
	}
	
	@Test
	public void emptyPipelineTest() {
		Map<String, Object> fieldMap = new HashMap<>();
		fieldMap.put("source1", "source1");
		fieldMap.put("source2", "source2");
		Record source = Record.from(fieldMap);
		assertThat(source.get("source1"), notNullValue());
		assertThat(source.get("source2"), notNullValue());
		assertThat(source.get("timestamp"), nullValue());
		assertThat(source.get("att1"), nullValue());
		assertThat(source.get("att2"), nullValue());
		
		assertTrue(RecordPipeline.EMPTY.attributes().isEmpty());
		Record output = RecordPipeline.EMPTY.run(source);
		assertThat(output, notNullValue());
		assertThat(output.get("source1"), notNullValue());
		assertThat(output.get("source2"), notNullValue());
		assertThat(output.get("timestamp"), nullValue());
		assertThat(output.get("att1"), nullValue());
		assertThat(output.get("att2"), nullValue());
	}

}
