package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;

import java.util.*;
import java.util.function.Consumer;

public final class Scheduler {

    // Used to order operations by number of attributes
    private static final Comparator<Operation> attComp =
            (Operation o1, Operation o2) -> {
        int o1s = o1.getAttributes().size();
        int o2s = o2.getAttributes().size();

        if (o1s < o2s) {
            return -1;
        } else if (o1s > o2s) {
            return 1;
        } else {
            return 0;
        }
    };

    private volatile boolean schedulable = true;

    private final List<? extends Operation> get;
    private final List<? extends Operation> set;
    private final List<? extends Operation> periodic;
    private final List<? extends Operation> async;

    public Scheduler(List<? extends Operation> get,
            List<? extends Operation> set,
            List<? extends Operation> periodic,
            List<? extends Operation> async) {

        this.get = get;
        this.set = set;
        this.periodic = periodic;
        this.async = async;

        Collections.sort(this.get, attComp);
        Collections.sort(this.set, attComp);
        Collections.sort(this.periodic, attComp);
        Collections.sort(this.async, attComp);
    }

    protected Operation set(Collection<Attribute> req, boolean strict)
            throws IllegalStateException {
        return bestFit(set, strict, req);
    }

    protected Operation get(List<Attribute> req, boolean strict)
            throws IllegalStateException {
        return bestFit(get, strict, req);
    }

    protected Operation periodic(List<Attribute> req, boolean strict)
            throws IllegalStateException {
        return bestFit(periodic, strict, req);
    }

    protected Operation async(List<Attribute> req, boolean strict)
            throws IllegalStateException {
        return bestFit(async, strict, req);
    }

    // Protected modifier allows access to this method during tests
    protected Operation bestFit(List<? extends Operation> ops, boolean strict,
            Collection<Attribute> req) throws IllegalStateException {
        if (!schedulable) {
            throw new IllegalStateException("Scheduler has been stopped.");
        }

        int score = 0;
        Operation match = null;
        for (Operation op : ops) {
            int s = getScore(op, req);
            if (s > score) {
                score = s;
                match = op;
            }
        }

        // Return null match when scheduling is strict and the selected
        // operation cannot fully answer the user's query
        if (match != null && strict && score != req.size()) {
            return null;
        }

        return match;
    }

    // The score is simply the number of requested attributes that are
    // present in the operation.
    // Protected modifier allows access to this method during tests
    protected int getScore(Operation o, Collection<Attribute> req) {
        int score = 0;
        for (Attribute oa : o.getAttributes()) {
            for (Attribute ra : req) {
                if (ra.match(oa)) {
                    score++;
                    break;
                }
            }
        }
        return score;
    }

    protected Operation getGetOperation(String id) {
        return findById(get, id);
    }

    protected Operation getSetOperation(String id) {
        return findById(set, id);
    }

    protected Operation getPeriodicOperation(String id) {
        return findById(periodic, id);
    }

    private Operation findById(Collection<? extends Operation> ops, String id) {
        for (Operation op : ops) {
            if (op.getId().equals(id)) {
                return op;
            }
        }
        return null;
    }

    protected void stop(Consumer<Void> h) {
        if (!schedulable) {
            return;
        }
        Consumer<Operation> stopHandler = new SchedulerStopHandler(h);
        schedulable = false;

        get.forEach(op -> op.stop(stopHandler));
        set.forEach(op -> op.stop(stopHandler));
        periodic.forEach(op -> op.stop(stopHandler));
        async.forEach(op -> op.stop(stopHandler));
    }

    /**
     *
     * @author Guido Rota (2014)
     *
     */
    private class SchedulerStopHandler implements Consumer<Operation> {

        private final Consumer<Void> parentStopHandler;
        private final Collection<Operation> ops;

        private SchedulerStopHandler(Consumer<Void> parentStopHandler) {
            this.parentStopHandler = parentStopHandler;
            ops = new HashSet<>();
            ops.addAll(get);
            ops.addAll(set);
            ops.addAll(periodic);
            ops.addAll(async);
        }

        @Override
        public void accept(Operation o) {
            synchronized (ops) {
                ops.remove(o);
                if (ops.isEmpty()) {
                    parentStopHandler.accept(null);
                }
            }
        }

    }

}
