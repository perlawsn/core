package org.dei.perla.fpc.engine;

import org.dei.perla.message.FpcMessage;
import org.dei.perla.message.Mapper;

/**
 * <p>
 * An instruction for creating new <code>Script</code> variables.
 * </p>
 * 
 * <p>
 * <code>Script</code> variables are identified by a name and a message type.
 * The latter information is used to define which attributes can be accessed on
 * the variable and the appropriate <code>Mapper</code> to be used for
 * marshalling and unmarshalling data.
 * </p>
 * 
 * @author Guido Rota (2014)
 *
 */
public class CreateComplexInstruction extends BasicInstruction {

	private final String variable;
	private final Mapper mapper;

	public CreateComplexInstruction(String varialbe, Mapper mapper) {
		this.variable = varialbe;
		this.mapper = mapper;
	}
	
	protected String getVariable() {
		return variable;
	}
	
	protected Mapper getMapper() {
		return mapper;
	}

	@Override
	public void runBasic(Runner runner) throws ScriptException {
		FpcMessage object = mapper.createMessage();
		if (object == null) {
			throw new RuntimeException(
					"Unexpected error while creating message '"
							+ mapper.getMessageId() + "' for variable '" + variable
							+ "' in create instruction");
		}
		runner.ctx.setVariable(variable, object);
	}

}
