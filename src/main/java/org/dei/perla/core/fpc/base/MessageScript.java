package org.dei.perla.core.fpc.base;

import org.dei.perla.core.engine.Script;
import org.dei.perla.core.message.Mapper;

/**
 * A wrapper object that stores the relation between a {@link Message} type,
 * identified by a {@link Mapper} object, and the {@link Script} that must be
 * employed to process it.
 *
 * @author Guido Rota 31/03/15.
 */
public final class MessageScript {

    private final Script script;
    private final Mapper mapper;
    private final boolean sync;
    private final String variable;
    private final int base;

    public MessageScript(Script script, Mapper mapper, boolean sync,
            String variable, int base) {
        this.script = script;
        this.mapper = mapper;
        this.sync = sync;
        this.variable = variable;
        this.base = base;
    }

    public Script getScript() {
        return script;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public boolean isSync() {
        return sync;
    }

    public String getVariable() {
        return variable;
    }

    public int getBase() {
        return base;
    }

}
