package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Task;
import org.dei.perla.core.fpc.TaskHandler;
import org.dei.perla.core.fpc.Sample;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LatchingTaskHandler implements TaskHandler {

    private int waitCount;
    private int count = 0;

    private Instant previousTime = null;
    private double avgPeriod = 0;

    private Throwable error;

    private final List<Sample> samples = new ArrayList<>();

    public LatchingTaskHandler(int waitCount) {
        this.waitCount = waitCount;
    }

    public synchronized int getCount() throws InterruptedException {
        while (waitCount > 0 && error == null) {
            wait();
        }
        if (error != null) {
            throw new RuntimeException(error);
        }
        return count;
    }

    public synchronized double getAveragePeriod() throws InterruptedException {
        while (waitCount > 0 && error == null) {
            wait();
        }
        if (error != null) {
            throw new RuntimeException(error);
        }
        return avgPeriod;
    }

    public synchronized List<Sample> getSamples() throws InterruptedException {
        while (waitCount > 0 && error == null) {
            wait();
        }
        if (error != null) {
            throw new RuntimeException(error);
        }
        return samples;
    }

    public synchronized Sample getLastSample() throws InterruptedException {
        if (waitCount > 0 && error == null) {
            wait();
        }
        if (error != null) {
            throw new RuntimeException(error);
        }
        return samples.get(samples.size() - 1);
    }

    public synchronized void awaitCompletion() throws InterruptedException {
        while (waitCount > 0 && error == null) {
            wait();
        }
        if (error != null) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public synchronized void complete(Task task) {
        waitCount = 0;
        notifyAll();
    }

    @Override
    public synchronized void data(Task task, Sample sample) {
        samples.add(sample);
        waitCount--;
        count++;

        Instant ts;
        if (sample.hasField("timestamp")) {
            ts = (Instant) sample.getValue("timestamp");
        } else {
            ts = Instant.now();
        }

        if (previousTime == null) {
            previousTime = ts;
        } else {
            long diff = ts.toEpochMilli() - previousTime.toEpochMilli();
            avgPeriod = (avgPeriod + diff) / count;
        }
        if (waitCount == 0) {
            notifyAll();
        }
    }

    @Override
    public synchronized void error(Task task, Throwable cause) {
        error = cause;
        notifyAll();
    }

}
