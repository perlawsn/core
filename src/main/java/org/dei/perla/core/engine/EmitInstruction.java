package org.dei.perla.core.engine;

/**
 * Adds the current record in the record list that will be returned upon
 * successful <code>Script</code> completion. For more information about record
 * management refer to the <code>PutInstruction</code>.
 *
 * @author Guido Rota (2014)
 *
 */
public class EmitInstruction extends BasicInstruction {

	@Override
	protected void runBasic(Runner runner) throws ScriptException {
		runner.ctx.emitSample();
	}

}
