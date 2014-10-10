package org.dei.perla.channel.simulator;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for a generic value-generating component.
 * <p/>
 * Implementations of this class are used by the <code>SimulatorChannel</code>
 * to dynamically create random generated responses.
 *
 * @author Guido Rota (2014)
 */
public class Generator {

    private final String id;
    private final FieldGenerator[] valueGeneratorArray;

    public Generator(String id, FieldGenerator[] valueGeneratorArray) {
        this.id = id;
        this.valueGeneratorArray = valueGeneratorArray;
    }

    public String getId() {
        return id;
    }

    public SimulatorPayload generateResponse() {
        Map<String, Object> resultMap = new HashMap<>();
        for (FieldGenerator valueGen : valueGeneratorArray) {
            resultMap.put(valueGen.getName(), valueGen.generateValue());
        }
        return new SimulatorPayload(resultMap);
    }

}
