package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Sample;
import org.dei.perla.core.sample.SamplePipeline;
import org.dei.perla.core.sample.SamplePipeline.PipelineBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class PeriodicTaskTest {

    private static final FakeOperation FAKE_OP = new FakeOperation();
    private static final Object[] EMPTY_SAMPLE = new Object[0];
    private static final SamplePipeline EMPTY_PIPELINE;
    static {
        PipelineBuilder pb = SamplePipeline.newBuilder(
                Collections.emptyList());
        EMPTY_PIPELINE = pb.create();
    }

    @Test
    public void testDirect() {
        CountingHandler countHandler = new CountingHandler();
        PeriodicTask task = new PeriodicTask(FAKE_OP, countHandler, 1,
                EMPTY_PIPELINE);
        task.setInputPeriod(1);
        task.start();

        assertThat(task.errorPercent(), equalTo(0));

        for (int i = 0; i < 10; i++) {
            task.newSample(EMPTY_SAMPLE);
        }
        assertThat(countHandler.getCount(), equalTo(10l));
    }

    @Test
    public void testRatioNoError() {
        int inputPeriod = 5;
        int outputPeriod = 10;
        CountingHandler countHandler = new CountingHandler();
        PeriodicTask task = new PeriodicTask(FAKE_OP, countHandler,
                outputPeriod, EMPTY_PIPELINE);
        task.setInputPeriod(inputPeriod);
        task.start();

        assertThat(task.errorPercent(), equalTo(0));

        int samples = 10000;
        for (int i = 0; i < samples; i++) {
            task.newSample(EMPTY_SAMPLE);
        }

        long ratio = outputPeriod / inputPeriod;
        assertThat(countHandler.getCount(), equalTo(samples / ratio));
    }

    @Test
    public void testRatioExcess() {
        int inputPeriod = 4;
        int outputPeriod = 11;
        CountingHandler countHandler = new CountingHandler();
        PeriodicTask task = new PeriodicTask(FAKE_OP, countHandler,
                outputPeriod, EMPTY_PIPELINE);
        task.setInputPeriod(inputPeriod);
        task.start();

        assertThat(task.errorPercent(), not(equalTo(0)));
        assertThat(task.errorPercent(), greaterThan(0));

        BigDecimal ipBig = BigDecimal.valueOf(inputPeriod);
        BigDecimal opBig = BigDecimal.valueOf(outputPeriod);
        long ratio = opBig.divide(ipBig, RoundingMode.HALF_EVEN).longValue();
        long samples = 10000 * ratio;
        for (long i = 0; i < samples; i++) {
            task.newSample(EMPTY_SAMPLE);
        }

        assertThat(countHandler.getCount(), equalTo(samples / ratio));
    }

    @Test
    public void testRatioDefect() {
        int inputPeriod = 5;
        int outputPeriod = 11;
        CountingHandler countHandler = new CountingHandler();
        PeriodicTask task = new PeriodicTask(FAKE_OP, countHandler,
                outputPeriod, EMPTY_PIPELINE);
        task.setInputPeriod(inputPeriod);
        task.start();

        assertThat(task.errorPercent(), not(equalTo(0)));
        assertThat(task.errorPercent(), lessThan(0));

        BigDecimal ipBig = BigDecimal.valueOf(inputPeriod);
        BigDecimal opBig = BigDecimal.valueOf(outputPeriod);
        long ratio = opBig.divide(ipBig, RoundingMode.HALF_EVEN).longValue();
        long samples = 10000 * ratio;
        for (long i = 0; i < samples; i++) {
            task.newSample(EMPTY_SAMPLE);
        }

        assertThat(countHandler.getCount(), equalTo(samples / ratio));
    }

    private static class FakeOperation extends PeriodicOperation {

        private FakeOperation() {
            super("fake", Collections.emptyList());
        }

        @Override
        protected void setSamplingPeriod(long period) {

        }

        @Override
        protected void doStop(Consumer<Operation> handler) {

        }

    }

    private class CountingHandler implements TaskHandler {

        private long count;

        public long getCount() {
            return count;
        }

        @Override
        public void complete(Task task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void data(Task task, Sample result) {
            count += 1;
        }

        @Override
        public void error(Task task, Throwable cause) {
            throw new RuntimeException();
        }

    }

}
