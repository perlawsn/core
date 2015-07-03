package org.dei.perla.core;

import org.apache.log4j.Logger;
import org.dei.perla.core.channel.*;
import org.dei.perla.core.descriptor.DeviceDescriptor;
import org.dei.perla.core.descriptor.DeviceDescriptorParser;
import org.dei.perla.core.descriptor.JaxbDeviceDescriptorParser;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcCreationException;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.base.BaseFpcFactory;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.registry.Registry;
import org.dei.perla.core.registry.TreeRegistry;

import java.io.InputStream;
import java.util.*;

/**
 * A simple helper class employed to automatically setup the PerLa Middleware.
 * See the perla-example and perla-web projects for examples of its use.
 *
 * @author Guido Rota 18/05/15.
 */
public final class PerLaSystem {

    private static final Logger log = Logger.getLogger(PerLaSystem.class);

    private final DeviceDescriptorParser parser;
    private final FpcFactory factory;
    private final TreeRegistry registry;
    
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
                String msg = "Unknown plugin type " + p.getClass().getName();
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // Create FPC Factory
        parser = new JaxbDeviceDescriptorParser(pkgs);
        factory = new BaseFpcFactory(maps, chans, reqs);
    }

    /**
     * Reads an XML Device Descriptor from the {@link InputStream} passed as
     * parameter and uses it to create a new {@link Fpc}.
     *
     * @param is Device Descriptor {@link InputStream}
     * @return the newly created {@link Fpc} object
     * @throws FpcCreationException if the {@link Fpc} creation process
     * fails due to an error in the Device Descriptor
     */
    public Fpc injectDescriptor(InputStream is) throws FpcCreationException {
        int id = -1;
        boolean idGenerated = false;

        try {

            DeviceDescriptor d = parser.parse(is);

            if (d.getId() != null) {
                id = d.getId();
            } else {
                id = registry.generateID();
                idGenerated = true;
            }

            Fpc fpc = factory.createFpc(d, id);
            registry.add(fpc);
            return fpc;

        } catch(Exception e) {
            if (idGenerated) {
                registry.releaseID(id);
            }
            String msg = "Error creating Fpc '" + id + "'";
            log.error(msg, e);
            throw new FpcCreationException(msg, e);
        }
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
                    .ifPresent(this::addFpc);
        }

        private void addFpc(InputStream is) {
            try {
                injectDescriptor(is);
            } catch (FpcCreationException e) {
                log.error(e);
            }
        }

        @Override
        public void error(IORequest request, Throwable cause) {
            log.error("Unexpected error while waiting for Device Descriptor",
                    cause);
        }

    }

}
