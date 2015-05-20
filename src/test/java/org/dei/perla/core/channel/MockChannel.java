package org.dei.perla.core.channel;

public class MockChannel extends AbstractChannel {

	// When paused the channel does not process requests
	private boolean paused = false;

	public MockChannel() {
		super("mock");
	}

	@Override
	public synchronized Payload handleRequest(IORequest request)
            throws InterruptedException {
        LoopbackRequest req = (LoopbackRequest) request;
        while (paused) {
            req.setPaused();
            this.wait();
        }
        req.setProcessed();
        return new LoopbackPayload(req.getMessage());
	}

	public synchronized void resume() {
		paused = false;
        this.notify();
	}

	public synchronized void pause() {
		paused = true;
	}

	public int pendingRequestCount() {
		return super.pendingRequestCount();
	}

}
