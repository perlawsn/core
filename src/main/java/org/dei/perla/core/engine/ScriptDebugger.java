package org.dei.perla.core.engine;

/**
 * Callback interface for a {@link Script} debugger.
 *
 * @author Guido Rota (2014)
 */
public interface ScriptDebugger {

	/**
	 * <p>
	 * Callback method invoked by the execution engine when a breakpoint
	 * instruction is encountered.
	 *
	 * <p>
	 * {@link Script} execution can be altered using the {@link Runner}
	 * instance passed as parameter.
	 *
	 * <ul>
	 * <li>To STEP the debugger to the next instruction invoke the
	 * {@link Runner#setBreakpoint()} method and return</li>
	 * <li>To CONTINUE until the next breakpoint or script termination just
	 * return</li>
	 * <li>To STOP the execution invoke the {@link Runner#cancel()} method
	 * and return</li>
	 * </ul>
	 *
	 * @param runner {@link Runner} that is executing the {@link Script}
	 * @param script {@link Script} being debugged
	 * @param instruction current {@link Instruction} (not yet executed)
	 */
	public void breakpoint(
			Runner runner,
			Script script,
			Instruction instruction);

}
