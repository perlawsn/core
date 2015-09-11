package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.fpc.TaskHandler;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Guido Rota 11/09/15.
 */
public class SchedulerTest {

    @Test
    public void testGetScore() {
        Scheduler s = new Scheduler(Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        List<Attribute> opAtts = Arrays.asList(new Attribute[] {
                Attribute.create("att1", DataType.INTEGER),
                Attribute.create("att2", DataType.FLOAT),
                Attribute.create("att3", DataType.BOOLEAN)
        });
        Operation op = new MockOperation(opAtts);

        assertThat(s.getScore(op, opAtts), equalTo(3));

        List<Attribute> req = Arrays.asList(new Attribute[] {
                Attribute.create("att1", DataType.NUMERIC),
                Attribute.create("att2", DataType.FLOAT),
                Attribute.create("att3", DataType.ANY)
        });
        assertThat(s.getScore(op, req), equalTo(3));

        req = Arrays.asList(new Attribute[] {
                Attribute.create("att2", DataType.ANY),
                Attribute.create("att1", DataType.INTEGER),
                Attribute.create("att3", DataType.NUMERIC)
        });
        assertThat(s.getScore(op, req), equalTo(2));
    }

    @Test
    public void testBestFit() {
        Scheduler s = new Scheduler(Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());

        List<Operation> ops = new ArrayList<>();
        List<Attribute> opAtts = Arrays.asList(new Attribute[] {
                Attribute.create("att1", DataType.INTEGER)
        });
        Operation op1 = new MockOperation(opAtts);
        opAtts = Arrays.asList(new Attribute[] {
                Attribute.create("att2", DataType.FLOAT)
        });
        Operation op2 = new MockOperation(opAtts);
        opAtts = Arrays.asList(new Attribute[] {
                Attribute.create("att3", DataType.BOOLEAN)
        });
        Operation op3 = new MockOperation(opAtts);
        opAtts = Arrays.asList(new Attribute[] {
                Attribute.create("att1", DataType.INTEGER),
                Attribute.create("att2", DataType.FLOAT),
                Attribute.create("att4", DataType.STRING)
        });
        Operation op4 = new MockOperation(opAtts);
        ops.add(op1);
        ops.add(op2);
        ops.add(op3);
        ops.add(op4);

        List<Attribute> req = Arrays.asList(new Attribute[] {
                Attribute.create("att3", DataType.BOOLEAN)
        });
        assertThat(s.bestFit(ops, true, req), equalTo(op3));

        req = Arrays.asList(new Attribute[] {
                Attribute.create("att3", DataType.NUMERIC)
        });
        assertThat(s.bestFit(ops, true, req), nullValue());

        req = Arrays.asList(new Attribute[] {
                Attribute.create("att2", DataType.NUMERIC)
        });
        assertThat(s.bestFit(ops, true, req), equalTo(op2));

        req = Arrays.asList(new Attribute[] {
                Attribute.create("att1", DataType.NUMERIC),
                Attribute.create("att2", DataType.NUMERIC),
                Attribute.create("att4", DataType.NUMERIC)
        });
        assertThat(s.bestFit(ops, false, req), equalTo(op4));
        assertThat(s.bestFit(ops, true, req), nullValue());
    }

    /**
     * Mockup Operation class used for testing the Scheduler class
     *
     * @author Guido Rota (2015)
     */
    private static final class MockOperation implements Operation {

        private final List<Attribute> atts;

        private MockOperation(List<Attribute> atts) {
            this.atts = Collections.unmodifiableList(atts);
        }

        @Override
        public String getId() {
            return "mock operation";
        }

        @Override
        public List<Attribute> getAttributes() {
            return atts;
        }

        @Override
        public BaseTask schedule(Map<String, Object> parameterMap, TaskHandler handler)
                throws IllegalArgumentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public BaseTask schedule(Map<String, Object> parameterMap, TaskHandler handler,
                SamplePipeline pipeline)
                throws IllegalArgumentException, IllegalStateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSchedulable() {
            return false;
        }

        @Override
        public void stop(Consumer<Operation> handler) {
            throw new UnsupportedOperationException();
        }

    }

}
