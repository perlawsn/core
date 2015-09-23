package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.Sample;
import org.dei.perla.core.fpc.base.SamplePipeline.Modifier;
import org.dei.perla.core.fpc.base.SamplePipeline.Copy;
import org.dei.perla.core.fpc.base.SamplePipeline.StaticAppender;
import org.dei.perla.core.fpc.base.SamplePipeline.TimestampAdder;
import org.junit.Test;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class SamplePipelineTest {

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

        SamplePipeline p = new SamplePipeline(atts);
        assertThat(p, notNullValue());
        assertTrue(atts.containsAll(p.getAttributes()));
        assertThat(p.getAttributes().size(), equalTo(atts.size()));

        Object[] in = new Object[]{v1, v2};
        Sample out = p.run(in);
        assertTrue(atts.containsAll(out.fields()));
        assertThat(out.fields().size(), equalTo(atts.size()));
        assertThat(out.getValue("a1"), equalTo(v1));
        assertThat(out.getValue("a2"), equalTo(v2));
    }

    @Test
    public void testTimestampAppender() {
        Modifier tsAppend = new TimestampAdder(3);
        Object[] out = new Object[4];
        for (Object o : out) {
            assertThat(o, nullValue());
        }
        tsAppend.process(null, out);
        for (int i = 0; i < out.length; i++) {
            if (i == 3) {
                assertTrue(out[i] instanceof Instant);
            } else {
                assertThat(out[i], nullValue());
            }
        }
    }

    @Test
    public void testStaticAppender() {
        Object[] values = new Object[]{v1, null, v2};

        // Multiple static attributes appender
        Modifier allAppender = new StaticAppender(values);

        Object[] r = new Object[3];
        assertThat(r[0], nullValue());
        assertThat(r[1], nullValue());
        assertThat(r[2], nullValue());
        allAppender.process(null, r);
        assertThat(r[0], equalTo(v1));
        assertThat(r[1], nullValue());
        assertThat(r[2], equalTo(v2));
    }

    @Test
    public void testCopy() {
        Object[] in = new Object[]{5, 3, 1, 2, 4, 0};
        int[] order = new int[]{7, 4, 1, 2, 6, 0};

        Copy copy = new Copy(order);
        Object[] out = new Object[8];
        for (Object o : out) {
            assertThat(o, nullValue());
        }
        copy.process(in, out);
        assertThat(out[0], equalTo(0));
        assertThat(out[1], equalTo(1));
        assertThat(out[2], equalTo(2));
        assertThat(out[3], nullValue());
        assertThat(out[4], equalTo(3));
        assertThat(out[5], nullValue());
        assertThat(out[6], equalTo(4));
        assertThat(out[7], equalTo(5));
    }

    @Test
    public void testPipelineSimple1() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);
        Attribute s3 = Attribute.create("source3", DataType.STRING);

        List<Attribute> in = Arrays.asList(new Attribute[] {
                s1, s2, s3
        });
        List<Attribute> out = Arrays.asList(new Attribute[] {
                s1, s2
        });

        SamplePipeline p = new SamplePipeline(in, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.size(), equalTo(3));
        assertThat(atts.get(0), equalTo(s1));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(Attribute.TIMESTAMP));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(2));
        assertTrue(mods.get(0) instanceof Copy);
        assertTrue(mods.get(1) instanceof TimestampAdder);

        Object[] source = new Object[]{"source1", "source2"};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(3));
        assertThat(values[0], equalTo("source1"));
        assertThat(values[1], equalTo("source2"));
        assertTrue(values[2] instanceof Instant);

        atts = sample.fields();
        assertThat(atts.size(), equalTo(3));
        assertThat(atts.get(0), equalTo(s1));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(Attribute.TIMESTAMP));
    }

    @Test
    public void testPipelineSimple2() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);

        List<Attribute> in = new ArrayList<>();
        in.add(s1);
        in.add(s2);
        List<Attribute> out = new ArrayList<>();
        out.add(s2);
        out.add(s1);

        SamplePipeline p = new SamplePipeline(in, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.get(0), equalTo(s2));
        assertThat(atts.get(1), equalTo(s1));
        assertThat(atts.get(2), equalTo(Attribute.TIMESTAMP));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(2));
        assertTrue(mods.get(0) instanceof Copy);
        assertTrue(mods.get(1) instanceof TimestampAdder);

        Object[] source = new Object[]{"source1", "source2"};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(3));
        assertThat(values[0], equalTo("source2"));
        assertThat(values[1], equalTo("source1"));
        assertTrue(values[2] instanceof Instant);

        atts = sample.fields();
        assertThat(atts.size(), equalTo(3));
        assertThat(atts.get(0), equalTo(s2));
        assertThat(atts.get(1), equalTo(s1));
        assertThat(atts.get(2), equalTo(Attribute.TIMESTAMP));
    }

    @Test
    public void testPipelineStatic() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);
        Attribute s3 = Attribute.create("source3", DataType.STRING);

        List<Attribute> in = Arrays.asList(new Attribute[]{
                s1,
                s2
        });
        List<Attribute> out = Arrays.asList(new Attribute[]{
                s3,
                s2,
                s1
        });

        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(s3, "static3");

        SamplePipeline p = new SamplePipeline(in, stat, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.get(0), equalTo(s3));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(s1));
        assertThat(atts.get(3), equalTo(Attribute.TIMESTAMP));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(3));
        assertTrue(mods.get(0) instanceof StaticAppender);
        assertTrue(mods.get(1) instanceof Copy);
        assertTrue(mods.get(2) instanceof TimestampAdder);

        Object[] source = new Object[]{"source1", "source2"};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertThat(sample.getValue("source3"), equalTo("static3"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(4));
        assertThat(values[0], equalTo("static3"));
        assertThat(values[1], equalTo("source2"));
        assertThat(values[2], equalTo("source1"));
        assertTrue(values[3] instanceof Instant);

        atts = sample.fields();
        assertThat(atts.size(), equalTo(4));
        assertThat(atts.get(0), equalTo(s3));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(s1));
        assertThat(atts.get(3), equalTo(Attribute.TIMESTAMP));
    }

    @Test
    public void testPipelineTimestampIn() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);
        Attribute s3 = Attribute.create("source3", DataType.STRING);

        List<Attribute> in = Arrays.asList(new Attribute[]{
                s1,
                s2,
                Attribute.TIMESTAMP
        });
        List<Attribute> out = Arrays.asList(new Attribute[]{
                s3,
                s2,
                s1
        });

        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(s3, "static3");

        SamplePipeline p = new SamplePipeline(in, stat, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.get(0), equalTo(s3));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(s1));
        assertThat(atts.get(3), equalTo(Attribute.TIMESTAMP));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(2));
        assertTrue(mods.get(0) instanceof StaticAppender);
        assertTrue(mods.get(1) instanceof Copy);

        Object[] source = new Object[]{"source1", "source2", Instant.now()};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertThat(sample.getValue("source3"), equalTo("static3"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(4));
        assertThat(values[0], equalTo("static3"));
        assertThat(values[1], equalTo("source2"));
        assertThat(values[2], equalTo("source1"));
        assertTrue(values[3] instanceof Instant);

        atts = sample.fields();
        assertThat(atts.size(), equalTo(4));
        assertThat(atts.get(0), equalTo(s3));
        assertThat(atts.get(1), equalTo(s2));
        assertThat(atts.get(2), equalTo(s1));
        assertThat(atts.get(3), equalTo(Attribute.TIMESTAMP));
    }

    @Test
    public void testPipelineTimestampInOut() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);
        Attribute s3 = Attribute.create("source3", DataType.STRING);

        List<Attribute> in = Arrays.asList(new Attribute[]{
                s1,
                s2,
                Attribute.TIMESTAMP
        });
        List<Attribute> out = Arrays.asList(new Attribute[]{
                Attribute.TIMESTAMP,
                s3,
                s2,
                s1
        });

        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(s3, "static3");

        SamplePipeline p = new SamplePipeline(in, stat, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.get(0), equalTo(Attribute.TIMESTAMP));
        assertThat(atts.get(1), equalTo(s3));
        assertThat(atts.get(2), equalTo(s2));
        assertThat(atts.get(3), equalTo(s1));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(2));
        assertTrue(mods.get(0) instanceof StaticAppender);
        assertTrue(mods.get(1) instanceof Copy);

        Object[] source = new Object[]{"source1", "source2", Instant.now()};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertThat(sample.getValue("source3"), equalTo("static3"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(4));
        assertTrue(values[0] instanceof Instant);
        assertThat(values[1], equalTo("static3"));
        assertThat(values[2], equalTo("source2"));
        assertThat(values[3], equalTo("source1"));

        atts = sample.fields();
        assertThat(atts.size(), equalTo(4));
        assertThat(atts.get(0), equalTo(Attribute.TIMESTAMP));
        assertThat(atts.get(1), equalTo(s3));
        assertThat(atts.get(2), equalTo(s2));
        assertThat(atts.get(3), equalTo(s1));
    }

    @Test
    public void testPipelineReorderNull() {
        Attribute s1 = Attribute.create("source1", DataType.STRING);
        Attribute s2 = Attribute.create("source2", DataType.STRING);
        Attribute s3 = Attribute.create("source3", DataType.STRING);
        Attribute null1 = Attribute.create("null1", DataType.INTEGER);
        Attribute null2 = Attribute.create("null2", DataType.INTEGER);

        List<Attribute> in = Arrays.asList(new Attribute[]{
                s1,
                s2
        });
        List<Attribute> out = Arrays.asList(new Attribute[]{
                Attribute.TIMESTAMP,
                s3,
                null1,
                s2,
                null2,
                s1
        });

        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(s3, "static3");

        SamplePipeline p = new SamplePipeline(in, stat, out);
        List<Attribute> atts = p.getAttributes();
        assertThat(atts.get(0), equalTo(Attribute.TIMESTAMP));
        assertThat(atts.get(1), equalTo(s3));
        assertThat(atts.get(2), equalTo(null1));
        assertThat(atts.get(3), equalTo(s2));
        assertThat(atts.get(4), equalTo(null2));
        assertThat(atts.get(5), equalTo(s1));

        List<Modifier> mods = p.getModifiers();
        assertThat(mods.size(), equalTo(3));
        assertTrue(mods.get(0) instanceof StaticAppender);
        assertTrue(mods.get(1) instanceof Copy);
        assertTrue(mods.get(2) instanceof TimestampAdder);

        Object[] source = new Object[]{"source1", "source2"};
        Sample sample = p.run(source);
        assertThat(sample, notNullValue());
        assertThat(sample.getValue("null1"), nullValue());
        assertThat(sample.getValue("null2"), nullValue());
        assertThat(sample.getValue("source1"), equalTo("source1"));
        assertThat(sample.getValue("source2"), equalTo("source2"));
        assertThat(sample.getValue("source3"), equalTo("static3"));
        assertTrue(sample.getValue("timestamp") instanceof Instant);

        Object[] values = sample.values();
        assertThat(values.length, equalTo(6));
        assertTrue(values[0] instanceof Instant);
        assertThat(values[1], equalTo("static3"));
        assertThat(values[2], nullValue());
        assertThat(values[3], equalTo("source2"));
        assertThat(values[4], nullValue());
        assertThat(values[5], equalTo("source1"));

        atts = sample.fields();
        assertThat(atts.size(), equalTo(6));
        assertThat(atts.get(0), equalTo(Attribute.TIMESTAMP));
        assertThat(atts.get(1), equalTo(s3));
        assertThat(atts.get(2), equalTo(null1));
        assertThat(atts.get(3), equalTo(s2));
        assertThat(atts.get(4), equalTo(null2));
        assertThat(atts.get(5), equalTo(s1));
    }

}
