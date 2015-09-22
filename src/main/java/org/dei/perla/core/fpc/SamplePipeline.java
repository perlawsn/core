package org.dei.perla.core.fpc;

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
        in = new ArrayList<>(in);
        out = new ArrayList<>(out);
        List<Modifier> mods = new ArrayList<>();

        addStatic(in, values, mods);
        addTimestamp(in, out, mods);
        addReorder(in, out, mods);

        modifiers = Collections.unmodifiableList(mods);
        attributes = Collections.unmodifiableList(out);
    }

    private void addStatic(List<Attribute> in, Map<Attribute, Object> values,
            List<Modifier> mods) {
        if (values.size() == 0) {
            return;
        }

        Object[] v = new Object[values.size()];
        int base = in.size();
        int i = 0;
        for (Attribute a : values.keySet()) {
            if (in.contains(a)) {
                throw new IllegalArgumentException("Cannot override sampled " +
                        "attribute '" + a + "' with static value");
            }
            in.add(a);
            v[i] = values.get(a);
        }
        mods.add(new StaticAppender(base, v));
    }

    private void addTimestamp(List<Attribute> in, List<Attribute> out,
            List<Modifier> mods) {
        if (!in.contains(Attribute.TIMESTAMP)) {
            mods.add(new TimestampAdder(in.size()));
            in.add(Attribute.TIMESTAMP);
        }

        boolean hasTs = false;
        // Custom by-id attribute check
        for (Attribute a : out) {
            if (a.getId().equals(Attribute.TIMESTAMP.getId())) {
                hasTs = true;
                break;
            }
        }
        if (!hasTs) {
            out.add(Attribute.TIMESTAMP);
        }
    }

    private void addReorder(List<Attribute> in, List<Attribute> out,
            List<Modifier> mods) {
        int[] order = new int[out.size()];

        for (int i = 0; i < out.size(); i++) {
            Attribute a = out.get(i);
            int idx = indexOf(in, a);
            if (idx != -1) {
                order[i] = idx;
                Attribute tmp = in.get(i);
                in.set(i, in.get(idx));
                in.set(idx, tmp);
            } else {
                order[i] = in.size();
                in.add(in.get(i));
                in.set(i, a);
            }
        }

        mods.add(new Reorder(order));
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
	public Sample run(Object[] sample) {
        Object[] s = Arrays.copyOf(sample, attributes.size());
        for (Modifier m : modifiers) {
            m.process(s);
        }
        return new Sample(attributes, s);
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
         * @param sample sample to modify
         */
        public void process(Object[] sample);

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
        public void process(Object[] sample) {
            sample[idx] = Instant.now();
        }

    }


    /**
     * {@link Modifier} for adding static attribute values
     *
     * @author Guido Rota (2014)
     */
    public static final class StaticAppender implements Modifier {

        private final Object[] values;
        private final int base;

        /**
         * Creates a new {@link Modifier} that adds new static values
         * to the output samples.
         *
         * @param base index at which the attributes must be added
         * @param am attibute-value map identifying the values to be added in
         *                 the output sample
         */
        protected StaticAppender(int base, Object[] values) {
            this.base = base;
            this.values = values;
        }

        @Override
        public void process(Object[] sample) {
            for (int i = 0; i < values.length; i++) {
                sample[base + i] = values[i];
            }
        }

    }


    /**
     * {@link Modifier} for reordering the sample attributes
     *
     * @author Guido Rota (2014)
     */
    public static final class Reorder implements Modifier {

        private final int[] order;

        /**
         * Creates a new {@link SampleModifier} that rearranges the order of
         * the output {@link Attribute}s.
         *
         * @param order order of arrangemet of the attributes
         */
        protected Reorder(int[] order) {
            this.order = order;
        }

        @Override
        public void process(Object[] sample) {
            Object tmp;
            int idx;

            for (int i = 0; i < order.length; i++) {
                idx = order[i];
                if (idx == -1) {
                    continue;
                }
                tmp = sample[i];
                sample[i] = sample[idx];
                sample[idx] = tmp;
            }
        }

    }


}
