package org.dei.perla.core.fpc.base;

import javassist.ClassClassPath;
import javassist.ClassPool;
import org.apache.log4j.Logger;
import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.ChannelFactory;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.IORequestBuilderFactory;
import org.dei.perla.core.descriptor.*;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributeAccessType;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.core.descriptor.instructions.InstructionDescriptor;
import org.dei.perla.core.engine.Compiler;
import org.dei.perla.core.engine.Script;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcFactory;
import org.dei.perla.core.fpc.IDGenerator;
import org.dei.perla.core.fpc.base.AsyncOperation.AsyncMessageHandler;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.message.MapperFactory;
import org.dei.perla.core.sample.Attribute;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.Conditions;
import org.dei.perla.core.utils.Errors;

import java.util.*;

public class BaseFpcFactory implements FpcFactory {

    private final Logger logger = Logger.getLogger(BaseFpcFactory.class);
    private final Map<Class<? extends MessageDescriptor>, MapperFactory>
            mapFcts = new HashMap<>();
    private final Map<Class<? extends ChannelDescriptor>, ChannelFactory>
            chanFcts = new HashMap<>();
    private final Map<Class<? extends IORequestDescriptor>, IORequestBuilderFactory>
            reqFcts = new HashMap<>();

    public BaseFpcFactory(List<MapperFactory> mapFcts, List<ChannelFactory> chanFcts,
            List<IORequestBuilderFactory> reqFcts) {
        Conditions.checkNotNull(mapFcts, "mapperFactoryList");
        Conditions.checkNotNull(chanFcts, "channelFactoryList");
        Conditions.checkNotNull(reqFcts, "requestBuilderFactoryList");

        for (MapperFactory f : mapFcts) {
            this.mapFcts.put(f.acceptedMessageDescriptorClass(), f);
        }
        for (ChannelFactory f : chanFcts) {
            this.chanFcts.put(f.acceptedChannelDescriptorClass(), f);
        }
        for (IORequestBuilderFactory f : reqFcts) {
            this.reqFcts.put(f.acceptedIORequestClass(), f);
        }
    }

    @Override
    public Fpc createFpc(DeviceDescriptor desc, IDGenerator gen)
            throws InvalidDeviceDescriptorException {
        Conditions.checkNotNull(desc, "descriptor");

        Errors err = new Errors("Device descriptor '%s'", desc.getType());
        int id = desc.getId() == null ? gen.generateID() : desc.getId();
        ParsingContext ctx = new ParsingContext(id);

        try {
            parseDescriptor(err, desc, ctx);
        } catch (Exception e) {
            err.addError(e, "An unknown error has occurred while creating an FPC");
        }
        if (!err.isEmpty()) {
            logger.error(err.asString());
            if (desc.getId() == null) {
                gen.releaseID(ctx.id);
            }
            throw new InvalidDeviceDescriptorException(err.asString());
        }

        Scheduler sched = new Scheduler(ctx.getOpList, ctx.setOpList,
                ctx.periodicOpList, ctx.asyncOpList);
        return new BaseFpc(ctx.id, desc.getType(), ctx.atts, ctx.attValues,
                ctx.channelMgr, sched);
    }

