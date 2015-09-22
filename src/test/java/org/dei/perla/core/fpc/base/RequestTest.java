package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.Sample;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.fpc.SamplePipeline.Modifier;
import org.dei.perla.core.fpc.SamplePipeline.Reorder;
import org.dei.perla.core.fpc.SamplePipeline.StaticAppender;
import org.dei.perla.core.fpc.SamplePipeline.TimestampAdder;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

/**
 * @author Guido Rota 21/09/15.
 */
public class RequestTest {

    private static final Attribute tempAtt =
            Attribute.create("temperature", DataType.INTEGER);
    private static final Attribute pressAtt =
            Attribute.create("pressure", DataType.INTEGER);

    @Test
    public void testSampled() {
        List<Attribute> atts = Arrays.asList(new Attribute[] {
                tempAtt,
                pressAtt
        });
        Request r = new Request(atts, Collections.emptyMap());

        assertTrue(r.isSampled());
        assertTrue(r.getGenerated().isEmpty());
        assertTrue(r.getSampled().containsAll(atts));
        assertThat(r.getSampled().size(), equalTo(atts.size()));

        // Check sample pipeline, generated timestamp
        List<Attribute> opAtts = Arrays.asList(new Attribute[] {
                pressAtt,
                tempAtt
        });
        SamplePipeline s = r.createPipeline(opAtts);

        List<Modifier> mods = s.getModifiers();
        assertThat(mods.size(), equalTo(2));
        assertTrue(mods.get(0) instanceof TimestampAdder);
        assertTrue(mods.get(1) instanceof Reorder);

        // Check sample pipeline, native timestamp
        opAtts = Arrays.asList(new Attribute[] {
                pressAtt,
                tempAtt,
                Attribute.TIMESTAMP
        });
        s = r.createPipeline(opAtts);

        mods = s.getModifiers();
        assertThat(mods.size(), equalTo(1));
        assertTrue(mods.get(0) instanceof Reorder);
    }

    @Test
    public void testGenerated() {
        List<Attribute> atts = Arrays.asList(new Attribute[] {
                tempAtt,
                pressAtt
        });
        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(tempAtt, 12);
        stat.put(pressAtt, 54);
        Request r = new Request(atts, stat);

        assertFalse(r.isSampled());
        assertTrue(r.getSampled().isEmpty());
        assertTrue(r.getGenerated().containsAll(stat.keySet()));
        assertTrue(r.getGenerated().contains(Attribute.TIMESTAMP));
        assertThat(r.getGenerated().size(), equalTo(stat.size() + 1));

        // Check generated sample
        Sample s = r.generateSample();
        assertTrue(s.fields().containsAll(atts));
        assertThat(s.fields().size(), equalTo(atts.size() + 1));
        assertThat(s.getValue(tempAtt.getId()), equalTo(stat.get(tempAtt)));
        assertThat(s.getValue(pressAtt.getId()), equalTo(stat.get(pressAtt)));
        Object ts = s.getValue(Attribute.TIMESTAMP.getId());
        assertThat(ts, notNullValue());
    }

    @Test
    public void testGeneratedTimestamp() {
        List<Attribute> atts = Arrays.asList(new Attribute[] {
                tempAtt,
                Attribute.TIMESTAMP,
                pressAtt
        });
        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(tempAtt, 12);
        stat.put(pressAtt, 54);
        Request r = new Request(atts, stat);

        assertFalse(r.isSampled());
        assertTrue(r.getSampled().isEmpty());
        assertTrue(r.getGenerated().containsAll(stat.keySet()));
        assertThat(r.getGenerated().size(), equalTo(atts.size()));

        // Check generated sample
        Sample s = r.generateSample();
        assertTrue(s.fields().containsAll(atts));
        assertThat(s.fields().size(), equalTo(atts.size()));
        assertThat(s.getValue(tempAtt.getId()), equalTo(stat.get(tempAtt)));
        assertThat(s.getValue(pressAtt.getId()), equalTo(stat.get(pressAtt)));
        Object ts = s.getValue(Attribute.TIMESTAMP.getId());
        assertThat(ts, notNullValue());
    }

    @Test
    public void testMixed() {
        List<Attribute> atts = Arrays.asList(new Attribute[] {
                tempAtt,
                Attribute.TIMESTAMP,
                pressAtt
        });
        Map<Attribute, Object> stat = new HashMap<>();
        stat.put(tempAtt, 12);
        Request r = new Request(atts, stat);

        assertTrue(r.isSampled());
        assertTrue(r.getSampled().contains(pressAtt));
        assertThat(r.getSampled().size(), equalTo(1));
        assertTrue(r.getGenerated().contains(tempAtt));
        assertThat(r.getGenerated().size(), equalTo(1));

        // Check sample pipeline, generated timestamp
        List<Attribute> opAtts = Arrays.asList(new Attribute[] {
                pressAtt
        });
        SamplePipeline s = r.createPipeline(opAtts);
        boolean hasTimestampMod = false;
        boolean hasReorderMod = false;
        boolean hasStaticMod = false;
        for (Modifier sm : s.getModifiers()) {
            if (sm instanceof TimestampAdder) {
                hasTimestampMod = true;
            } else if (sm instanceof Reorder) {
                hasReorderMod = true;
            } else if (sm instanceof StaticAppender) {
                hasStaticMod = true;
            }
        }
        assertTrue(hasTimestampMod);
        assertTrue(hasReorderMod);
        assertTrue(hasStaticMod);
    }

}
