package org.dei.perla.core.sample;

import org.dei.perla.core.sample.SampleModifier.Reorder;
import org.dei.perla.core.sample.SampleModifier.StaticAppender;
import org.dei.perla.core.sample.SampleModifier.TimestampAppender;

import java.util.*;
import java.util.Map.Entry;

/**
 * An immutable pipeline of {@link SampleModifier}s for adding new fields to an
 * existing {@link Sample}.
 *
 * New {@code SamplePipeline} objects are created using a
 * {@code PipelineBuilder}, which can be obtained through the
 * {@code SamplePipeline.newBuilder()} method.
 *
 * @author Guido Rota (2014)
 *
 */
public class SamplePipeline {

    public final List<SampleModifier> mods;
    public final List<Attribute> atts;

    /**
     * Private {@code SamplePipeline} constructor, new isntances must be
     * buiilt using the {@link PipelineBuilder} class.
     *
     * @param mods {@link SampleModifier} used by the pipeline
     * @param attOrder {@link Attribute}s in the order required by the fpc user
     */
    private SamplePipeline(List<SampleModifier> mods,
            List<Attribute> atts) {
        this.mods = mods;
        this.atts = atts;
    }

    /**
     * Creates a passthrough {@code SamplePipeline} that creates a new sample
     * wihtout performing any modification to the source data
     *
     * @param atts sample attributes
     * @return a passthrough {@code SamplePipeline} that does not perform any
     * operation to the data received as an input.
     */
    public static SamplePipeline passthrough(List<Attribute> atts) {
        atts = Collections.unmodifiableList(atts);
        return new SamplePipeline(Collections.emptyList(), atts);
    }

    /**
     * Returns a new {@code PipelineBuilder} instance for creating new
     * {@code SamplePipeline} objects.
     *
     * @return New {@code PipelineBuilder} object
     */
    public static PipelineBuilder newBuilder(List<Attribute> atts) {
        return new PipelineBuilder(atts);
    }

	/**
	 * Returns a collection of all {@link Attribute}s that this
	 * {@code SamplePipeline} adds to the processed {@link Sample}
	 *
	 * @return {@link Attribute}s added to the processed {@link Sample}
	 */
	public Collection<Attribute> attributes() {
        return atts;
    }

	/**
	 * Runs a sample through the {@code SamplePipeline}.
	 *
	 * @param sample
	 *            {@link Sample} to be processed
	 * @return New {@link Sample} produced by the pipeline
	 */
	public Sample run(Object[] sample) {
        int size = atts.size() > sample.length ? atts.size() : sample.length;
        Object[] r = Arrays.copyOf(sample, size);
        for (SampleModifier m : mods) {
            m.process(r);
        }

        return new Sample(atts, r);
    }

	/**
	 * A builder class for creating new {@code SamplePipeline} objects.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class PipelineBuilder {

        private final List<Attribute> atts = new ArrayList<>();
		private final List<SampleModifier> mods = new ArrayList<>();

        /**
         * Private constructur, {@code PipelineBuilder} instances are
         * supposed to be built using the static newBuilder method available
         * in the {@link SamplePipeline} class.
         */
		private PipelineBuilder(List<Attribute> original) {
            atts.addAll(original);
        }

        public void addTimestamp() {
            if (atts.contains(Attribute.TIMESTAMP)) {
                throw new RuntimeException(
                        "The sample already contains a timestamp field");
            }
            SampleModifier m = new TimestampAppender(atts.size());
            atts.add(Attribute.TIMESTAMP);
            mods.add(m);
        }

        public void addStatic(Map<Attribute, Object> staticValues) {
            int base = atts.size();
            Object[] values = new Object[staticValues.size()];

            int i = 0;
            for (Entry<Attribute, Object> e : staticValues.entrySet()) {
                Attribute a = e.getKey();
                if (atts.contains(a)) {
                    throw new RuntimeException(
                            "The sample already contains a " + a + " field");
                }
                atts.add(a);
                values[i++] = e.getValue();
            }
            SampleModifier m = new StaticAppender(base, values);
            mods.add(m);
        }

        public void reorder(List<Attribute> out) {
            mods.add(new Reorder(atts, out));
        }

		/**
		 * Creates a new immutable {@code SamplePipeline} containing all
		 * {@link SampleModifier}s added to the builder
		 *
         * @param attOrder {@link Attribute}s in the order required by the fpc user
		 */
		public SamplePipeline create() {
			return new SamplePipeline(mods, atts);
		}

	}

}