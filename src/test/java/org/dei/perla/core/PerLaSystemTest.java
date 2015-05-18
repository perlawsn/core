package org.dei.perla.core;

import org.dei.perla.core.channel.http.HttpChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.message.json.JsonMapperFactory;
import org.dei.perla.core.message.urlencoded.UrlEncodedMapperFactory;
import org.dei.perla.core.registry.Registry;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Guido Rota 18/05/15.
 */
public class PerLaSystemTest {

    private static final String descPath =
            "src/test/java/org/dei/perla/core/fpc/base/fpc_descriptor.xml";

    private static final List<Plugin> plugins;
    static {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new JsonMapperFactory());
        ps.add(new UrlEncodedMapperFactory());
        ps.add(new SimulatorMapperFactory());
        ps.add(new HttpChannelPlugin());
        ps.add(new SimulatorChannelPlugin());
        plugins = Collections.unmodifiableList(ps);
    }

    @Test
    public void testCreation() {
        PerLaSystem ps = new PerLaSystem(plugins);
        assertThat(ps.getRegistry(), notNullValue());
    }

    @Test
    public void testFpcCreation() throws Exception {
        PerLaSystem sys = new PerLaSystem(plugins);
        Registry reg = sys.getRegistry();
        assertThat(reg.getAll().size(), equalTo(0));

        sys.injectDescriptor(new FileInputStream(descPath));
        assertThat(reg.getAll().size(), equalTo(1));
    }

}
