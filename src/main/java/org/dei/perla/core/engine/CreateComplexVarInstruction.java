package org.dei.perla.core.engine;

import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;

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
public class CreateComplexVarInstruction extends BasicInstruction {

    private final String name;
    private final Mapper mapper;

    public CreateComplexVarInstruction(String name, Mapper mapper) {
        this.name = name;
        this.mapper = mapper;
    }

    protected String getName() {
        return name;
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
                            + mapper.getMessageId() + "' for variable '" + name
                            + "' in create instruction");
        }
        runner.ctx.setVariable(name, object);
    }

}
