package org.dei.perla.core.engine;

public class PauseInstruction extends BasicInstruction {

	private Boolean pause = true;

	public void resume() {
		synchronized (this) {
			pause = false;
			this.notify();
		}
	}

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		synchronized (this) {
			try {
				while (pause) {
					this.wait();
				}
			} catch (InterruptedException e) {
				runner.cancel();
			}
		}
	}

}
