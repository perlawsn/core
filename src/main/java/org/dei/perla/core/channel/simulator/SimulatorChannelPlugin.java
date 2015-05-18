package org.dei.perla.core.channel.simulator;

import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.ChannelPlugin;
import org.dei.perla.core.channel.IOHandler;
import org.dei.perla.core.channel.IORequestBuilderFactory;

/**
 * @author Guido Rota 18/05/15.
 */
public class SimulatorChannelPlugin implements ChannelPlugin {

    private final SimulatorChannelFactory chFct;
    private final SimulatorIORequestBuilderFactory ioreqFct;

    public SimulatorChannelPlugin() {
        chFct = new SimulatorChannelFactory();
        ioreqFct = new SimulatorIORequestBuilderFactory();
    }

    @Override
    public void registerFactoryHandler(IOHandler handler) { }

    @Override
    public ChannelFactory getChannelFactory() {
        return chFct;
    }

    @Override
    public IORequestBuilderFactory getIORequestBuilderFactory() {
        return ioreqFct;
    }

}
