package org.dei.perla.fpc.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;

import org.apache.log4j.Logger;
import org.dei.perla.channel.Channel;
import org.dei.perla.channel.ChannelFactory;
import org.dei.perla.channel.IORequestBuilder;
import org.dei.perla.channel.IORequestBuilderFactory;
import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.FpcFactory;
import org.dei.perla.fpc.base.AsyncOperation.AsyncMessageHandler;
import org.dei.perla.fpc.base.NativePeriodicOperation.PeriodicMessageHandler;
import org.dei.perla.fpc.descriptor.AsyncOperationDescriptor;
import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributeAccessType;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.fpc.descriptor.ChannelDescriptor;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.descriptor.DeviceDescriptor;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.fpc.descriptor.GetOperationDescriptor;
import org.dei.perla.fpc.descriptor.IORequestDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.fpc.descriptor.MessageDescriptor;
import org.dei.perla.fpc.descriptor.OnReceiveDescriptor;
import org.dei.perla.fpc.descriptor.OperationDescriptor;
import org.dei.perla.fpc.descriptor.PeriodicOperationDescriptor;
import org.dei.perla.fpc.descriptor.SetOperationDescriptor;
import org.dei.perla.fpc.descriptor.instructions.InstructionDescriptor;
import org.dei.perla.fpc.engine.CompiledScript;
import org.dei.perla.fpc.engine.Compiler;
import org.dei.perla.message.Mapper;
import org.dei.perla.message.MapperFactory;
import org.dei.perla.utils.Check;
import org.dei.perla.utils.Conditions;
import org.dei.perla.utils.Errors;

public class BaseFpcFactory implements FpcFactory {

	private final Logger logger = Logger.getLogger(BaseFpcFactory.class);
	private final Map<Class<? extends MessageDescriptor>, MapperFactory> mapperFactoryMap = new HashMap<>();
	private final Map<Class<? extends ChannelDescriptor>, ChannelFactory> channelFactoryMap = new HashMap<>();
	private final Map<Class<? extends IORequestDescriptor>, IORequestBuilderFactory> requestBuilderFactoryMap = new HashMap<>();

	public BaseFpcFactory(List<MapperFactory> mapperFactoryList,
			List<ChannelFactory> channelFactoryList,
			List<IORequestBuilderFactory> requestBuilderFactoryList) {
		Conditions.checkNotNull(mapperFactoryList, "mapperFactoryList");
		Conditions.checkNotNull(channelFactoryList, "channelFactoryList");

		for (MapperFactory factory : mapperFactoryList) {
			mapperFactoryMap.put(factory.acceptedMessageDescriptorClass(),
					factory);
		}
		for (ChannelFactory factory : channelFactoryList) {
			channelFactoryMap.put(factory.acceptedChannelDescriptorClass(),
					factory);
		}
		for (IORequestBuilderFactory factory : requestBuilderFactoryList) {
			requestBuilderFactoryMap.put(factory.acceptedIORequestClass(),
					factory);
		}
	}

