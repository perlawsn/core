package org.dei.perla.core.channel.simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dei.perla.core.channel.AbstractChannel;
import org.dei.perla.core.channel.ChannelException;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.Payload;

/**
 * <p>
 * A <code>Channel</code> for simulating a network node.
 * </p>
 * <p/>
 * <p>
 * The <code>SimulatorChannel</code> behaviour can be specified directly in the
 * XML Device Descriptor using the appropriate {@code<channel>} element with
 * namespace http://perla.dei.org/channel/simulator.
 * </p>
 * <p/>
 * <p>
 * A <code>SimulatorChannel</code> is able to reply a user-defined series of
 * messages containing fixed or random data. The following snippet of code
 * exemplifies a typical setup of the <code>SimulatorChannel</code>. Three
 * different types of messages are defined:
 * <ul>
 * <li>{@code temp-only} message:
 * <ul>
 * <li>A {@code type} field with static value "temp-only"</li>
 * <li>A {@code temperature} field with random value between 12 and 32</li>
 * </ul>
 * </li>
 * <li>{@code press-only} message:
 * <ul>
 * <li>A {@code type} field with static value "press-only"</li>
 * <li>A {@code pressure} field with random value between 450 and 600</li>
 * </ul>
 * </li>
 * </li>
 * <li>{@code all} message:
 * <ul>
 * <li>A {@code type} field with static value "all"</li>
 * <li>A {@code temperature} field with random value between 12 and 32</li>
 * <li>A {@code pressure} field with random value between 450 and 600</li>
 * </ul>
 * </li>
 * <ul>
 * </p>
 * <p/>
 * <p>
 * <p/>
 * <pre>
 * {@code
 * <sim:channel id="test">
 *   <sim:generator id="temp-only">
 *     <sim:field name="type" qualifier="static" value="temp-only"/>
 *     <sim:field name="temperature" qualifier="dynamic"
 *         type="float" min="12" max="32"/>
 *     </sim:generator>
 *   <sim:generator id="press-only">
 *     <sim:field name="type" qualifier="static" value="press-only"/>
 *     <sim:field name="pressure" qualifier="dynamic"
 *         type="float" min="450" max="600"/>
 *   </sim:generator>
 *   <sim:generator id="all">
 *     <sim:field name="type" qualifier="static" value="all"/>
 *     <sim:field name="temperature" qualifier="dynamic"
 *         type="float" min="12" max="32"/>
 *     <sim:field name="pressure" qualifier="dynamic"
 *         type="float" min="450" max="600"/>
 *   </sim:generator>
 * </sim:channel>
 * }
 * </pre>
 * <p/>
 * </p>
 * <p/>
 * <p>
 * The {@code<field>} qualifier attribute allows users to specify how that field
 * is produced by the <code>SimulatorChannel</code>.
 * <ul>
 * <li><code>static</code>: the field will always assume the value specified in
 * the <code>value</code> attribute of the {@code<response-field>} element</li>
 * <li><code>dynamic</code>: the field will be randomly generated using the
 * <code>min</code> and <code>max</code> attributes of the {@code<field>}
 * element.</li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * Dynamic field generation depends on the field type:
 * <ul>
 * <li><code>int</code>: random value generated between min and max</li>
 * <li><code>float</code>: random value generated between min and max</li>
 * <li><code>string</code>: random string of random length between min and max</li>
 * <li><code>boolean</code>: true or false (min and max ignored)</li>
 * <li><code>timestamp</code>: response creation date and time (min and max
 * ignored)</li>
 * <li><code>id</code>: NOT ALLOWED</li>
 * <li></li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * <code>SimulatorChannel</code> does not provide any means of creating dynamic
 * attributes with a static value. Such behaviour can be achieved by configuring
 * an FPC attribute with static <code>access</code>.
 * </p>
 * <p/>
 * <p>
 * <code>SimulatorChannel</code> users can request a specific response by
 * sending a <code>SimulatorIORequest</code> with the desired response
 * identifier set in the <code>generatorId</code> field.
 * </p>
 * <p/>
 * <p>
 * For example, sending a <code>SimulatorIORequest</code> with the
 * <code>responseId</code> field set to <code>press-only</code> will result in
 * the creation of a <code>ChannelResponse</code> conforming to the
 * <code>press-only</code> section of the XML code snippet above.
 * </p>
 * <p/>
 * <p>
 * The <code>period</code> parameter inside the <code>SimulatorIORequest</code>
 * is used for starting, stopping and re-scheduling value generation at periodic
 * intervals. The <code>SimulatorPayload</code> associated with this parameter
 * must be marshalled from a <code>SimulatorMessage</code> with a field named
 * 'period', which represents the interval in ms of the periodic value
 * generation task.
 * </p>
 * <p/>
 * <p>
 * Scheduling (or re-scheduling) happens when the period is > 0. A running
 * periodic generation task is stopped when the <code>SimulatorChannel</code>
 * receive a request with period equal to 0.
 * </p>
 *
 * @author Guido Rota (2014)
 */
public class SimulatorChannel extends AbstractChannel {

    private final Generator[] generatorArray;
    private ScheduledExecutorService exec = new ScheduledThreadPoolExecutor(1);
    private Map<String, ScheduledFuture<?>> runningMap = new HashMap<>();

    public SimulatorChannel(String id, Generator[] generatorArray) {
        super(id);
        this.generatorArray = generatorArray;
    }

    @Override
    public Payload handleRequest(IORequest request) throws ChannelException,
            InterruptedException {
        SimulatorIORequest simReq;

        simReq = (SimulatorIORequest) request;

        Generator generator = findGenerator(simReq.getGeneratorId());
        if (generator == null) {
            return null;
        }

        if (simReq.getPeriod() != null) {
            SimulatorPayload payload = (SimulatorPayload) simReq.getPeriod();
            if (!payload.getValueMap().containsKey("period")) {
                throw new ChannelException(
                        "Missing 'period' attribute in period message");
            }
            int period = (Integer) payload.getValueMap().get("period");
            scheduleResponse(simReq, period, generator);
            return null;
        }

        return generator.generateResponse();
    }

    private Generator findGenerator(String id) {
        for (Generator generator : generatorArray) {
            if (generator.getId().equals(id)) {
                return generator;
            }
        }
        return null;
    }

    private void scheduleResponse(SimulatorIORequest request, int period,
            final Generator generator) throws ChannelException {

        if (period < 0) {
            throw new ChannelException(
                    "Invalid negative period in Simulator request '"
                            + request.getId() + "'.");
        }

        ScheduledFuture<?> future = runningMap.remove(request.getGeneratorId());
        if (future != null) {
            future.cancel(true);
        }

        // Setting a period = 0 stops the simulator from periodically generating
        // values
        if (period == 0) {
            return;
        }

        future = exec.scheduleAtFixedRate(() -> {
            Payload response = generator.generateResponse();
            notifyAsyncData(response);
        }, 0, period, TimeUnit.MILLISECONDS);

        runningMap.put(request.getGeneratorId(), future);
    }

    @Override
    public void close() {
        super.close();
        for (ScheduledFuture<?> future : runningMap.values()) {
            future.cancel(true);
        }
        runningMap = null;
        exec.shutdownNow();
        exec = null;
    }

}
