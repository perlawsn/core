package org.dei.perla.fpc.engine;


/**
 * Breakpoint instruction. Instructs the <code>ScriptEngine</code> to invoke the
 * debugger before running the following instructions.
 * 
 * @author Guido Rota (2014)
 *
 */
public class BreakpointInstruction extends BasicInstruction {

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		runner.setBreakpoint();
	}

}
