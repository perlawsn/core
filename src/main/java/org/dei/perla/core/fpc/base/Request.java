package org.dei.perla.core.fpc.base;

import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Sample;
import org.dei.perla.core.fpc.SamplePipeline;
import org.dei.perla.core.fpc.SamplePipeline.PipelineBuilder;

import java.time.Instant;
import java.util.*;

/**
 * {@code Request} objects are a utility objects that implement the
 * functionalities required to determine, given a list of attributes, which
 * of these are associated to values that can be generated statically by an
 * {@link FPC}.
 *
 * @author Guido Rota 02/07/15.
 */
public final class Request {

    private final boolean isSampled;

    // Original user request
    private final List<Attribute> request;

    // List of attributes that can be generated without sampling the device
    private final List<Attribute> generated;
    private final int generatedTsIdx;
    private final Object[] template;
    private final Map<Attribute, Object> values = new HashMap<>();

    // List of attributes that must be sampled
    private final List<Attribute> sampled;

    /**
     * Creates a new {@code Request} object
     *
     * @param request {@link Attribute}s requested by the user
     * @param staticFpc {@link Attribute}s that can be served statically by
     *                                   an {@link FPC}
     */
    public Request(List<Attribute> request,
            Map<Attribute, Object> staticFpc) {
        this.request = Collections.unmodifiableList(request);
        List<Attribute> ge = new ArrayList<>();
        List<Attribute> sa = new ArrayList<>();

        // Scan request attributes to determine if they can be generated from
        // static FPC values or if they must be sampled from the device
        int tsIdx = -1;
        int i = 0;
        for (Attribute a : request) {
            if (a == Attribute.TIMESTAMP) {
                tsIdx = i;
            } else if (staticFpc.containsKey(a)) {
                ge.add(a);
                values.put(a, staticFpc.get(a));
            } else {
                sa.add(a);
            }
            i++;
        }

        isSampled = sa.size() != 0;

        if (isSampled) {
            generated = Collections.unmodifiableList(ge);
            sampled = Collections.unmodifiableList(sa);
            template = null;
            generatedTsIdx = -1;

        } else {
            sampled = Collections.emptyList();
            if (tsIdx == -1) {
                generatedTsIdx = ge.size();
                ge.add(Attribute.TIMESTAMP);
                generated = Collections.unmodifiableList(ge);
            } else {
                generatedTsIdx = tsIdx;
                generated = Collections.unmodifiableList(request);
            }
            template = precomputeGeneratedSample();
        }
    }

    private Object[] precomputeGeneratedSample() {
        Object[] o = new Object[generated.size()];
        for (int i = 0; i < generated.size(); i++) {
            Attribute a = generated.get(i);
            // Skip timestamp, it's always generated dynamically
            if (a == Attribute.TIMESTAMP) {
                continue;
            }
            o[i] = values.get(a);
        }
        return o;
    }

    public List<Attribute> getRequest() {
        return request;
    }

    /**
     * Indicates if the request contains some {@link Attribute}s that must be
     * sampled from the remote sensing device. It is important to note that
     * the TIMESTAMP attribute is not considered 'sampled' if all other
     * attributes are static.
     *
     * @return true if some {@link Attribute}s must be sampled, false otherwise
     */
    public boolean isSampled() {
        return isSampled;
    }

    /**
     * Returns a list of the user requested {@link Attribute}s that can be
     * served without sampling the sensing device.
     *
     * <p> When the request can be generated statically, i.e. isSampled() ==
     * false, the return value of this method will always contain the
     * TIMESTAMP attribute, even when this is not explicitly requested by the
     * user.
     *
     * @return list of {@link Attribute}s that can be served without sampling
     * the sensing device
     */
    public List<Attribute> getGenerated() {
        return generated;
    }

    /**
     * Returns a list of the user requested {@link Attribute}s whose value
     * must be sampled from the device connected to the {@link FPC}
     *
     * @return list of {@link Attribute}s that must be sampled from the
     * sensing device
     */
    public List<Attribute> getSampled() {
        return sampled;
    }

    /**
     * Creates a new {@link Sample} using the static {@link Fpc} {@link
     * Attributes}. This method is employed to create a new sample when the
     * user request contains only static {@link Attribute}s.
     *
     * @return new {@link Sample} instance
     * @throws RuntimeException if the {@code Request} is not completely static
     */
    public Sample generateSample() throws RuntimeException {
        if (isSampled) {
            throw new RuntimeException("Cannot generate sample, " +
                    "some attributes must be sampled");
        }

        Object[] o = Arrays.copyOf(template, template.length);
        o[generatedTsIdx] = Instant.now();
        return new Sample(generated, o);
    }

    /**
     * Creates a {@link SamplePipeline} that can be used to process the data
     * coming from the underlying {@link Operaiton}. This method will take care
     * to decorate the raw samples coming from the {@link Operaiton} with
     * a timestamp (if needed) and static attributes. Moreover, it will
     * instruct the pipeline to reorder the attribute order to
     *
     * @param opAtts {@link Attribute}s generated by the {@link Operation}
     * @return new {@link SamplePipeline} tailored around the {@link
     * Operation} that generates the raw data and the user's request
     * @throws IllegalStateException when the user's request only contains
     * static {@link Attribute}s
     */
    public SamplePipeline createPipeline(List<Attribute> opAtts)
            throws IllegalStateException {
        if (!isSampled()) {
            throw new IllegalStateException(
                    "Cannot create pipeline, request can be generated without" +
                            " sampling the remote sensing device");
        }

        PipelineBuilder build = SamplePipeline.newBuilder(opAtts);

        // Add a timestamp attribute when the operation doesn't provide a
        // native one
        boolean ts = false;
        for (Attribute a : opAtts) {
            if (a == Attribute.TIMESTAMP) {
                ts = true;
            }
        }
        if (!ts) {
            build.addTimestamp();
        }

        // Add static attributes (if any)
        if (generated.size() != 0) {
            build.addStatic(values);
        }

        // Reorder attributes according to user's requests
        build.reorder(request);

        return build.create();
    }

}
