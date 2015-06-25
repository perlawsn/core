package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.sample.Sample;
import org.dei.perla.core.sample.SamplePipeline;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class BaseOperationTest {

    @Test
    public void testScheduling() {
        TaskHandler handler = new TestTaskHandler(0);
        TestOperation op = new TestOperation();

        BaseTask task1 = op.schedule(Collections.emptyMap(), handler);
        assertThat(task1, notNullValue());
        assertThat(op.taskCount(), equalTo(1));
        assertFalse(task1.isRunning());
        task1.start();
        assertTrue(task1.isRunning());

        BaseTask task2 = op.schedule(Collections.emptyMap(), handler);
        assertThat(task2, notNullValue());
        assertThat(op.taskCount(), equalTo(2));
        assertFalse(task2.isRunning());
        task2.start();
        assertTrue(task2.isRunning());

        task1.stop();
        assertFalse(task1.isRunning());
        assertTrue(task2.isRunning());
        assertThat(op.taskCount(), equalTo(1));
    }

    @Test
    public void stopTest() throws InterruptedException {
        TestTaskHandler handler = new TestTaskHandler(2);
        TestOperation op = new TestOperation();

        BaseTask task1 = op.schedule(Collections.emptyMap(), handler);
        assertThat(task1, notNullValue());
        assertFalse(task1.isRunning());
        task1.start();
        assertTrue(task1.isRunning());
        BaseTask task2 = op.schedule(Collections.emptyMap(), handler);
        assertThat(task2, notNullValue());
        assertFalse(task2.isRunning());
        task2.start();
        assertTrue(task2.isRunning());

        TestStopHandler stopHandler = new TestStopHandler();
        op.stop(stopHandler);
        assertTrue(stopHandler.isDone());
        assertFalse(op.isSchedulable());
        assertThat(op.taskCount(), equalTo(0));
        assertThat(handler.getCompletionCount(), equalTo(2));
        assertFalse(task1.isRunning());
        assertFalse(task2.isRunning());
    }

    private static class TestStopHandler implements Consumer<Operation> {

        private boolean done = false;

        public synchronized boolean isDone() throws InterruptedException {
            while (!done) {
                this.wait();
            }
            return true;
        }

        @Override
        public synchronized void accept(Operation object) {
            done = true;
        }

    }

    /**
     * Test implementation of the {@link TaskHandler} interface
     *
     * @author Guido Rota (2014)
     *
     */
    private static class TestTaskHandler implements TaskHandler {

        private final int eventCount;
        private int completionCount = 0;
        private int sampleCount = 0;
        private int errorCount = 0;

        public TestTaskHandler(int eventCount) {
            this.eventCount = eventCount;
        }

        public synchronized int getCompletionCount()
                throws InterruptedException {
            while (!isComplete()) {
                this.wait();
            }
            return completionCount;
        }

        private synchronized boolean isComplete() {
            if (completionCount + sampleCount + errorCount >= eventCount) {
                return true;
            }

            return false;
        }

        @Override
        public synchronized void complete(Task task) {
            completionCount++;
            this.notifyAll();
        }

        @Override
        public synchronized void data(Task task, Sample sample) {
            sampleCount++;
            this.notifyAll();
        }

        @Override
        public synchronized void error(Task task, Throwable cause) {
            errorCount++;
            this.notifyAll();
        }

    }

    /**
     * Test implementation of the {@link BaseOperation} class
     *
     * @author Guido Rota (2014)
     *
     */
    private static class TestOperation extends BaseOperation<TestTask> {

        public TestOperation() {
            super("test", Collections.emptyList());
        }

        @Override
        protected TestTask doSchedule(Map<String, Object> parameterMap,
                TaskHandler handler, SamplePipeline pipeline)
                throws IllegalArgumentException {
            TestTask task = new TestTask(this, handler, pipeline);
            add(task);
            return task;
        }

        @Override
        protected void doStop() {

        }

        @Override
        protected void doStop(Consumer<Operation> handler) {
            handler.accept(this);
        }

    }

    /**
     * Test implementation of the {@link BaseTask} class
     *
     * @author Guido Rota (2014)
     *
     */
    private static class TestTask extends BaseTask {

        public TestTask(BaseOperation<? extends BaseTask> operation,
                TaskHandler handler, SamplePipeline pipeline) {
            super(operation, handler, pipeline);
        }

    }

}