	@Override
	public Fpc createFpc(DeviceDescriptor descriptor, int id)
			throws InvalidDeviceDescriptorException {
		Conditions.checkNotNull(descriptor, "descriptor");
		ParsingContext ctx = new ParsingContext(id);

		Errors errors = parseDescriptor(descriptor, ctx);
		if (!errors.isEmpty()) {
			logger.error(errors.asString());
			throw new InvalidDeviceDescriptorException(errors.asString());
		}

		OperationScheduler scheduler = new OperationScheduler(ctx.getOpList,
				ctx.setOpList, ctx.periodicOpList, ctx.asyncOpList);
		return new BaseFpc(id, ctx.attributeSet, ctx.staticAttributeSet,
				ctx.channelMgr, scheduler);
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
	 * @param ctx
	 *            Data structure for storing intermediate parsing result
	 * @return Number of errors found while parsing the Device Descriptor
	 */
	private Errors parseDescriptor(DeviceDescriptor desc, ParsingContext ctx) {
		Errors err = new Errors("Device descriptor '%s'", desc.getName());

		// Check device name
		String deviceName = desc.getName();
		if (Check.nullOrEmpty(deviceName)) {
			err.addError(MISSING_DEVICE_TYPE);
		}

		// Parse device attributes
		List<AttributeDescriptor> attList = desc.getAttributeList();
		if (attList.size() == 0) {
			err.addError(MISSING_ATTRIBUTE_DECLARATIONS);
		}
		parseAttributeList(attList, ctx, err);

		// Parse device messages
		List<MessageDescriptor> msgList = desc.getMessageList();
		if (msgList.size() == 0) {
			err.addError(MISSING_MESSAGE_DECLARATIONS);
		}
		parseMessageList(msgList, ctx, err);

		// Parse device channels
		List<ChannelDescriptor> chList = desc.getChannelList();
		if (chList.size() == 0) {
			err.addError(MISSING_CHANNEL_DECLARATIONS);
		}
		parseChannelList(chList, ctx, err);
		ctx.channelMgr = new ChannelManager(new ArrayList<>(
				ctx.channelMap.values()));

		// Parse channel requests
		List<IORequestDescriptor> reqList = desc.getRequestList();
		if (reqList.size() == 0) {
			err.addError(MISSING_REQUEST_DECLARATIONS);
		}
		parseRequestList(reqList, ctx, err);

		if (!err.isEmpty()) {
			// Operation parsing can't be performed properly if there are errors
			// in previous descriptor sections
			return err;
		}

		// Parse operations
		List<OperationDescriptor> opList = desc.getOperationList();
		if (opList.size() == 0) {
			err.addError(MISSING_OPERATION_DECLARATIONS);
		}
		parseOperationList(opList, ctx, err);

		return err;
	}

	private void parseAttributeList(List<AttributeDescriptor> attList,
			ParsingContext ctx, Errors err) {
		boolean hasNativeTimestamp = false;
		List<String> attIdList = new ArrayList<>();

		for (AttributeDescriptor att : attList) {
			String id = att.getId();
			if (Check.nullOrEmpty(id)) {
				err.addError(MISSING_ATTRIBUTE_ID);
			}
			if (attIdList.contains(id)) {
				err.addError(DUPLICATE_ATTRIBUTE_ID, id);
			}
			if (att.getType() == DataType.TIMESTAMP) {
				hasNativeTimestamp = true;
			}
			attIdList.add(att.getId());
			parseAttribute(att, ctx, err.inContext("Attribute '%s'", id));
		}

		if (!hasNativeTimestamp) {
			ctx.addAttribute(new AttributeDescriptor("timestamp",
					DataType.TIMESTAMP, AttributePermission.READ_ONLY));
		}

		ctx.addAttribute(new AttributeDescriptor("id", DataType.ID, ctx.id
				.toString()));
	}

	private void parseAttribute(AttributeDescriptor att, ParsingContext ctx,
			Errors err) {
		// Check missing attribute type
		if (att.getType() == null) {
			err.addError(MISSING_ATTRIBUTE_TYPE);
		}

		// Check attribute access
		if (att.getAccess() == AttributeAccessType.STATIC
				&& Check.nullOrEmpty(att.getValue())) {
			err.addError(MISSING_STATIC_ATTRIBUTE_VALUE);

		} else if (att.getAccess() == AttributeAccessType.STATIC
				&& att.getPermission() != AttributePermission.READ_ONLY) {
			err.addError(INVALID_STATIC_ATTRIBUTE_PERMISSION, att.getAccess());

		} else if (att.getAccess() != AttributeAccessType.STATIC
				&& !Check.nullOrEmpty(att.getValue())) {
			err.addError(MISPLACED_ATTRIBUTE_VALUE);
		}

		// Check timestamp attribute
		if (att.getType() == DataType.TIMESTAMP
				&& att.getAccess() == AttributeAccessType.STATIC) {
			err.addError(FORBIDDEN_STATIC_ATTRIBUTE);
		}

		// Check id attribute
		if (att.getId().compareToIgnoreCase("id") == 0
				&& att.getType() == DataType.ID) {
			err.addError(FORBIDDEN_ID_ATTRIBUTE);
		}

		ctx.addAttribute(att);
	}

	private void parseMessageList(List<MessageDescriptor> msgList,
			ParsingContext ctx, Errors err) {
		Set<String> messageIdSet = new HashSet<>();

		for (MessageDescriptor msg : msgList) {
			String id = msg.getId();
			if (Check.nullOrEmpty(id)) {
				err.addError(MISSING_MESSAGE_ID);
			}
			if (messageIdSet.contains(id)) {
				err.addError(DUPLICATE_MESSAGE_ID, id);
			}
			messageIdSet.add(id);
			parseMessage(msg, ctx, err.inContext("Message '%s'", id));
		}
	}

	private void parseMessage(MessageDescriptor msg, ParsingContext ctx,
			Errors err) {
		MapperFactory mapperFactory;

		// Check fields
		List<String> fieldNameSet = new ArrayList<>();
		for (FieldDescriptor field : msg.getFieldList()) {
			if (fieldNameSet.contains(field.getName())) {
				err.addError(DUPLICATE_FIELD_NAME, field.getName());
			}
			fieldNameSet.add(field.getName());
			checkField(field, ctx, err.inContext("Field '%s'", field.getName()));
		}

		if (!err.isEmpty()) {
			// Return immediately without creating the Mapper
			return;
		}

		// Return immediately without creating the Mapper creation
		mapperFactory = mapperFactoryMap.get(msg.getClass());
		if (mapperFactory == null) {
			err.addError(MISSING_MAPPER_FACTORY, msg.getClass());
			return;
		}

		Mapper mapper = null;
		try {
			mapper = mapperFactory.createMapper(msg, ctx.mapperMap,
					ctx.classPool);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e, MAPPER_CREATION_ERROR);
			return;
		}

		if (mapper == null) {
			err.addError(MAPPER_CREATION_ERROR);
			return;
		}
		ctx.addMapper(mapper);
	}

