package org.dei.perla.core.fpc.engine;

/**
 * Callback interface for a <code>Script</code> debugger.
 *
 * @author Guido Rota (2014)
 *
 */
public interface ScriptDebugger {

	/**
	 * <p>
	 * Callback method invoked by the execution engine when a breakpoint
	 * instruction is encountered.
	 * </p>
	 *
	 * <p>
	 * <code>Script</code> execution can be altered using the
	 * <code>Runner</code> instance passed as parameter.
	 * <ul>
	 * <li>To STEP the debugger to the next instruction invoke the
	 * <code>Runner.setBreakpoint()</code> method and return</li>
	 * <li>To CONTINUE until the next breakpoint or script termination just
	 * return</li>
	 * <li>To STOP the execution invoke the <code>Runner.cancel()</code> method
	 * and return</li>
	 * </ul>
	 * </p>
	 *
	 *
	 * @param runner
	 * @param script
	 *            <code>Script</code> being debugged
	 * @param instruction
	 *            Current <code>Instruction</code> (not executed yet)
	 */
	public void breakpoint(Runner runner, Script script, Instruction instruction);

}
