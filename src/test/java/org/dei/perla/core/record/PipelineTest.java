package org.dei.perla.core.record;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.record.SampleModifier.StaticAppender;
import org.dei.perla.core.record.SampleModifier.TimestampAppender;
import org.dei.perla.core.record.SamplePipeline.PipelineBuilder;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

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
		SampleModifier tsAppend = new TimestampAppender(0);
        Object[] r = new Object[1];
        assertThat(r[0], nullValue());
		tsAppend.process(r);
        assertThat(r[0], notNullValue());
        assertTrue(r[0] instanceof Instant);
	}

	@Test
	public void testStaticAppender() {
		Object[] values = new Object[]{v1, v2};

		// Multiple static attributes appender
		SampleModifier allAppender =
				new StaticAppender(0, values);

        Object[] r = new Object[2];
        assertThat(r[0], nullValue());
        assertThat(r[1], nullValue());
		allAppender.process(r);
		assertThat(r[0], equalTo(1));
		assertThat(r[1], equalTo("test"));
	}

	@Test
	public void pipelineTest() {
        LinkedHashMap<Attribute, Object> st = new LinkedHashMap<>();
        st.put(a1, v1);

		List<Attribute> sourceAtt = Arrays.asList(new Attribute[] {
				Attribute.create("source1", DataType.STRING),
				Attribute.create("source2", DataType.STRING)
		});
		PipelineBuilder b = SamplePipeline.newBuilder(sourceAtt);
		b.addTimestamp();
		b.addStatic(st);

		SamplePipeline p = b.create();
		assertThat(p, notNullValue());
		assertTrue(p.attributes().contains(Attribute.TIMESTAMP));
		assertTrue(p.attributes().contains(a1));
		assertFalse(p.attributes().contains(a2));

		Object[] source = new Object[]{"source1", "source2"};
		Record output = p.run(source);
		assertThat(output, notNullValue());
		assertThat(output.getValue("source1"), equalTo("source1"));
		assertThat(output.getValue("source2"), equalTo("source2"));
		assertThat(output.getValue("timestamp"), notNullValue());
		assertThat(output.getValue("a1"), equalTo(v1));
		assertThat(output.getValue("a2"), nullValue());
	}

}
