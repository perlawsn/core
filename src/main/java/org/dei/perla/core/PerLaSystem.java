package org.dei.perla.core;

import org.apache.log4j.Logger;
import org.dei.perla.core.channel.*;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptorException;
import org.dei.perla.core.descriptor.DeviceDescriptorParser;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.base.BaseFpcFactory;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.registry.Registry;
import org.dei.perla.core.registry.TreeRegistry;

import java.io.InputStream;
import java.util.*;

/**
 * A simple helper class employed to automatically setup the PerLa Middleware.
 * See the perla-example and perla-web for examples of its use.
 *
 * @author Guido Rota 18/05/15.
 */
public final class PerLaSystem {

    private static final Logger log = Logger.getLogger(PerLaSystem.class);

    private final DeviceDescriptorParser parser;
    private final FpcFactory factory;
    private final Registry registry;

    private final FactoryHandler fctHand = new FactoryHandler();

    /**
     * Creates a new {@code PerLaSystem} object configured with the required
     * {@link Plugin}s.
     *
     * @param plugins plugin objects to use in the PerLa installation
     *                (MapperFactory and ChannelPlugin)
     */
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

    /**
     * Off-band Device Descriptor injection. This method allows the creation
     * of {@link Fpc} proxies for devices whose {@link Channel} cannot relay
     * Device Descriptor information.
     *
     * @param is Device Descriptor {@link InputStream}
     * @throws DeviceDescriptorException if the {@link Fpc} creation process
     * fails due to an error in the Device Descriptor
     */
    public void injectDescriptor(InputStream is)
            throws DeviceDescriptorException {
        DeviceDescriptor d = parser.parse(is);
        Fpc fpc = factory.createFpc(d, 1);
        registry.add(fpc);
    }

    /**
     * Returns the FPC {@link Registry} in use inside this object.
     *
     * @return FPC {@link Registry} instance
     */
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
            result.map(Payload::asInputStream)
                    .map(this::createFpc)
                    .ifPresent(registry::add);
        }

        private Fpc createFpc(InputStream is) {
            try {
                DeviceDescriptor d = parser.parse(is);
                return factory.createFpc(d, 1);
            } catch (Exception e) {
                log.error("Error parsing device descriptor", e);
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
