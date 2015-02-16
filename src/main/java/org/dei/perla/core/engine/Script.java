package org.dei.perla.core.engine;

import org.dei.perla.core.fpc.Attribute;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
    private final Set<Attribute> emit;
    private final Set<Attribute> set;
    private final Map<Attribute, Integer> attIdx;

	public Script(String name, Instruction first, Set<Attribute> emit,
            Set<Attribute> set, Map<Attribute, Integer> attIdx) {
		this.name = name;
		this.first = first;
        this.emit = Collections.unmodifiableSet(emit);
        this.set = Collections.unmodifiableSet(set);
        this.attIdx = Collections.unmodifiableMap(attIdx);
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
     * Returns the number of {@link Attribute}s that the {@code Script}
     * retrieves from the remote device.
     *
     * @return Number of {@Attribute}s emitted by the {@code Script}
     */
    public int getEmitCount() {
        return emit.size();
    }

    /**
     * Returns the list of {@link Attribute}s that the {@code Script} gathers
     * from the remote device.
     *
     * @return {@link Attribute}s emitted by the {@code Script}
     */
    public Set<Attribute> getEmit() {
        return emit;
    }

    /**
     * Returns the list of {@link Attribute}s that the {@code Script} sends
     * to the remote device.
     *
     * @return {@link Attribute}s set by the {@code Script}
     */
    public Set<Attribute> getSet() {
        return set;
    }

    /**
     * Returns a {@link Map} containing the position that every emitted
     * {@link Attribute} will occupy in the output records created by the
     * {@code Script}.
     *
     * @return {@link Attribute} position in the output record
     */
    public Map<Attribute, Integer> getIndexes() {
        return attIdx;
    }

}
