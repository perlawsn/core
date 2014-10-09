package org.dei.perla.channel;

public class MockChannel extends AbstractChannel {

	// Ensures that the channel fetched a request and is waiting to be resumed
	private boolean waiting = false;
	// When paused the channel does not process requests
	private boolean paused = false;
	
	public MockChannel() {
		super("mock");
	}

	@Override
	public Payload handleRequest(IORequest request) throws InterruptedException {
		LoopbackRequest loopbackRequest;
		LoopbackPayload response;

		loopbackRequest = (LoopbackRequest) request;

		synchronized (this) {
			response = new LoopbackPayload(loopbackRequest.getMessage(), paused);
			if (paused) {
				waiting = true;
				this.notify();
				this.wait();
			}
			waiting = false;
		}

		return response;
	}

	public synchronized void pause() {
		paused = true;
	}

	public synchronized void resume() throws InterruptedException {
		if (waiting == false && !this.isClosed()) {
			this.wait();
		}
		paused = false;
		this.notify();
	}

	public int pendingRequestCount() {
		return super.pendingRequestCount();
	}

}
