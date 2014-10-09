package org.dei.perla.fpc.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.engine.Record;
import org.dei.perla.utils.Check;

/**
 * <p>
 * An immutable pipeline of {@link RecordModifier}s for adding new fields to an
 * existing {@link Record}.
 * </p>
 * 
 * <p>
 * New {@code RecordPipeline} objects are created using a
 * {@code PipelineBuilder}, which can be obtained through the
 * {@code RecordPipeline.newBuilder()} method.
 * </p>
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

		private List<RecordModifier> modifierList = null;

		private PipelineBuilder() {
		}

		/**
		 * Appends a new {@link RecordModifier} to the end of the
		 * {@code RecordPipeline} being built.
		 * 
		 * @param modifier
		 *            {@link RecordModifier} to be added
		 */
		public void add(RecordModifier modifier) {
			if (modifierList == null) {
				modifierList = new ArrayList<>();
			}

			modifierList.add(modifier);
		}

		/**
		 * Appends all the {@link RecordModifier}s contained in the pipeline
		 * passed as parameter to the end of the {@code RecordPipeline} being
		 * built.
		 * 
		 * @param pipeline
		 *            Pipeline containing the {@link RecordModifier}s to add
		 */
		public void add(RecordPipeline pipeline) {
			if (pipeline instanceof EmptyPipeline) {
				return;
			}

			if (modifierList == null) {
				modifierList = new ArrayList<>();
			}

			ModifierPipeline p = (ModifierPipeline) pipeline;
			modifierList.addAll(p.modifierList);
		}

		/**
		 * Appends all the {@link RecordModifier}s passed as parameter to the
		 * end of the {@code RecordPipeline} being built.
		 * 
		 * @param modifiers
		 *            Collection of {@link RecordModifier}s to be added
		 */
		public void addAll(Collection<RecordModifier> modifiers) {
			if (modifierList == null) {
				modifierList = new ArrayList<>();
			}

			modifierList.addAll(modifiers);
		}

		/**
		 * Creates a new immutable {@code RecordPipeline} containing all
		 * {@link RecordModifier}s added to the builder
		 * 
		 * @return
		 */
		public RecordPipeline create() {
			if (Check.nullOrEmpty(modifierList)) {
				return RecordPipeline.EMPTY;
			}

			Set<Attribute> attributeSet = new HashSet<>();
			for (RecordModifier modifier : modifierList) {
				attributeSet.addAll(modifier.attributes());
			}
			return new ModifierPipeline(modifierList, attributeSet);
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

		private final List<RecordModifier> modifierList;
		private final Set<Attribute> attributeSet;

		private ModifierPipeline(List<RecordModifier> modifierList,
				Set<Attribute> attributeSet) {
			this.modifierList = modifierList;
			this.attributeSet = attributeSet;
		}

		@Override
		public Set<Attribute> attributes() {
			return attributeSet;
		}

		@Override
		public Record run(Record source) {
			Map<String, Object> recordMap = new HashMap<>();
			for (RecordModifier modifier : modifierList) {
				modifier.process(source, recordMap);
			}
			Record computed = Record.from(recordMap);
			return Record.merge(computed, source);
		}
	}

}
