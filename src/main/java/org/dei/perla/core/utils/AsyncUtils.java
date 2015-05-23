package org.dei.perla.core.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A collection of utility methods for managing tasks running asynchronously.
 *
 * @author Guido Rota 14/05/15.
 */
public final class AsyncUtils {

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Runs a {@link Runnable} task in a {@link Thread} different than the
     * current one.
     *
     * @param task task to run in a {@link Thread} different than the current
     *             one.
     */
    public static void runInNewThread(Runnable task) {
        pool.submit(task);
    }

}
