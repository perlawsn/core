package org.dei.perla.core.record;

import org.dei.perla.core.utils.Check;

import java.util.*;

/**
 * An immutable pipeline of {@link RecordModifier}s for adding new fields to an
 * existing {@link Record}.
 *
 * New {@code RecordPipeline} objects are created using a
 * {@code PipelineBuilder}, which can be obtained through the
 * {@code RecordPipeline.newBuilder()} method.
 *
 * @author Guido Rota (2014)
 *
 */
public class RecordPipeline {

	/**
	 * An empty {@code RecordPipeline}. Does not alter the {@link Record}
	 * content.
	 */
	public static final RecordPipeline EMPTY =
            new RecordPipeline(Collections.emptyList());

    public final List<RecordModifier> mods;
    public final List<Attribute> atts;

	/**
	 * Returns a new {@code PipelineBuilder} instance for creating new
	 * {@code RecordPipeline} objects.
	 *
	 * @return New {@code PipelineBuilder} object
	 */
	public static PipelineBuilder newBuilder() {
		return new PipelineBuilder();
	}

    /**
     * Private {@code RecordPipeline} constructor, new isntances must be
     * buiilt using the {@link PipelineBuilder} class.
     *
     * @param mods {@link RecordModifier} used by the pipeline
     */
    private RecordPipeline(List<RecordModifier> mods) {
        List<Attribute> atts = new ArrayList<>();
        mods.forEach(m -> atts.addAll(m.getAttributes()));
        this.atts = Collections.unmodifiableList(atts);
        this.mods = Collections.unmodifiableList(mods);
    }

	/**
	 * Returns a collection of all {@link Attribute}s that this
	 * {@code RecordPipeline} adds to the processed {@link Record}
	 *
	 * @return {@link Attribute}s added to the processed {@link Record}
	 */
	public Collection<Attribute> attributes() {
        return atts;
    }

	/**
	 * Runs a {@link Record} through the {@code RecordPipeline}.
	 *
	 * @param source
	 *            {@link Record} to be processed
	 * @return New {@link Record} produced by the pipeline
	 */
	public Record run(Record source) {
        if (mods.isEmpty()) {
            return source;
        }

        Object[] s = source.getFields();
        Object[] r = Arrays.copyOf(s, s.length + mods.size());
        int i = s.length;
        for (RecordModifier m : mods) {
            m.process(r, i);
            i += m.getAttributes().size();
        }

        List<Attribute> sa = source.getAttributes();
        List<Attribute> al = new ArrayList<>(atts.size() + sa.size());
        al.addAll(sa);
        al.addAll(atts);
        return new Record(al, r);
    }
	/**
	 * A builder class for creating new {@code RecordPipeline} objects.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class PipelineBuilder {

        private Set<Attribute> atts = new HashSet<>();
		private List<RecordModifier> mods = new ArrayList<>();

        /**
         * Private constructur, {@code PipelineBuilder} instances are
         * supposed to be built using the static newBuilder method available
         * in the {@link RecordPipeline} class.
         */
		private PipelineBuilder() {}

		/**
		 * Appends a new {@link RecordModifier} to the end of the
		 * {@code RecordPipeline} being built.
		 *
		 * @param modifier
		 *            {@link RecordModifier} to be added
		 */
		public void add(RecordModifier mod) {
            if (!Collections.disjoint(atts, mod.getAttributes())) {
                throw new RuntimeException("Record modifier is attempting to " +
                        "overwrite an existing field");
            }
            atts.addAll(mod.getAttributes());
			mods.add(mod);
		}

		/**
		 * Creates a new immutable {@code RecordPipeline} containing all
		 * {@link RecordModifier}s added to the builder
		 *
		 * @return
		 */
		public RecordPipeline create() {
			if (Check.nullOrEmpty(mods)) {
				return RecordPipeline.EMPTY;
			}

			return new RecordPipeline(mods);
		}

	}

}
