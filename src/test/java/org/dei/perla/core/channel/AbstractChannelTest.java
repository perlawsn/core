package org.dei.perla.core.channel;

import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class AbstractChannelTest {

	private static final String testMessage = "TestMessage";

	@Test
	public void testSingleRequest() throws InterruptedException,
			ExecutionException {
		MockChannel channel = new MockChannel();

		assertNotNull(channel);
		assertFalse(channel.isClosed());

		LoopbackRequest req = new LoopbackRequest(testMessage);
		SynchronizerIOHandler handler = new SynchronizerIOHandler();
		channel.submit(req, handler);
        req.waitProcessed();
		LoopbackPayload response = (LoopbackPayload) handler.getResult()
				.orElseThrow(RuntimeException::new);
		assertTrue(response.getMessage().equals(testMessage));

		channel.close();
		assertTrue(channel.isClosed());
	}

	@Test(expected = ChannelException.class)
	public void testClosedChannelSubmit() throws InterruptedException {
		MockChannel channel = new MockChannel();
		LoopbackRequest request;

		channel.close();
		assertTrue(channel.isClosed());

		request = new LoopbackRequest(testMessage);
		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
		channel.submit(request, syncHandler);
	}

	@Test
	public void testPausedRequest()
            throws InterruptedException, ExecutionException {
		MockChannel channel = new MockChannel();

		assertNotNull(channel);
		assertFalse(channel.isClosed());

		channel.pause();
		SynchronizerIOHandler syncHandler = new SynchronizerIOHandler();
        LoopbackRequest req = new LoopbackRequest(testMessage);
		channel.submit(req, syncHandler);
        req.waitPaused();
		channel.resume();

		LoopbackPayload response = (LoopbackPayload) syncHandler.getResult()
				.orElseThrow(RuntimeException::new);
		assertTrue(response.getMessage().equals(testMessage));

		channel.close();
		assertTrue(channel.isClosed());
	}

	@Test
	public void testMultipleRequests() throws InterruptedException {
		MockChannel channel = new MockChannel();
		LoopbackRequest request;

		int requestCount = 10;
		final AtomicInteger responseCount = new AtomicInteger(0);
		final CountDownLatch latch = new CountDownLatch(requestCount);

		channel.pause();
		for (int i = 0; i < requestCount; i++) {
			request = new LoopbackRequest(String.valueOf(i));
			channel.submit(request, new IOHandler() {
				@Override
				public void complete(IORequest request, Optional<Payload> result) {
					LoopbackPayload loopRes = (LoopbackPayload) result
							.orElseThrow(RuntimeException::new);
					assertThat(Integer.valueOf(loopRes.getMessage()),
							greaterThanOrEqualTo(0));
					assertThat(Integer.valueOf(loopRes.getMessage()),
							lessThanOrEqualTo(10));
					responseCount.addAndGet(1);
					latch.countDown();
				}

				@Override
				public void error(IORequest request, Throwable cause) {
					latch.countDown();
				}
			});
		}
		assertThat(channel.pendingRequestCount(), greaterThan(1));
		channel.resume();
		latch.await();

		assertThat(responseCount.get(), equalTo(requestCount));
		channel.close();
		assertTrue(channel.isClosed());
	}

	@Test
	public void testCancellation() throws InterruptedException {
		MockChannel channel = new MockChannel();
		IOTask task;
		final CountDownLatch latch = new CountDownLatch(10);

		IOHandler handler = new IOHandler() {
			@Override
			public void complete(IORequest request, Optional<Payload> result) {
				assertTrue(request instanceof LoopbackRequest);
				LoopbackRequest loopReq = (LoopbackRequest) request;
				assertThat(Integer.valueOf(loopReq.getMessage()),
						not(equalTo(5)));
				latch.countDown();
			}

			@Override
			public void error(IORequest request, Throwable cause) {
				assertTrue(request instanceof LoopbackRequest);
				LoopbackRequest loopReq = (LoopbackRequest) request;
				assertThat(Integer.valueOf(loopReq.getMessage()), equalTo(5));
				assertTrue(cause instanceof IOTaskCancelledException);
				latch.countDown();
			}
		};

		channel.pause();
		for (int i = 0; i < 10; i++) {
			LoopbackRequest request = new LoopbackRequest(String.valueOf(i));
			task = channel.submit(request, handler);
			assertFalse(task.isCancelled());
			if (i == 5) {
				task.cancel();
				assertTrue(task.isCancelled());
			}
		}
		assertThat(channel.pendingRequestCount(), greaterThan(1));
		channel.resume();
		latch.await();
		channel.close();
	}

	@Test
	public void testCancelOnClose() throws InterruptedException {
		MockChannel channel = new MockChannel();
		int requestCount = 10;
		final AtomicInteger cancelled = new AtomicInteger(0);
		final CountDownLatch latch = new CountDownLatch(requestCount);

		IOHandler handler = new IOHandler() {
			@Override
			public void complete(IORequest request, Optional<Payload> result) {
                latch.countDown();
			}

			@Override
			public void error(IORequest request, Throwable cause) {
				cancelled.addAndGet(1);
				latch.countDown();
			}
		};

		channel.pause();
		for (int i = 0; i < requestCount; i++) {
			LoopbackRequest request = new LoopbackRequest(String.valueOf(i));
			channel.submit(request, handler);
		}
		channel.close();
		channel.resume();
		latch.await();

		// Due to the particular implementation of AbstractChannel, one request
		// could be scheduled for execution before the channel gets closed,
		// hence the requestCount -1 and the greaterThanOrEqualTo matcher
		assertThat(cancelled.get(), greaterThanOrEqualTo(requestCount - 1));
	}

}
