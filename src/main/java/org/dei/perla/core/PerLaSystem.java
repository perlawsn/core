package org.dei.perla.core;

import org.apache.log4j.Logger;
import org.dei.perla.core.channel.*;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptorParser;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.base.BaseFpcFactory;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.registry.Registry;
import org.dei.perla.core.registry.TreeRegistry;

import java.util.*;

/**
 * @author Guido Rota 18/05/15.
 */
public final class PerLaSystem {

    private static final Logger log = Logger.getLogger(PerLaSystem.class);

    private final DeviceDescriptorParser parser;
    private final FpcFactory factory;
    private final Registry registry;

    private final FactoryHandler fctHand = new FactoryHandler();

    public PerLaSystem(List<Plugin> plugins) {
        registry = new TreeRegistry();

        // Initialize default Device Descriptor packages
        Set<String> pkgs = new HashSet<>();
        pkgs.add("org.dei.perla.core.descriptor");
        pkgs.add("org.dei.perla.core.descriptor.instructions");

        // Parse user-defined Plugins
        List<MapperFactory> maps = new ArrayList<>();
        List<ChannelFactory> chans = new ArrayList<>();
        List<IORequestBuilderFactory> reqs = new ArrayList<>();
        for (Object p : plugins) {
            pkgs.add(p.getClass().getPackage().getName());
            if (p instanceof MapperFactory) {
                // Manage mapper plugin
                maps.add((MapperFactory) p);

            } else if (p instanceof ChannelPlugin) {
                // Manage channel plugin
                ChannelPlugin cp = (ChannelPlugin) p;
                cp.registerFactoryHandler(fctHand);
                chans.add(cp.getChannelFactory());
                reqs.add(cp.getIORequestBuilderFactory());

            } else {
                throw new IllegalArgumentException("Unknown plugin type " +
                        p.getClass().getName());
            }
        }

        // Create FPC Factory
        parser = new JaxbDeviceDescriptorParser(pkgs);
        factory = new BaseFpcFactory(maps, chans, reqs);
    }

    public Registry getRegistry() {
        return registry;
    }

    /**
     * IOHandler for processing FPC Device Descriptors
     *
     * @author Guido Rota 18/05/15
     */
    private final class FactoryHandler implements IOHandler {

        @Override
        public void complete(IORequest request, Optional<Payload> result) {
            result.map(this::createFpc).ifPresent(registry::add);
        }

        private Fpc createFpc(Payload p) {
            try {
                DeviceDescriptor d = parser.parse(p.asInputStream());
                return factory.createFpc(d, 1);
            } catch (Exception e) {
                log.error("Error parsing device descriptor:\n" +
                        p.asString(), e);
            }
            return null;
        }

        @Override
        public void error(IORequest request, Throwable cause) {
            log.error("Unexpected error while waiting for Device Descriptor",
                    cause);
        }

    }

}
