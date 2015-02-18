package org.dei.perla.core.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A convenience class for creating new <code>Script</code> objects. Supports
 * method chaining through the <code>add</code> method to simplify script
 * creation.
 *
 * @author Guido Rota (2014)
 *
 */
public class ScriptBuilder {

    private final List<Attribute> emit = new ArrayList<>();
	private Instruction first = null;
	private Instruction last = null;

	public static ScriptBuilder newScript() {
		return new ScriptBuilder();
	}

	// Private constructor, new instances of the ScriptBuilder class have to be
	// created using the static newScript method.
	private ScriptBuilder() {
	}

	public ScriptBuilder add(Instruction in) {
		if (first == null) {
			first = last = in;
		} else {
			last.setNext(in);
			last = in;
		}

        if (in instanceof PutInstruction) {
            PutInstruction put = (PutInstruction) in;
            Attribute a = Attribute.create(put.getAttribute());
            if (!emit.contains(a)) {
                emit.add(a);
            }
        }

		return this;
	}

    public ScriptBuilder extraEmit(List<Attribute> atts) {
        for (Attribute a : atts) {
            if (emit.contains(a)) {
                continue;
            }
            emit.add(a);
        }
        return this;
    }

	public Script buildScript(String name) {
		if (!(last instanceof StopInstruction)) {
			this.add(new StopInstruction());
		}
		return new Script(name, first, emit, Collections.emptyList());
	}

    public Instruction getCode() {
        return first;
    }

}