    /**
     * Parses the device descriptor passed as parameter.
     *
     * This method update the ParsingContext data structure with all the
     * information and software components needed to create a new FPC.
     *
     * The parsing procedure continues even when errors are found. This allows
     * the parser to catch as many error as possible, and return to the user a
     * clearer pictures of the changes that need to be applied to the Device
     * Descriptor.
     *
     * @param err  Errors object
     * @param desc Device descriptor
     * @param ctx  Data structure for storing intermediate parsing result
     * @return Number of errors found while parsing the Device Descriptor
     */
    private void parseDescriptor(Errors err, DeviceDescriptor desc, ParsingContext ctx) {

        // Check device name
        String deviceName = desc.getType();
        if (Check.nullOrEmpty(deviceName)) {
            err.addError(MISSING_DEVICE_TYPE);
        }

        // Parse device attributes
        List<AttributeDescriptor> atts = desc.getAttributeList();
        if (atts.size() == 0) {
            err.addError(MISSING_ATTRIBUTE_DECLARATIONS);
        }
        parseAttributeList(atts, ctx, err);

        // Parse device messages
        List<MessageDescriptor> msgs = desc.getMessageList();
        if (msgs.size() == 0) {
            err.addError(MISSING_MESSAGE_DECLARATIONS);
        }
        parseMessageList(msgs, ctx, err);

        // Parse device channels
        List<ChannelDescriptor> chans = desc.getChannelList();
        if (chans.size() == 0) {
            err.addError(MISSING_CHANNEL_DECLARATIONS);
        }
        parseChannelList(chans, ctx, err);
        ctx.channelMgr = new ChannelManager(new ArrayList<>(
                ctx.channels.values()));

        // Parse channel requests
        List<IORequestDescriptor> reqs = desc.getRequestList();
        if (reqs.size() == 0) {
            err.addError(MISSING_REQUEST_DECLARATIONS);
        }
        parseRequestList(reqs, ctx, err);

        if (!err.isEmpty()) {
            // Operation parsing can't be performed properly if there are errors
            // in previous descriptor sections
            return;
        }

        // Parse operations
        List<OperationDescriptor> ops = desc.getOperationList();
        if (ops.size() == 0) {
            err.addError(MISSING_OPERATION_DECLARATIONS);
        }
        parseOperationList(ops, ctx, err);
    }

    private void parseAttributeList(List<AttributeDescriptor> atts,
            ParsingContext ctx, Errors err) {
        boolean hasNativeTimestamp = false;
        List<String> ids = new ArrayList<>();

        for (AttributeDescriptor a : atts) {
            String id = a.getId();
            if (Check.nullOrEmpty(id)) {
                err.addError(MISSING_ATTRIBUTE_ID);
            }
            if (ids.contains(id)) {
                err.addError(DUPLICATE_ATTRIBUTE_ID, id);
            }
            if (a.getType() == DataType.TIMESTAMP) {
                hasNativeTimestamp = true;
            }
            ids.add(a.getId());
            parseAttribute(a, ctx, err.inContext("Attribute '%s'", id));
        }

        if (!hasNativeTimestamp) {
            ctx.add(new AttributeDescriptor("timestamp",
                    DataType.TIMESTAMP, AttributePermission.READ_ONLY));
        }

        ctx.add(new AttributeDescriptor("id", DataType.ID, ctx.id.toString()));
    }

    private void parseAttribute(AttributeDescriptor a, ParsingContext ctx,
            Errors err) {
        // Check missing attribute type
        if (a.getType() == null) {
            err.addError(MISSING_ATTRIBUTE_TYPE);
        }

        // Check attribute access
        if (a.getAccess() == AttributeAccessType.STATIC
                && Check.nullOrEmpty(a.getValue())) {
            err.addError(MISSING_STATIC_ATTRIBUTE_VALUE);

        } else if (a.getAccess() == AttributeAccessType.STATIC
                && a.getPermission() != AttributePermission.READ_ONLY) {
            err.addError(INVALID_STATIC_ATTRIBUTE_PERMISSION, a.getAccess());

        } else if (a.getAccess() != AttributeAccessType.STATIC
                && !Check.nullOrEmpty(a.getValue())) {
            err.addError(MISPLACED_ATTRIBUTE_VALUE);
        }

        // Check timestamp attribute
        if (a.getType() == DataType.TIMESTAMP
                && a.getAccess() == AttributeAccessType.STATIC) {
            err.addError(FORBIDDEN_STATIC_ATTRIBUTE);
        }

        // Check id attribute
        if (a.getId().compareToIgnoreCase("id") == 0
                && a.getType() == DataType.ID) {
            err.addError(FORBIDDEN_ID_ATTRIBUTE);
        }

        ctx.add(a);
    }

