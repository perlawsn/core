package org.dei.perla.core.fpc;

import org.dei.perla.core.fpc.*;
import org.dei.perla.core.fpc.SampleModifier.Reorder;
import org.dei.perla.core.fpc.SampleModifier.StaticAppender;
import org.dei.perla.core.fpc.SampleModifier.TimestampAppender;
import org.dei.perla.core.fpc.SamplePipeline.PipelineBuilder;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
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
	public void testPassthrough() {
		List<Attribute> atts = Arrays.asList(new Attribute[] {
				a1,
				a2
		});

		SamplePipeline p = SamplePipeline.passthrough(atts);
		assertThat(p, notNullValue());
		assertTrue(atts.containsAll(p.attributes()));
		assertThat(p.attributes().size(), equalTo(atts.size()));

		Object[] in = new Object[]{v1, v2};
		Sample out = p.run(in);
		assertTrue(atts.containsAll(out.fields()));
		assertThat(out.fields().size(), equalTo(atts.size()));
		assertThat(out.getValue("a1"), equalTo(v1));
		assertThat(out.getValue("a2"), equalTo(v2));
	}

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
	public void testReorder() {
		List<Attribute> in = new ArrayList<>();
		in.add(Attribute.create("5", DataType.INTEGER));
		in.add(Attribute.create("3", DataType.INTEGER));
		in.add(Attribute.create("1", DataType.INTEGER));
		in.add(Attribute.create("2", DataType.INTEGER));
		in.add(Attribute.create("4", DataType.INTEGER));
		in.add(Attribute.create("0", DataType.INTEGER));

		List<Attribute> out = Arrays.asList(new Attribute[] {
				Attribute.create("0", DataType.INTEGER),
				Attribute.create("1", DataType.INTEGER),
				Attribute.create("2", DataType.INTEGER),
				Attribute.create("3", DataType.INTEGER),
				Attribute.create("4", DataType.INTEGER),
				Attribute.create("5", DataType.INTEGER)
		});
		Object[] values = new Object[]{5, 3, 1, 2, 4, 0};

		SampleModifier reorder = new Reorder(in, out);
		reorder.process(values);
		for (int i = 0; i < values.length; i++) {
			Object o = values[i];
			assertTrue(o instanceof Integer);
			assertThat((Integer) o, equalTo(i));
		}
	}

	@Test
	public void testPartialReorder() {
		List<Attribute> in = new ArrayList<>();
		in.add(Attribute.create("5", DataType.INTEGER));
		in.add(Attribute.create("3", DataType.INTEGER));
		in.add(Attribute.create("1", DataType.INTEGER));
		in.add(Attribute.create("2", DataType.INTEGER));
		in.add(Attribute.create("4", DataType.INTEGER));
		in.add(Attribute.create("0", DataType.INTEGER));

		List<Attribute> out = Arrays.asList(new Attribute[] {
				Attribute.create("0", DataType.INTEGER),
				Attribute.create("1", DataType.INTEGER),
				Attribute.create("2", DataType.INTEGER),
		});
		Object[] values = new Object[]{5, 3, 1, 2, 4, 0};

		SampleModifier reorder = new Reorder(in, out);
		reorder.process(values);
		int i;
		for (i = 0; i < out.size(); i++) {
			Integer o = (Integer) values[i];
			assertThat((Integer) o, equalTo(i));
		}
	}

	@Test
	public void testNullReorder() {
		List<Attribute> in = new ArrayList<>();
		in.add(Attribute.create("5", DataType.INTEGER));
		in.add(Attribute.create("3", DataType.INTEGER));
		in.add(Attribute.create("2", DataType.INTEGER));
		in.add(Attribute.create("4", DataType.INTEGER));
		in.add(Attribute.create("0", DataType.INTEGER));

		List<Attribute> out = Arrays.asList(new Attribute[] {
				Attribute.create("0", DataType.INTEGER),
				Attribute.create("1", DataType.INTEGER),
				Attribute.create("2", DataType.INTEGER),
		});
		Object[] values = new Object[]{5, 3, 2, 4, 0, null};

		SampleModifier reorder = new Reorder(in, out);
		reorder.process(values);
		assertThat(values[0], equalTo(0));
		assertThat(values[1], nullValue());
		assertThat(values[2], equalTo(2));
	}

	@Test
	public void testPipeline() {
		Attribute s1 = Attribute.create("source1", DataType.STRING);
		Attribute s2 = Attribute.create("source2", DataType.STRING);
        LinkedHashMap<Attribute, Object> st = new LinkedHashMap<>();
        st.put(a1, v1);

		List<Attribute> atts = new ArrayList<>();
		atts.add(s1);
		atts.add(s2);

		PipelineBuilder b = SamplePipeline.newBuilder(atts);
		b.addTimestamp();
		b.addStatic(st);
		b.reorder(Arrays.asList(new Attribute[]{
				Attribute.TIMESTAMP,
				s2,
				s1
		}));

		SamplePipeline p = b.create();
		assertThat(p, notNullValue());
		assertTrue(p.attributes().contains(Attribute.TIMESTAMP));
		assertTrue(p.attributes().contains(a1));
		assertFalse(p.attributes().contains(a2));

		Object[] source = new Object[]{"source1", "source2"};
		Sample output = p.run(source);
		assertThat(output, notNullValue());
		assertThat(output.getValue("source1"), equalTo("source1"));
		assertThat(output.getValue("source2"), equalTo("source2"));
		assertThat(output.getValue("timestamp"), notNullValue());
		assertThat(output.getValue("a1"), equalTo(v1));
		assertThat(output.getValue("a2"), nullValue());

		List<Attribute> ratts = output.fields();
		assertThat(ratts.get(0), equalTo(Attribute.TIMESTAMP));
		assertThat(ratts.get(1), equalTo(s2));
		assertThat(ratts.get(2), equalTo(s1));
		assertThat(ratts.get(3), equalTo(a1));
	}

	@Test
	public void testPipelineShorterSample() {
		Attribute s1 = Attribute.create("source1", DataType.STRING);
		Attribute s2 = Attribute.create("source2", DataType.INTEGER);
		List<Attribute> atts = new ArrayList<>();
		atts.add(s1);
		atts.add(s2);
		Object[] sample = new Object[] {"source1", 2};

		PipelineBuilder b = SamplePipeline.newBuilder(atts);
		b.reorder(Arrays.asList(new Attribute[]{s2}));

		SamplePipeline p = b.create();
		assertThat(p, notNullValue());
		assertTrue(p.attributes().contains(s2));
		assertThat(p.attributes().size(), equalTo(2));

		Sample r = p.run(sample);
		assertThat(r.values().length, equalTo(2));
		assertThat(r.fields().size(), equalTo(2));
	}

}
