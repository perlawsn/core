package org.dei.perla.core.channel;

import org.apache.log4j.Logger;
import org.dei.perla.core.utils.Conditions;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * An abstract {@code Channel} implementation with several convenience methods
 * for handling {@code IORequest}s.
 * </p>
 *
 * <p>
 * This {@code AbstractChannel} guarantees that all submitted requests are
 * performed sequentially in insertion order.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public abstract class AbstractChannel implements Channel {

	private final Logger log;

	private final String id;
	private final BlockingQueue<FutureIOTask> pending = new LinkedBlockingQueue<>();
	private volatile IOHandler asyncHandler = null;

	// Coordination between threads achieved through CAS
	private final AtomicBoolean stopped;
	// Worker thread used to sequentially perform the submitted requests
	private final Thread thread;

	public AbstractChannel(String id) {
		this.id = id;
		log = Logger.getLogger(this.getClass().getCanonicalName() + "_" + id);
		thread = new Thread(this::dispatch);
		stopped = new AtomicBoolean(false);
		thread.start();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setAsyncIOHandler(IOHandler handler)
			throws IllegalStateException {
		if (this.asyncHandler != null) {
			throw new IllegalStateException(
					"An IOHandler has already been set for this Channel");
		}
		this.asyncHandler = Conditions.checkNotNull(handler, "handler");
	}

	@Override
	public IOTask submit(IORequest request, IOHandler handler)
			throws ChannelException {
		if (stopped.get()) {
			throw new ChannelException();
		}
		FutureIOTask task = new FutureIOTask(request, handler);
		pending.add(task);
		return task;
	}

	private void dispatch() {
		FutureIOTask task;
		while (!stopped.get() && !Thread.currentThread().isInterrupted()) {
			try {
				task = pending.take();
				if (task.isCancelled()) {
					continue;
				}
				task.run();
			} catch (InterruptedException e) {
				// Thread exits on InterruptedException
				break;
			} catch (Exception e) {
				log.error("Unexpected Channel error", e);
			}
		}
		// Cancel pending tasks before exiting
		while ((task = pending.poll()) != null) {
			task.cancel();
		}
	}

	/**
	 * Notifies an interested component that data was received asynchronously
	 * from the remote device.
	 *
	 * @param result
	 *            Data asynchronously received from the remote device
	 */
	protected void notifyAsyncData(Payload result) {
		if (asyncHandler == null || stopped.get()) {
			return;
		}
		asyncHandler.complete(null, Optional.ofNullable(result));
	}

	/**
	 * Notifies an interested component that an exception occurred while
	 * processing a communication asynchronously initiated by the remote device.
	 *
	 * @param cause
	 *            Exception caught while handling an asynchronous communication
	 */
	protected void notifyAsyncError(Throwable cause) {
		if (asyncHandler == null || stopped.get()) {
			return;
		}
		log.error("Asynchronous reception error", cause);
		asyncHandler.error(null, cause);
	}

	/**
	 * Returns the pending queue size, which represents the number of requests
	 * waiting to be processed by this channel.
	 *
	 * @return Number of requests in the pending queue
	 */
	protected int pendingRequestCount() {
		return pending.size();
	}

	@Override
	public boolean isClosed() {
		return stopped.get();
	}

	@Override
	public void close() {
		if (!stopped.compareAndSet(false, true)) {
			return;
		}
		thread.interrupt();
	}

	/**
	 * <p>
	 * {@code IORequest} handler. This method is invoked by the main working
	 * thread to execute IOrequests submitted to this {@code Channel}.
	 * </p>
	 *
	 * <p>
	 * {@code AbstractChannel} implementations are required to encapsulate any
	 * exception or error received while performing the {@code IORequest} into a
	 * {@code ChannelException}. {@code InterruptedException} represents the
	 * only exception to this rule. {@code handleRequest} implementations must
	 * never consume this exception silently; it has to be be re-thrown to the
	 * caller instead, where it is appropriately handled by the main
	 * {@code AbstractChannel} thread.
	 * </p>
	 *
	 * @param request
	 *            {@code IORequest} to be performed.
	 * @return Response {@code Response} received by the device or by the
	 *         physical channel
	 * @throws ChannelException
	 *             if an error occurs while processing the
	 *             <code>IORequest</code>
	 * @throws InterruptedException
	 */
	public abstract Payload handleRequest(IORequest request)
			throws ChannelException, InterruptedException;

	/**
	 * Convenience implementation of the {@code IOTask} interface.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class FutureIOTask implements IOTask {

		private static final int NEW = 0;
		private static final int RUNNING = 1;
		private static final int FINISHED = 2;
		private static final int CANCELLED = 3;

		// State
		private AtomicInteger state = new AtomicInteger(NEW);

		private final IORequest request;
		private final IOHandler handler;

		public FutureIOTask(final IORequest request, final IOHandler handler) {
			this.request = request;
			this.handler = handler;
		}

		public void run() {
			if (!state.compareAndSet(NEW, RUNNING)) {
				// Task was cancelled or has already been run
				throw new IllegalStateException("Cannot start, IOTask has " +
						"already run");
			}

			try {
				Payload result = handleRequest(request);
				complete(result);

			} catch (ChannelException e) {
				log.error("An error occurred while processing an I/O Request", e);
				error(e);

			} catch (InterruptedException e) {
				// Close the entire Channel if an InterruptedException is
				// received. This will cause the interrupted status to
				// be correctly restored and all pending tasks to be
				// cancelled.
				error(new ChannelException("Thread interruption received " +
						"while processing I/O request", e));
				close();

			} catch (Exception e) {
                String msg = "Unexpected error while processing an I/O Request";
				log.error("msg", e);
				error(new ChannelException(msg, e));
			}
		}

		private void complete(Payload result) {
			if (!state.compareAndSet(RUNNING, FINISHED)) {
				return;
			}

			handler.complete(request, Optional.ofNullable(result));
		}

		private void error(Throwable cause) {
			if (!state.compareAndSet(RUNNING, FINISHED)) {
				return;
			}

			handler.error(request, cause);
		}

		@Override
		public boolean isDone() {
			return state.get() > RUNNING;
		}

		@Override
		public void cancel() {
			state.set(CANCELLED);
			handler.error(request, new IOTaskCancelledException());
		}

		@Override
		public IORequest getRequest() {
			return request;
		}

		@Override
		public boolean isCancelled() {
			return state.get() == CANCELLED;
		}

	}

}
