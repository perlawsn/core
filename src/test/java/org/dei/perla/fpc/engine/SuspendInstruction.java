package org.dei.perla.fpc.engine;

public class SuspendInstruction implements Instruction {

	private boolean suspended;
	private Instruction next;
	
	@Override
	public void setNext(Instruction instruction) {
		next = instruction;
		suspended = false;
	}

	@Override
	public Instruction next() {
		return next;
	}
	
	public void waitSuspend() throws InterruptedException {
		synchronized (this) {
			while (!suspended) {
				this.wait();
			}
		}
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		if (suspended == false) {
			synchronized (this) {
				suspended = true;
				runner.suspend();
				this.notifyAll();
				return this;
			}
		} else {
			return next;
		}
	}


}
