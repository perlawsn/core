package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.IDGenerator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Guido Rota 18/05/15.
 */
public class IncrementalIDGenerator implements IDGenerator {

    private final AtomicInteger id = new AtomicInteger(0);

    @Override
    public int generateID() {
        return id.getAndIncrement();
    }

    @Override
    public void releaseID(int id) { }

}
