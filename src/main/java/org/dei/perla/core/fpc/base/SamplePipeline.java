package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.Sample;

import java.time.Instant;
import java.util.*;

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
public final class SamplePipeline {

    public final List<Modifier> modifiers;
    public final List<Attribute> attributes;

    /**
     * Creates a new passthrough {@code SamplePipeline}
     *
     * @param atts pipeline parameters
     */
    public SamplePipeline(List<Attribute> atts) {
        modifiers = Collections.emptyList();
        atts = new ArrayList<>(atts);
        attributes = Collections.unmodifiableList(atts);
    }

    /**
     * Creates a new {@code SamplePipeline}
     *
     * @param in attributes given as an input to the pipeline
     * @param out attributes returned as an output by the pipeline
     */
    public SamplePipeline(List<Attribute> in, List<Attribute> out) {
        this(in, Collections.emptyMap(), out);
    }

    /**
     * Creates a new {@code SamplePipeline}
     *
     * @param in attributes given as an input to the pipeline
     * @param values static attribute values
     * @param out attributes returned as an output by the pipeline
     */
    public SamplePipeline(List<Attribute> in,
            Map<Attribute, Object> values, List<Attribute> out) {
        List<Modifier> mods = new ArrayList<>();
        in = new ArrayList<>(in);
        out = new ArrayList<>(out);

        boolean nativeTs = in.contains(Attribute.TIMESTAMP);
        int tsIdx = indexOf(out, Attribute.TIMESTAMP);
        // Custom by-id attribute check
        if (tsIdx == -1) {
            out.add(Attribute.TIMESTAMP);
        }

        addStatic(in, out, values, mods);
        addCopy(in, out, mods);

        if (!nativeTs) {
            tsIdx = indexOf(out, Attribute.TIMESTAMP);
            mods.add(new TimestampAdder(tsIdx));
        }

        modifiers = Collections.unmodifiableList(mods);
        attributes = Collections.unmodifiableList(out);
    }

    private void addStatic(List<Attribute> in, List<Attribute> out,
            Map<Attribute, Object> values, List<Modifier> mods) {
        if (values.size() == 0) {
            return;
        }

        Object[] v = new Object[out.size()];
        int i = 0;
        for (Attribute a : values.keySet()) {
            if (in.contains(a)) {
                throw new IllegalArgumentException("Cannot override sampled " +
                        "attribute '" + a + "' with static value");
            }
            int idx = indexOf(out, a);
            if (idx == -1) {
                throw new IllegalArgumentException("Attribute '" + a + "' is " +
                        "not an output attribute");
            }
            v[idx] = values.get(a);
        }
        mods.add(new StaticAppender(v));
    }

    private void addCopy(List<Attribute> in, List<Attribute> out,
            List<Modifier> mods) {
        int[] order = new int[in.size()];

        for (int i = 0; i < order.length; i++) {
            order[i] = -1;
        }

        for (int i = 0; i < out.size(); i++) {
            Attribute a = out.get(i);
            int idx = indexOf(in, a);
            if (idx == -1) {
                continue;
            }
            order[idx] = i;
        }

        mods.add(new Copy(order));
    }

    private int indexOf(List<Attribute> list, Attribute a) {
        for (int i = 0; i < list.size(); i++) {
            Attribute b = list.get(i);
            if (b.getId().equals(a.getId())) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Returns the list of modifiers contained in this {@code SamplePipeline}
     *
     * @return list of sample modifiers in the pipeline
     */
    public List<Modifier> getModifiers() {
        return modifiers;
    }

    /**
     * Returns the {@link Attribute}s that are contained in the {@link
     * Sample}s processed using the {@code SamplePipeline}.
     *
     * @return {@link Attribute}s added to the processed {@link Sample}
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

	/**
	 * Runs data sampled by the {@link Fpc} through the {@code
     * SamplePipeline} in order to create an output {@link Sample}.
	 *
	 * @param sample data to be processed
	 * @return New {@link Sample} produced by the pipeline
	 */
	public Sample run(Object[] in) {
        Object[] out = Arrays.copyOf(in, attributes.size());
        for (Modifier m : modifiers) {
            m.process(in, out);
        }
        return new Sample(attributes, out);
    }


    /**
     * A class implementing a single processing operation to be performed on a
     * {@code Sample}.
     *
     * @author Guido Rota (2014)
     */
    public interface Modifier {

        /**
         * Processes the contents of the sample array passed as parameter
         *
         * @param in original sample generated by the {@link Fpc}. Its
         *           contents must not be modified
         * @param out output sample, will be modified during the execution fo
         *            the {@code Modifier}
         */
        public void process(Object[] in, Object[] out);

    }


    /**
     * {@code SampleModifier} for adding a Timestamp field
     *
     * @author Guido Rota (2014)
     *
     */
    public static final class TimestampAdder implements Modifier {

        private final int idx;

        /**
         * @param idx position where the timestamp attribute has to be added
         */
        protected TimestampAdder(int idx) {
            this.idx = idx;
        }

        @Override
        public void process(Object[] in, Object[] out) {
            out[idx] = Instant.now();
        }

    }


    /**
     * {@link Modifier} for adding static attribute values
     *
     * @author Guido Rota (2014)
     */
    public static final class StaticAppender implements Modifier {

        private final Object[] values;

        /**
         * Creates a new {@link Modifier} that adds new static values
         * to the output samples.
         *
         * @param values array of values to be added. Must be the same size
         *               of the output sample. Only non-null values will be
         *               copied in the output sample array.
         */
        protected StaticAppender(Object[] values) {
            this.values = values;
        }

        @Override
        public void process(Object[] in, Object[] out) {
            for (int i = 0; i < out.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                out[i] = values[i];
            }
        }

    }


    /**
     * {@link Modifier} for copying the input values in their appropriate
     * position inside the output sample.
     *
     * @author Guido Rota (2014)
     */
    public static final class Copy implements Modifier {

        private final int[] order;

        /**
         * Creates a new {@link SampleModifier} that
         *
         * @param order order of arrangemet of the attributes
         */
        protected Copy(int[] order) {
            this.order = order;
        }

        @Override
        public void process(Object[] in, Object[] out) {
            for (int i = 0; i < order.length; i++) {
                int idx = order[i];
                if (idx == -1) {
                    continue;
                }
                out[idx] = in[i];
            }
        }

    }


}
