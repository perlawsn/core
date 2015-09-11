package org.dei.perla.core.fpc;

import java.time.Instant;
import java.util.List;

/**
 * A class implementing a single processing operation to be performed on a
 * {@code Sample}. {@code SampleModifier} are usually employed to add new fields
 * to an existing {@link Sample}.
 *
 * @author Guido Rota (2014)
 *
 */
public interface SampleModifier {

    /**
     * Function for processing a {@code Record}.
     *
     * @param record
     *            record to modify
     */
    public void process(Object[] sample);

    /**
     * {@code SampleModifier} for adding a Timestamp field
     *
     * @author Guido Rota (2014)
     *
     */
    public static final class TimestampAppender implements SampleModifier {

        private final int idx;

        /**
         * @param idx position where the timestamp attribute has to be added
         */
        protected TimestampAppender(int idx) {
            this.idx = idx;
        }

        @Override
        public void process(Object[] sample) {
            sample[idx] = Instant.now();
        }

    }

    /**
     * {@code SampleModifier} for adding fields with static values
     *
     * @author Guido Rota (2014)
     *
     */
    public static final class StaticAppender implements SampleModifier {

        private final Object[] values;
        private final int base;

        /**
         * Creates a new {@link SampleModifier} that adds new static values
         * to the output samples.
         *
         * @param am attibute-value map identifying the values to be added in
         *                 the output sample
         * @param base index at which the attributes must be added
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

    public static final class Reorder implements SampleModifier {

        private final int[] order;

        /**
         * Creates a new {@link SampleModifier} that rearranges the order of
         * the output {@link Attribute}s. All input {@link Attribute}s not
         * found in the {@code out} list are not discarded, but appended at
         * the end of the list instead.
         *
         * <p>
         * NOTE: This constructor changes the order of the {@code in} list.
         *
         * @param in Original {@link Attribute} order
         * @param out Desired output {@link Attribute} order
         */
        protected Reorder(List<Attribute> in, List<Attribute> out) {
            order = new int[out.size()];

            Attribute a;
            Attribute tmp;
            int last = in.size();
            int i;
            for (i = 0; i < out.size(); i++) {
                a = out.get(i);
                int idx = indexOf(in, a);
                if (idx == -1) {
                    idx = last++;
                    in.add(a);
                }
                order[i] = idx;
                tmp = in.get(i);
                in.set(i, in.get(idx));
                in.set(idx, tmp);
            }
        }

        // Custom indexOf function allows the Reorder SampleModifier with
        // TypeClasses
        private static int indexOf(List<Attribute> list, Attribute a) {
            for (int i = 0; i < list.size(); i++) {
                if (a.match(list.get(i))) {
                    return i;
                }
            }
            return -1;
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
