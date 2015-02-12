package org.dei.perla.core.fpc.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.engine.Record;
import org.dei.perla.core.utils.Check;

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
public abstract class RecordPipeline {

	/**
	 * An empty {@code RecordPipeline}. Does not alter the {@link Record}
	 * content.
	 */
	public static final RecordPipeline EMPTY = new EmptyPipeline();

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
	 * Returns a collection of all {@link Attribute}s that this
	 * {@code RecordPipeline} adds to the processed {@link Record}
	 *
	 * @return {@link Attribute}s added to the processed {@link Record}
	 */
	public abstract Collection<Attribute> attributes();

	/**
	 * Runs a {@link Record} through the {@code RecordPipeline}.
	 *
	 * @param source
	 *            {@link Record} to be processed
	 * @return New {@link Record} produced by the pipeline
	 */
	public abstract Record run(Record source);

	/**
	 * A builder class for creating new {@code RecordPipeline} objects.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class PipelineBuilder {

		private List<RecordModifier> mods = null;

		private PipelineBuilder() {
		}

		/**
		 * Appends a new {@link RecordModifier} to the end of the
		 * {@code RecordPipeline} being built.
		 *
		 * @param modifier
		 *            {@link RecordModifier} to be added
		 */
		public void add(RecordModifier mod) {
			if (mods == null) {
				mods = new ArrayList<>();
			}

			mods.add(mod);
		}

		/**
		 * Appends all the {@link RecordModifier}s contained in the pipeline
		 * passed as parameter to the end of the {@code RecordPipeline} being
		 * built.
		 *
		 * @param pipeline
		 *            Pipeline containing the {@link RecordModifier}s to add
		 */
		public void add(RecordPipeline p) {
			if (p instanceof EmptyPipeline) {
				return;
			}

			if (mods == null) {
				mods = new ArrayList<>();
			}

			ModifierPipeline mp = (ModifierPipeline) p;
			mods.addAll(mp.mods);
		}

		/**
		 * Appends all the {@link RecordModifier}s passed as parameter to the
		 * end of the {@code RecordPipeline} being built.
		 *
		 * @param modifiers
		 *            Collection of {@link RecordModifier}s to be added
		 */
		public void addAll(Collection<RecordModifier> newMods) {
			if (mods == null) {
				mods = new ArrayList<>();
			}

			mods.addAll(newMods);
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

			Set<Attribute> atts = new HashSet<>();
            mods.forEach(m -> atts.addAll(m.attributes()));
			return new ModifierPipeline(mods, atts);
		}

	}

	/**
	 * An empty {@code RecordPipeline}. This pipeline does not apply any
	 * modifications to the {@link Record}s that run through it.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class EmptyPipeline extends RecordPipeline {

		@Override
		public Set<Attribute> attributes() {
			return Collections.emptySet();
		}

		@Override
		public Record run(Record source) {
			return source;
		}

	}

	/**
	 * A pipeline of {@link RecordModifier}s that are applied sequentially to
	 * add new {@link Record} fields.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class ModifierPipeline extends RecordPipeline {

		private final List<RecordModifier> mods;
		private final Set<Attribute> atts;

		private ModifierPipeline(List<RecordModifier> mods, Set<Attribute> atts) {
			this.mods = mods;
			this.atts = atts;
		}

		@Override
		public Set<Attribute> attributes() {
			return atts;
		}

		@Override
		public Record run(Record source) {
			Map<String, Object> rm = new HashMap<>();
			for (RecordModifier m : mods) {
				m.process(source, rm);
			}
			Record computed = Record.from(rm);
			return Record.merge(computed, source);
		}
	}

}