    private void parseMessageList(List<MessageDescriptor> msgs,
            ParsingContext ctx, Errors err) {
        Set<String> ids = new HashSet<>();

        for (MessageDescriptor m : msgs) {
            String id = m.getId();
            if (Check.nullOrEmpty(id)) {
                err.addError(MISSING_MESSAGE_ID);
            }
            if (ids.contains(id)) {
                err.addError(DUPLICATE_MESSAGE_ID, id);
            }
            ids.add(id);
            parseMessage(m, ctx, err.inContext("Message '%s'", id));
        }
    }

    private void parseMessage(MessageDescriptor m, ParsingContext ctx,
            Errors err) {
        MapperFactory mapFct;

        // Check fields
        List<String> fieldNames = new ArrayList<>();
        for (FieldDescriptor f : m.getFieldList()) {
            if (fieldNames.contains(f.getName())) {
                err.addError(DUPLICATE_FIELD_NAME, f.getName());
            }
            fieldNames.add(f.getName());
            checkField(f, err.inContext("Field '%s'", f.getName()));
        }

        if (!err.isEmpty()) {
            // Return immediately without creating the Mapper
            return;
        }

        // Return immediately without creating the Mapper creation
        mapFct = mapFcts.get(m.getClass());
        if (mapFct == null) {
            err.addError(MISSING_MAPPER_FACTORY, m.getClass());
            return;
        }

        Mapper map = null;
        try {
            map = mapFct.createMapper(m, ctx.mappers, ctx.classPool);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e, MAPPER_CREATION_ERROR);
            return;
        }

