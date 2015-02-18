package org.dei.perla.core.engine;

import java.util.Collections;
import java.util.List;

/**
 * A <code>Script</code> that can be executed by the Fpc execution engine. Each
 * <code>Script</code> is identified by a name and a linked list of
 * instructions.
 *
 * @author Guido Rota (2014)
 *
 */
public class Script {

	private final String name;
	private final Instruction first;

    // The attribute order in the emit list is important, as it will
    // correspond to the attribute order in every record created by this script
    private final List<Attribute> set;
    private final List<Attribute> emit;

	public Script(String name, Instruction first, List<Attribute> emit,
            List<Attribute> set) {
		this.name = name;
		this.first = first;
        this.emit = Collections.unmodifiableList(emit);
        this.set = Collections.unmodifiableList(set);
	}

	/**
	 * Name of the <code>Script</code>
	 *
	 * @return Script name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the first instruction of the <code>Script</code>
	 *
	 * @return First {@code Script} instruction
	 */
	public Instruction getCode() {
		return first;
	}

    /**
     * Returns the list of {@link Attribute}s that the {@code Script} gathers
     * from the remote device.
     *
     * It is important to note that the order of the {@link Atttibute}s in
     * this list is guaranteed to be the same as the {@link Attribute} order in
     * the records produced by the {@code Script}.
     *
     * @return {@link Attribute}s emitted by the {@code Script}
     */
    public List<Attribute> getEmit() {
        return emit;
    }

    /**
     * Returns the list of {@link Attribute}s that the {@code Script} sends
     * to the remote device.
     *
     * @return {@link Attribute}s set by the {@code Script}
     */
    public List<Attribute> getSet() {
        return set;
    }

}