	private void checkField(FieldDescriptor field, ParsingContext ctx,
			Errors err) {
		String fieldName = field.getName();
		String value = field.getValue();

		// Check field name
		if (Check.nullOrEmpty(fieldName)) {
			err.addError(MISSING_FIELD_NAME);
		}

		// Check field type
		if (Check.nullOrEmpty(field.getType())) {
			err.addError(MISSING_FIELD_TYPE);
		}

		// Check static fields
		if (field.isStatic() && Check.nullOrEmpty(value)) {
			err.addError(MISSING_FIELD_VALUE);

		} else if (!field.isStatic() && !Check.nullOrEmpty(value)) {
			err.addError(MISPLACED_FIELD_VALUE);
		}

		// Check Timestamp
		if (!DataType.TIMESTAMP.is(field.getType())
				&& !Check.nullOrEmpty(field.getFormat())) {
			err.addError(INVALID_TIMESTMAP_FORMAT);
		} else if (DataType.TIMESTAMP.is(field.getType())
				&& !Check.nullOrEmpty(field.getFormat())) {
			err.addError(MISSING_TIMESTAMP_FORMAT);
		}
	}

	private void parseChannelList(List<ChannelDescriptor> chList,
			ParsingContext ctx, Errors err) {
		Set<String> channelIdSet = new HashSet<>();

		for (ChannelDescriptor ch : chList) {
			String id = ch.getId();
			if (Check.nullOrEmpty(id)) {
				err.addError(MISSING_CHANNEL_ID);
			}
			if (channelIdSet.contains(ch.getId())) {
				err.addError(DUPLICATE_CHANNEL_ID, ch.getId());
			}
			channelIdSet.add(ch.getId());
			parseChannel(ch, ctx, err.inContext("Channel '%s'", ch.getId()));
		}
	}