        if (map == null) {
            err.addError(MAPPER_CREATION_ERROR);
            return;
        }
        ctx.add(map);
    }

    private void checkField(FieldDescriptor f, Errors err) {
        String name = f.getName();
        String value = f.getValue();

        // Check field name
        if (Check.nullOrEmpty(name)) {
            err.addError(MISSING_FIELD_NAME);
        }

        // Check field type
        if (Check.nullOrEmpty(f.getType())) {
            err.addError(MISSING_FIELD_TYPE);
        }

        // Check static fields
        if (f.isStatic() && Check.nullOrEmpty(value)) {
            err.addError(MISSING_FIELD_VALUE);

        } else if (!f.isStatic() && !Check.nullOrEmpty(value)) {
            err.addError(MISPLACED_FIELD_VALUE);
        }

        // Check Timestamp
        if (!DataType.TIMESTAMP.is(f.getType())
                && !Check.nullOrEmpty(f.getFormat())) {
            err.addError(INVALID_TIMESTMAP_FORMAT);
        } else if (DataType.TIMESTAMP.is(f.getType())
                && !Check.nullOrEmpty(f.getFormat())) {
            err.addError(MISSING_TIMESTAMP_FORMAT);
        }
    }

    private void parseChannelList(List<ChannelDescriptor> chans,
            ParsingContext ctx, Errors err) {
        Set<String> ids = new HashSet<>();

        for (ChannelDescriptor c : chans) {
            String id = c.getId();
            if (Check.nullOrEmpty(id)) {
                err.addError(MISSING_CHANNEL_ID);
            }
            if (ids.contains(c.getId())) {
                err.addError(DUPLICATE_CHANNEL_ID, c.getId());
            }
            ids.add(c.getId());
            parseChannel(c, ctx, err.inContext("Channel '%s'", c.getId()));
        }
    }

    private void parseChannel(ChannelDescriptor c, ParsingContext ctx,
            Errors err) {

        ChannelFactory fct = chanFcts.get(c.getClass());
        if (fct == null) {
            err.addError(MISSING_CHANNEL_FACTORY, c.getClass());
        }

        try {
            Channel ch = fct.createChannel(c);
            ctx.add(c.getId(), ch);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e, CHANNEL_CREATION_ERROR);
        }
    }

    private void parseRequestList(List<IORequestDescriptor> reqs,
            ParsingContext ctx, Errors err) {
        Set<String> ids = new HashSet<>();

        for (IORequestDescriptor r : reqs) {
            if (Check.nullOrEmpty(r.getId())) {
                err.addError(MISSING_REQUEST_ID);
            }
            if (ids.contains(r.getId())) {
                err.addError(DUPLICATE_REQUEST_ID, r.getId());
            }
            ids.add(r.getId());
            parseRequest(r, ctx, err.inContext("Request '%s'", r.getId()));
        }
    }

    private void parseRequest(IORequestDescriptor r, ParsingContext ctx,
            Errors err) {
        IORequestBuilderFactory fct = reqFcts.get(r.getClass());
        if (fct == null) {
            err.addError(MISSING_REQUEST_BUILDER_FACTORY, r.getId());
            return;
        }

        try {
            ctx.add(fct.create(r));
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e, REQUEST_BUILDER_CREATION_ERROR);
        }
    }

    private void parseOperationList(List<OperationDescriptor> ops,
            ParsingContext ctx, Errors err) {
        Set<String> ids = new HashSet<>();

        for (OperationDescriptor o : ops) {
            // Checks common operation fields
            if (Check.nullOrEmpty(o.getId())) {
                err.addError(MISSING_OPERATION_NAME);
            }
            if (o.getId().startsWith("_")) {
                err.addError(INVALID_OPERATION_NAME);
            }
            if (ids.contains(o.getId())) {
                err.addError(DUPLICATE_OPERATION_NAME, o.getId());
            }
            ids.add(o.getId());

            if (o instanceof PeriodicOperationDescriptor) {
                parsePeriodicOperation((PeriodicOperationDescriptor) o, ctx,
                        err.inContext("Sampling operation '%s'", o.getId()));

            } else if (o instanceof AsyncOperationDescriptor) {
                parseAsyncOperation((AsyncOperationDescriptor) o, ctx,
                        err.inContext("Async operation '%s'", o.getId()));

            } else if (o instanceof GetOperationDescriptor) {
                parseGetOperation((GetOperationDescriptor) o, ctx,
                        err.inContext("Get operation '%s'", o.getId()));

            } else if (o instanceof SetOperationDescriptor) {
                parseSetOperation((SetOperationDescriptor) o, ctx,
                        err.inContext("Set operation '%s'", o.getId()));

            } else {
                err.addError(UNSUPPORTED_OPERATION_TYPE, o.getClass());
            }
        }
    }

    private void parsePeriodicOperation(PeriodicOperationDescriptor o,
            ParsingContext ctx, Errors err) {
        Script start = null;
        Script stop = null;

        try {
            start = compileScript(o.getStartScript(), "_start", ctx);
            stop = compileScript(o.getStopScript(), "_stop", ctx);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e.getMessage());
            return;
        }

        if (!start.getEmit().isEmpty()
                || !stop.getEmit().isEmpty()) {
            err.addError(EMIT_NOT_ALLOWED_START_STOP);
            return;
        }

        List<Attribute> emitAtts = new ArrayList<>();
        List<MessageScript> msgs = parsePeriodicOnHandlerDescriptor(o, ctx,
                        err.inContext("sampling 'on' clause"), emitAtts);

        if (msgs == null) {
            return;
        }

        ctx.periodicOpList.add(new NativePeriodicOperation(o.getId(),
                emitAtts, start, stop,
                msgs, ctx.channelMgr));
    }

    private List<MessageScript> parsePeriodicOnHandlerDescriptor(
            PeriodicOperationDescriptor o, ParsingContext ctx, Errors err,
            List<Attribute> emitAtts) {
        int base = 0;
        boolean hasErr = false;
        boolean hasSync = false;

        List<MessageScript> msgs = new ArrayList<>();
        for (OnReceiveDescriptor on : o.getOnReceiveList()) {
            Mapper map = ctx.mappers.get(on.getMessage());
            if (map == null) {
                err.addError(MISSING_MESSAGE_TYPE);
                hasErr = true;
            }
            if (ctx.onMsgHandlerList.contains(on.getMessage())) {
                err.addError(DUPLICATE_ON_HANDLER_SAMPLE,
                        on.getMessage());
                hasErr = true;
            }
            if (Check.nullOrEmpty(on.getVariable())) {
                err.addError(MISSING_VARIABLE_NAME);
                hasErr = true;
            }
            ctx.onMsgHandlerList.add(on.getMessage());

            // Preload the variableTypeMap with the variable corresponding to
            // the message that triggers this 'on' clause being parsed
            Map<String, String> varTypes = new HashMap<>();
            varTypes.put(on.getVariable(), on.getMessage());
            String scriptName = "_" + o.getId() + "_on_" + on.getMessage();

            // Compile the script
            Script script = null;
            try {
                script = compileScript(on.getInstructionList(), scriptName, ctx);
            } catch (InvalidDeviceDescriptorException e) {
                err.addError(e.getMessage());
                return null;
            }

            if (!Collections.disjoint(emitAtts, script.getEmit())) {
                err.addError(HANDLER_COLLISION);
                hasErr = true;
            }
            emitAtts.addAll(script.getEmit());
            if (hasSync && on.isSync()) {
                err.addError(MULTIPLE_ON_SYNC);
                hasErr = true;
            }
            hasSync |= on.isSync();
            msgs.add(new MessageScript(script, map, on.isSync(),
                    on.getVariable(), base));
            base += script.getEmit().size();
        }

        if (hasSync == false && msgs.size() > 1) {
            err.addError(MISSING_ON_SYNC);
            hasErr = true;
        }

        if (hasErr) {
            return null;
        }

        return msgs;
    }

    private void parseAsyncOperation(AsyncOperationDescriptor o,
            ParsingContext ctx, Errors err) {
        Script start = null;

        List<Attribute> emitAtts = new ArrayList<>();
        AsyncMessageHandler handler = parseAsyncOnHandlerDescriptor(o, ctx,
                err.inContext("async 'on' clause"), emitAtts);
        if (handler == null) {
            return;
        }

        if (!o.getStartScript().isEmpty()) {
            try {
                start = compileScript(o.getStartScript(), "_start", ctx);
            } catch (InvalidDeviceDescriptorException e) {
                err.addError(e.getMessage());
                return;
            }
        }

        if (start!= null && !start.getEmit().isEmpty()) {
            err.addError(EMIT_NOT_ALLOWED_START_STOP);
        }

        AsyncOperation asyncOp = new AsyncOperation(o.getId(), emitAtts,
                start, handler, ctx.channelMgr);
        ctx.asyncOpList.add(asyncOp);
        ctx.getOpList.add(asyncOp.getAsyncOneoffOperation());
        ctx.periodicOpList.add(asyncOp.getAsyncPeriodicOperation());
    }

    private AsyncMessageHandler parseAsyncOnHandlerDescriptor(
            AsyncOperationDescriptor o, ParsingContext ctx, Errors err,
            List<Attribute> emitAtts) {
        boolean hasErr = false;
        OnReceiveDescriptor onRecv= o.getOnReceive();

        Mapper map = ctx.mappers.get(onRecv.getMessage());
        if (map == null) {
            err.addError(MISSING_MESSAGE_TYPE);
            hasErr = true;
        }
        if (ctx.onMsgHandlerList.contains(onRecv.getMessage())) {
            err.addError(DUPLICATE_ON_HANDLER_SAMPLE, onRecv.getMessage());
            hasErr = true;
        }
        if (Check.nullOrEmpty(onRecv.getVariable())) {
            err.addError(MISSING_VARIABLE_NAME);
            hasErr = true;
        }
        ctx.onMsgHandlerList.add(onRecv.getMessage());

        // Preload the variableTypeMap with the variable corresponding to
        // the message that triggers this 'on' clause being parsed
        Map<String, String> variableTypeMap = new HashMap<>();
        variableTypeMap.put(onRecv.getVariable(), o.getOnReceive()
                .getMessage());
        String scriptName = "_" + o.getId() + "_on_" + onRecv.getMessage();

        Script script = null;
        try {
            script = compileScript(onRecv.getInstructionList(),
                    scriptName, ctx);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e.getMessage());
            return null;
        }

        if (hasErr) {
            return null;
        }

        // TODO: change the operation constructor so that only the script
        // is required for its creation.
        emitAtts.addAll(script.getEmit());
        return new AsyncMessageHandler(map, script, onRecv.getVariable());
    }

    private void parseGetOperation(GetOperationDescriptor o,
            ParsingContext ctx, Errors err) {
        Script script = null;
        try {
            script = compileScript(o.getScript(), o.getId(), ctx);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e.getMessage());
            return;
        }

        // TODO: change the operation constructor so that only the script
        // is required for its creation.
        ctx.getOpList.add(new OneoffOperation(o.getId(), script.getEmit(),
                script));
        ctx.periodicOpList.add(new SimulatedPeriodicOperation("_" + o.getId()
                + "_sim", script));
    }

    private void parseSetOperation(SetOperationDescriptor o,
            ParsingContext ctx, Errors err) {
        Script script = null;
        try {
            script = compileScript(o.getInstructionList(), o.getId(), ctx);
        } catch (InvalidDeviceDescriptorException e) {
            err.addError(e.getMessage());
            return;
        }

        if (!script.getEmit().isEmpty()) {
            err.addError(EMIT_NOT_ALLOWED_SET);
            return;
        }

        // TODO: change the operation constructor so that only the script
        // is required for its creation.
        ctx.setOpList.add(new OneoffOperation(o.getId(), script.getSet(),
                script));
    }

    private Script compileScript(List<InstructionDescriptor> insts,
            String name, ParsingContext ctx)
            throws InvalidDeviceDescriptorException {
        return Compiler.compile(insts, name, ctx.attDescMap,
                ctx.mappers, ctx.requests, ctx.channels);
    }

    /**
     * Convenience class for storing common information related to the
     * DeviceDescriptor being parsed.
     *
     * @author Guido Rota (2014)
     */
    private class ParsingContext {

        private final ClassPool classPool = new ClassPool(true);

        private final Integer id;

        // Attributes
        private final Map<String, AttributeDescriptor> attDescMap = new HashMap<>();
        private final Set<Attribute> atts = new HashSet<>();
        private final Map<Attribute, Object> attValues = new HashMap<>();

        private final Map<String, Mapper> mappers = new HashMap<>();
        private final Map<String, Channel> channels = new HashMap<>();
        private final Map<String, IORequestBuilder> requests = new HashMap<>();

        private ChannelManager channelMgr;

        // Messages bound to async 'on' scripts in native periodic operations
        private final List<String> onMsgHandlerList = new ArrayList<>();

        // Operations
        private final List<Operation> getOpList = new ArrayList<>();
        private final List<Operation> setOpList = new ArrayList<>();
        private final List<Operation> periodicOpList = new ArrayList<>();
        private final List<AsyncOperation> asyncOpList = new ArrayList<>();

        private ParsingContext(int id) {
            // Adds the classpath of the PerLa middleware to the ClassPool.
            // This allows the ClassPool to find PerLa classes also when
            // instantiated by a webapp server, where multiple classloader
            // may be present
            classPool.insertClassPath(new ClassClassPath(this.getClass()));
            this.id = id;
        }

        protected void add(AttributeDescriptor desc) {
            attDescMap.put(desc.getId(), desc);
            Attribute a = Attribute.create(desc);
            atts.add(a);
            if (desc.getAccess() == AttributeAccessType.STATIC) {
                Object v = DataType.parse(a.getType(), desc.getValue());
                attValues.put(a, v);
            }
        }

        protected void add(Mapper mapper) {
            mappers.put(mapper.getMessageId(), mapper);
        }

        protected void add(String id, Channel channel) {
            channels.put(id, channel);
        }

        protected void add(IORequestBuilder builder) {
            requests.put(builder.getRequestId(), builder);
        }

    }

    /* Error messages */
    private static final String MISSING_DEVICE_TYPE = "Missing device type";
    private static final String MISSING_ATTRIBUTE_DECLARATIONS = "No attribute declarations found";
    private static final String MISSING_ATTRIBUTE_ID = "Empty or missing attribute identifier";
    private static final String DUPLICATE_ATTRIBUTE_ID = "Duplicate attribute identifier '%s'";
    private static final String MISSING_ATTRIBUTE_TYPE = "Missing type";
    private static final String MISSING_STATIC_ATTRIBUTE_VALUE = "Missing value for static-qualified attribute";
    private static final String INVALID_STATIC_ATTRIBUTE_PERMISSION = "Invalid '%s' permission for static attribute (only read-only is allowed)";
    private static final String MISPLACED_ATTRIBUTE_VALUE = "'value' is forbidden for non-STATIC attribute";
    private static final String FORBIDDEN_STATIC_ATTRIBUTE = "Attributes of type 'timestamp' cannot be static";
    private static final String FORBIDDEN_ID_ATTRIBUTE = "Cannot declare reserved 'id' attribute with 'ID' data type";
    private static final String MISSING_MESSAGE_DECLARATIONS = "No message declarations found";
    private static final String MISSING_MESSAGE_ID = "Empty or missing message identifier";
    private static final String DUPLICATE_MESSAGE_ID = "Duplicate message identifier '%s'";
    private static final String DUPLICATE_FIELD_NAME = "Duplicate field name '%s'";
    private static final String MISSING_MAPPER_FACTORY = "No Mapper factory found for message '%s'";
    private static final String MAPPER_CREATION_ERROR = "Error while creating Mapper";
    private static final String MISSING_FIELD_NAME = "Empty or missing field name";
    private static final String MISSING_FIELD_TYPE = "Missing field";
    private static final String MISSING_FIELD_VALUE = "Missing value initializer for STATIC field";
    private static final String MISPLACED_FIELD_VALUE = "Value initializer is forbidden for non-STATIC fields";
    private static final String INVALID_TIMESTMAP_FORMAT = "Forbidden time-format attribute on non timestamp field";
    private static final String MISSING_TIMESTAMP_FORMAT = "Format is required for timestamp field";
    private static final String MISSING_CHANNEL_DECLARATIONS = "No channel declarations found";
    private static final String MISSING_CHANNEL_ID = "Empty or missing channel identifier";
    private static final String DUPLICATE_CHANNEL_ID = "Duplicate channel identifier '%s'";
    private static final String MISSING_CHANNEL_FACTORY = "No Channel factory found for '%s'";
    private static final String CHANNEL_CREATION_ERROR = "Error while creating Channel";
    private static final String MISSING_REQUEST_DECLARATIONS = "No request declarations found";
    private static final String MISSING_REQUEST_ID = "Empty or missing request identifier";
    private static final String DUPLICATE_REQUEST_ID = "Duplicate request identifier '%s'";
    private static final String MISSING_REQUEST_BUILDER_FACTORY = "No IORequestBuilderFactory found for request '%s'";
    private static final String REQUEST_BUILDER_CREATION_ERROR = "Error while creating Request Builder";

    // Operation error messages
    private static final String MISSING_OPERATION_DECLARATIONS = "No Operation declarations found";
    private static final String MISSING_OPERATION_NAME = "Empty or missing operation name";
    private static final String INVALID_OPERATION_NAME = "Operation name cannot start with an underscore character '_'";
    private static final String DUPLICATE_OPERATION_NAME = "Duplicate operation name %s";
    private static final String MISSING_MESSAGE_TYPE = "Empty or missing message type";
    private static final String MISSING_VARIABLE_NAME = "Missing or empty variable name";
    private static final String HANDLER_COLLISION = "Single attribute set by multiple handlers belonging to a single periodic operation";
    private static final String MULTIPLE_ON_SYNC = "Multiple synchronizing event set";
    private static final String MISSING_ON_SYNC = "No synchronizing event set";
    private static final String DUPLICATE_ON_HANDLER_SAMPLE = "Duplicate 'on' handler for message '%s'";
    private static final String UNSUPPORTED_OPERATION_TYPE = "Usupported operation '%s'";
    private static final String EMIT_NOT_ALLOWED_START_STOP = "Emit instruction not allowed in start and stop blocks";
    private static final String EMIT_NOT_ALLOWED_SET = "Emit instruction not allowed in set operation";

}