	private void parseChannel(ChannelDescriptor chDesc, ParsingContext ctx,
			Errors err) {

		ChannelFactory fct = channelFactoryMap.get(chDesc.getClass());
		if (fct == null) {
			err.addError(MISSING_CHANNEL_FACTORY, chDesc.getClass());
		}

		try {
			Channel ch = fct.createChannel(chDesc);
			ctx.addChannel(chDesc.getId(), ch);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e, CHANNEL_CREATION_ERROR);
		}
	}

	private void parseRequestList(List<IORequestDescriptor> reqList,
			ParsingContext ctx, Errors err) {
		Set<String> requestIdSet = new HashSet<>();

		for (IORequestDescriptor req : reqList) {
			if (Check.nullOrEmpty(req.getId())) {
				err.addError(MISSING_REQUEST_ID);
			}
			if (requestIdSet.contains(req.getId())) {
				err.addError(DUPLICATE_REQUEST_ID, req.getId());
			}
			requestIdSet.add(req.getId());
			parseRequest(req, ctx, err.inContext("Request '%s'", req.getId()));
		}
	}

	private void parseRequest(IORequestDescriptor req, ParsingContext ctx,
			Errors err) {
		IORequestBuilderFactory fct = requestBuilderFactoryMap.get(req
				.getClass());
		if (fct == null) {
			err.addError(MISSING_REQUEST_BUILDER_FACTORY, req.getId());
			return;
		}

		try {
			ctx.addRequestBuilder(fct.create(req));
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e, REQUEST_BUILDER_CREATION_ERROR);
		}
	}

	private void parseOperationList(List<OperationDescriptor> opList,
			ParsingContext ctx, Errors err) {
		Set<String> opIdSet = new HashSet<>();

		for (OperationDescriptor op : opList) {
			// Checks common operation fields
			if (Check.nullOrEmpty(op.getId())) {
				err.addError(MISSING_OPERATION_NAME);
			}
			if (op.getId().startsWith("_")) {
				err.addError(INVALID_OPERATION_NAME);
			}
			if (opIdSet.contains(op.getId())) {
				err.addError(DUPLICATE_OPERATION_NAME, op.getId());
			}
			opIdSet.add(op.getId());

			if (op instanceof PeriodicOperationDescriptor) {
				parsePeriodicOperation((PeriodicOperationDescriptor) op, ctx,
						err.inContext("Sampling operation '%s'", op.getId()));

			} else if (op instanceof AsyncOperationDescriptor) {
				parseAsyncOperation((AsyncOperationDescriptor) op, ctx,
						err.inContext("Async operation '%s'", op.getId()));

			} else if (op instanceof GetOperationDescriptor) {
				parseGetOperation((GetOperationDescriptor) op, ctx,
						err.inContext("Get operation '%s'", op.getId()));

			} else if (op instanceof SetOperationDescriptor) {
				parseSetOperation((SetOperationDescriptor) op, ctx,
						err.inContext("Set operation '%s'", op.getId()));

			} else {
				err.addError(UNSUPPORTED_OPERATION_TYPE, op.getClass());
			}
		}
	}

	private void parsePeriodicOperation(PeriodicOperationDescriptor op,
			ParsingContext ctx, Errors err) {
		CompiledScript startCScript = null;
		CompiledScript stopCScript = null;

		try {
			startCScript = compileScript(op.getStartScript(), "_start", ctx);

			stopCScript = compileScript(op.getStopScript(), "_stop", ctx);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e.getMessage());
			return;
		}

		if (!startCScript.getEmitSet().isEmpty()
				|| !stopCScript.getEmitSet().isEmpty()) {
			err.addError(EMIT_NOT_ALLOWED_START_STOP);
			return;
		}

		Set<Attribute> emitAttSet = new HashSet<>();
		List<PeriodicMessageHandler> handlerList = parsePeriodicOnHandlerDescriptor(
				op, ctx, err.inContext("sampling 'on' clause"), emitAttSet);

		if (handlerList == null) {
			return;
		}

		ctx.periodicOpList.add(new NativePeriodicOperation(op.getId(),
				emitAttSet, startCScript.getScript(), stopCScript.getScript(),
				handlerList, ctx.channelMgr));
	}

	private List<PeriodicMessageHandler> parsePeriodicOnHandlerDescriptor(
			PeriodicOperationDescriptor op, ParsingContext ctx, Errors err,
			Set<Attribute> emitAttSet) {
		boolean errorFound = false;
		boolean syncFound = false;

		List<PeriodicMessageHandler> handlerList = new ArrayList<>();
		for (OnReceiveDescriptor onRecvDesc : op.getOnReceiveList()) {
			Mapper mapper = ctx.mapperMap.get(onRecvDesc.getMessage());
			if (mapper == null) {
				err.addError(MISSING_MESSAGE_TYPE);
				errorFound = true;
			}
			if (ctx.onMsgHandlerList.contains(onRecvDesc.getMessage())) {
				err.addError(DUPLICATE_ON_HANDLER_SAMPLE,
						onRecvDesc.getMessage());
				errorFound = true;
			}
			if (Check.nullOrEmpty(onRecvDesc.getVariable())) {
				err.addError(MISSING_VARIABLE_NAME);
				errorFound = true;
			}
			ctx.onMsgHandlerList.add(onRecvDesc.getMessage());

			// Preload the variableTypeMap with the variable corresponding to
			// the message that triggers this 'on' clause being parsed
			Map<String, String> variableTypeMap = new HashMap<>();
			variableTypeMap.put(onRecvDesc.getVariable(),
					onRecvDesc.getMessage());
			String scriptName = "_" + op.getId() + "_on_"
					+ onRecvDesc.getMessage();

			// Compile the script
			CompiledScript cScript = null;
			try {
				cScript = compileScript(onRecvDesc.getInstructionList(),
						scriptName, ctx);
			} catch (InvalidDeviceDescriptorException e) {
				err.addError(e.getMessage());
				return null;
			}

			emitAttSet.addAll(cScript.getEmitSet());
			if (syncFound && onRecvDesc.isSync()) {
				err.addError(MULTIPLE_ON_SYNC);
				errorFound = true;
			}
			syncFound |= onRecvDesc.isSync();
			handlerList.add(new PeriodicMessageHandler(onRecvDesc.isSync(),
					mapper, onRecvDesc.getVariable(), cScript.getScript()));
		}

		if (syncFound == false && handlerList.size() > 1) {
			err.addError(MISSING_ON_SYNC);
			errorFound = true;
		}

		if (errorFound) {
			return null;
		}

		return handlerList;
	}

	private void parseAsyncOperation(AsyncOperationDescriptor op,
			ParsingContext ctx, Errors err) {
		CompiledScript startScript = null;

		Set<Attribute> emitAttSet = new HashSet<>();
		AsyncMessageHandler handler = parseAsyncOnHandlerDescriptor(op, ctx,
				err.inContext("async 'on' clause"), emitAttSet);
		if (handler == null) {
			return;
		}

		if (!op.getStartScript().isEmpty()) {
			try {
				startScript = compileScript(op.getStartScript(), "_start", ctx);
			} catch (InvalidDeviceDescriptorException e) {
				err.addError(e.getMessage());
				return;
			}
		}

		if (startScript != null && !startScript.getEmitSet().isEmpty()) {
			err.addError(EMIT_NOT_ALLOWED_START_STOP);
		}

		AsyncOperation asyncOp = new AsyncOperation(op.getId(), emitAttSet,
				startScript.getScript(), handler, ctx.channelMgr);
		ctx.asyncOpList.add(asyncOp);
		ctx.getOpList.add(asyncOp.getAsyncOneoffOperation());
		ctx.periodicOpList.add(asyncOp.getAsyncPeriodicOperation());
	}

	private AsyncMessageHandler parseAsyncOnHandlerDescriptor(
			AsyncOperationDescriptor op, ParsingContext ctx, Errors err,
			Set<Attribute> emitAttSet) {
		boolean errorFound = false;
		OnReceiveDescriptor onRecvDesc = op.getOnReceive();

		Mapper mapper = ctx.mapperMap.get(onRecvDesc.getMessage());
		if (mapper == null) {
			err.addError(MISSING_MESSAGE_TYPE);
			errorFound = true;
		}
		if (ctx.onMsgHandlerList.contains(onRecvDesc.getMessage())) {
			err.addError(DUPLICATE_ON_HANDLER_SAMPLE, onRecvDesc.getMessage());
			errorFound = true;
		}
		if (Check.nullOrEmpty(onRecvDesc.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			errorFound = true;
		}
		ctx.onMsgHandlerList.add(onRecvDesc.getMessage());

		// Preload the variableTypeMap with the variable corresponding to
		// the message that triggers this 'on' clause being parsed
		Map<String, String> variableTypeMap = new HashMap<>();
		variableTypeMap.put(onRecvDesc.getVariable(), op.getOnReceive()
				.getMessage());
		String scriptName = "_" + op.getId() + "_on_" + onRecvDesc.getMessage();

		CompiledScript cScript = null;
		try {
			cScript = compileScript(onRecvDesc.getInstructionList(),
					scriptName, ctx);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e.getMessage());
			return null;
		}

		if (errorFound) {
			return null;
		}

		emitAttSet.addAll(cScript.getEmitSet());
		return new AsyncMessageHandler(mapper, cScript.getScript(),
				onRecvDesc.getVariable());
	}

	private void parseGetOperation(GetOperationDescriptor op,
			ParsingContext ctx, Errors err) {
		CompiledScript cScript = null;
		try {
			cScript = compileScript(op.getInstructionList(), op.getId(), ctx);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e.getMessage());
			return;
		}

		ctx.getOpList.add(new OneoffOperation(op.getId(), cScript.getEmitSet(),
				cScript.getScript()));
		ctx.periodicOpList.add(new SimulatedPeriodicOperation("_" + op.getId()
				+ "_sim", cScript.getEmitSet(), cScript.getScript()));
	}

	private void parseSetOperation(SetOperationDescriptor op,
			ParsingContext ctx, Errors err) {
		CompiledScript cScript = null;
		try {
			cScript = compileScript(op.getInstructionList(), op.getId(), ctx);
		} catch (InvalidDeviceDescriptorException e) {
			err.addError(e.getMessage());
			return;
		}

		if (!cScript.getEmitSet().isEmpty()) {
			err.addError(EMIT_NOT_ALLOWED_SET);
			return;
		}

		ctx.setOpList.add(new OneoffOperation(op.getId(), cScript.getSetSet(),
				cScript.getScript()));
	}

	private CompiledScript compileScript(
			List<InstructionDescriptor> instructionList, String scriptName,
			ParsingContext ctx) throws InvalidDeviceDescriptorException {
		return Compiler.compile(instructionList, scriptName, ctx.attDescMap,
				ctx.mapperMap, ctx.reqBuilderMap, ctx.channelMap);
	}

	/**
	 * Convenience class for storing common information related to the
	 * DeviceDescriptor being parsed.
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	private class ParsingContext {

		private final ClassPool classPool = new ClassPool(true);

		private final Integer id;

		// Attributes
		private final Map<String, AttributeDescriptor> attDescMap = new HashMap<>();
		private final Set<Attribute> attributeSet = new HashSet<>();
		private final Set<StaticAttribute> staticAttributeSet = new HashSet<>();

		private final Map<String, Mapper> mapperMap = new HashMap<>();
		private final Map<String, Channel> channelMap = new HashMap<>();
		private final Map<String, IORequestBuilder> reqBuilderMap = new HashMap<>();

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

		protected void addAttribute(AttributeDescriptor desc) {
			attDescMap.put(desc.getId(), desc);
			if (desc.getAccess() == AttributeAccessType.STATIC) {
				StaticAttribute att = new StaticAttribute(desc);
				attributeSet.add(att);
				staticAttributeSet.add(att);
			} else {
				attributeSet.add(new Attribute(desc));
			}
		}

		protected void addMapper(Mapper mapper) {
			mapperMap.put(mapper.getMessageId(), mapper);
		}

		protected void addChannel(String id, Channel channel) {
			channelMap.put(id, channel);
		}

		protected void addRequestBuilder(IORequestBuilder builder) {
			reqBuilderMap.put(builder.getRequestId(), builder);
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
	private static final String MULTIPLE_ON_SYNC = "Multiple synchronizing event set";
	private static final String MISSING_ON_SYNC = "No synchronizing event set";
	private static final String DUPLICATE_ON_HANDLER_SAMPLE = "Duplicate 'on' handler for message '%s'";
	private static final String UNSUPPORTED_OPERATION_TYPE = "Usupported operation '%s'";
	private static final String EMIT_NOT_ALLOWED_START_STOP = "Emit instruction not allowed in start and stop blocks";
	private static final String EMIT_NOT_ALLOWED_SET = "Emit instruction not allowed in set operation";

}
