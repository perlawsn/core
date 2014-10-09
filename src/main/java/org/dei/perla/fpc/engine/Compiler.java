package org.dei.perla.fpc.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.dei.perla.channel.Channel;
import org.dei.perla.channel.IORequestBuilder;
import org.dei.perla.channel.IORequestBuilder.IORequestParameter;
import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.descriptor.AttributeDescriptor;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributeAccessType;
import org.dei.perla.fpc.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.fpc.descriptor.instructions.AppendInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.BreakpointInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.CreateInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.EmitInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.ErrorInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.ForeachInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.IfInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.InstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.ParameterBinding;
import org.dei.perla.fpc.descriptor.instructions.PutInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.SetInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.StopInstructionDescriptor;
import org.dei.perla.fpc.descriptor.instructions.SubmitInstructionDescriptor;
import org.dei.perla.fpc.engine.SubmitInstruction.RequestParameter;
import org.dei.perla.message.FpcMessage;
import org.dei.perla.message.Mapper;
import org.dei.perla.utils.Check;
import org.dei.perla.utils.Errors;

/**
 * Script compiler. This class can be used to compile a list of {@Link
 * InstructionDescriptor}s into an executable {@link Script}.
 * 
 * @author Guido Rota (2014)
 *
 */
public class Compiler {

	/**
	 * Compilers a list of {@link InstructionDescriptor}s into a {@link Script}.
	 * 
	 * @param instructionList
	 *            List of {@link InstructionDescriptors}s to be compiled
	 * @param scriptName
	 *            Script name
	 * @param attributeMap
	 *            Map of {@link AttributeDescriptor}s, indexed by attribute name
	 * @param mapperMap
	 *            Map of message {@link Mapper}s, indexed by message id
	 * @param requestBuilderMap
	 *            Map of {@link IORequestBuilder}s, indexed by request id
	 * @param channelMap
	 *            Map of {@link Channel}s, indexed by channel id
	 * @return Compiled Script, ready for execution
	 * @throws InvalidDeviceDescriptorException
	 *             If the sequence of {@link InstructionDescriptor}s does not
	 *             correspond to a valid {@link Script}
	 */
	public static CompiledScript compile(
			List<InstructionDescriptor> instructionList, String scriptName,
			Map<String, AttributeDescriptor> attributeMap,
			Map<String, Mapper> mapperMap,
			Map<String, IORequestBuilder> requestBuilderMap,
			Map<String, Channel> channelMap)
			throws InvalidDeviceDescriptorException {
		CompilerContext ctx = new CompilerContext(attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		Errors err = new Errors("Script '%s'", scriptName);

		ScriptBuilder builder = parseInstList(instructionList, ctx, err);
		if (err.getErrorCount() != 0) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}
		return new CompiledScript(builder.buildScript(scriptName), ctx.emitSet,
				ctx.setSet);
	}

	private static ScriptBuilder parseInstList(
			List<InstructionDescriptor> iList, CompilerContext ctx, Errors err) {
		ScriptBuilder builder = ScriptBuilder.newScript();

		for (InstructionDescriptor iDesc : iList) {
			Instruction inst = parseInstruction(iDesc, ctx,
					err.inContext("Instruction nr. " + ctx.instCount));
			builder.add(inst);
			ctx.instCount += 1;
		}

		return builder;
	}

	private static Instruction parseInstruction(InstructionDescriptor iDesc,
			CompilerContext ctx, Errors err) {
		Errors iErr;

		if (iDesc instanceof AppendInstructionDescriptor) {
			iErr = err.inContext("Append instruction");
			return parseAppendInstruction((AppendInstructionDescriptor) iDesc,
					ctx, iErr);

		} else if (iDesc instanceof BreakpointInstructionDescriptor) {
			return new BreakpointInstruction();

		} else if (iDesc instanceof CreateInstructionDescriptor) {
			iErr = err.inContext("Create instruction");
			return parseCreateInstruction((CreateInstructionDescriptor) iDesc,
					ctx, iErr);

		} else if (iDesc instanceof EmitInstructionDescriptor) {
			return new EmitInstruction();

		} else if (iDesc instanceof ErrorInstructionDescriptor) {
			iErr = err.inContext("Error instruction");
			return parseErrorInstruction((ErrorInstructionDescriptor) iDesc,
					ctx, iErr);

		} else if (iDesc instanceof ForeachInstructionDescriptor) {
			iErr = err.inContext("Foreach instruction");
			return parseForeachInstruction(
					(ForeachInstructionDescriptor) iDesc, ctx, iErr);

		} else if (iDesc instanceof IfInstructionDescriptor) {
			iErr = err.inContext("If instruction");
			return parseIfInstruction((IfInstructionDescriptor) iDesc, ctx,
					iErr);

		} else if (iDesc instanceof PutInstructionDescriptor) {

			iErr = err.inContext("Put instruction");
			return parsePutInstruction((PutInstructionDescriptor) iDesc, ctx,
					iErr);

		} else if (iDesc instanceof SetInstructionDescriptor) {
			iErr = err.inContext("Set instruction");
			return parseSetInstruction((SetInstructionDescriptor) iDesc, ctx,
					iErr);

		} else if (iDesc instanceof StopInstructionDescriptor) {
			return new StopInstruction();

		} else if (iDesc instanceof SubmitInstructionDescriptor) {
			iErr = err.inContext("Submit instruction");
			return parseSubmitInstruction((SubmitInstructionDescriptor) iDesc,
					ctx, iErr);

		} else {
			throw new RuntimeException("Cannot parse '"
					+ iDesc.getClass().getSimpleName()
					+ "' instruction descriptor.");
		}
	}

	private static Instruction parseAppendInstruction(
			AppendInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check variable
		if (Check.nullOrEmpty(iDesc.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			return new NoopInstruction();
		}
		String type = ctx.variableTypeMap.get(iDesc.getVariable());
		if (Check.nullOrEmpty(type)) {
			err.addError(UNDECLARED_VARIABLE, iDesc.getVariable());
			return new NoopInstruction();
		}

		// Check variable field
		if (Check.nullOrEmpty(iDesc.getField())) {
			err.addError(MISSING_FIELD);
			return new NoopInstruction();
		}
		Mapper mapper = ctx.mapperMap.get(type);
		FieldDescriptor field = mapper.getFieldDescriptor(iDesc.getField());
		if (field == null) {
			err.addError(INVALID_FIELD, iDesc.getField(), iDesc.getVariable(),
					type);
			errorFound = true;
		}
		String fieldType = field.getType();
		Class<?> fieldClass = null;
		if (DataType.isPrimitive(fieldType)) {
			fieldClass = DataType.getJavaClass(fieldType);
		} else {
			fieldClass = FpcMessage.class;
		}

		// Check value
		if (iDesc.getValue() == null) {
			err.addError(MISSING_EXPRESSION);
			errorFound = true;
		}

		if (errorFound == true) {
			return new NoopInstruction();
		}

		return new AppendInstruction(iDesc.getVariable(), iDesc.getField(),
				fieldClass, iDesc.getValue());
	}

	private static Instruction parseCreateInstruction(
			CreateInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check variable field
		if (Check.nullOrEmpty(iDesc.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			errorFound = true;
		} else if (iDesc.getVariable().equals("param")) {
			err.addError(INVALID_PARAM_VARIABLE_NAME);
			errorFound = true;
		} else if (ctx.variableTypeMap.containsKey(iDesc.getVariable())) {
			err.addError(DUPLICATE_VARIABLE, iDesc.getVariable());
			errorFound = true;
		}
		ctx.variableTypeMap.put(iDesc.getVariable(), iDesc.getType());

		// Check variable type
		if (Check.nullOrEmpty(iDesc.getType())) {
			err.addError(MISSING_MESSAGE_TYPE);
			return new NoopInstruction();
		}

		if (errorFound) {
			return new NoopInstruction();
		}

		if (DataType.isPrimitive(iDesc.getType())) {
			DataType type = DataType.valueOf(iDesc.getType().toUpperCase());
			return new CreatePrimitiveInstruction(iDesc.getVariable(), type);
		} else {
			Mapper mapper = ctx.mapperMap.get(iDesc.getType());
			if (mapper == null) {
				err.addError(INVALID_TYPE, iDesc.getType());
				return new NoopInstruction();
			}
			return new CreateComplexInstruction(iDesc.getVariable(), mapper);
		}
	}

	private static Instruction parseErrorInstruction(
			ErrorInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {

		if (Check.nullOrEmpty(iDesc.getMessage())) {
			err.addError(MISSING_ERROR_MESSAGE);
			return new NoopInstruction();
		}

		return new ErrorInstruction(iDesc.getMessage());
	}

	private static Instruction parseForeachInstruction(
			ForeachInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check item variable
		if (Check.nullOrEmpty(iDesc.getItemsVar())) {
			err.addError(MISSING_ITEMS_VARIABLE);
			return new NoopInstruction();
		}
		String varType = ctx.variableTypeMap.get(iDesc.getItemsVar());
		if (varType == null) {
			err.addError(UNDECLARED_VARIABLE, iDesc.getItemsVar());
			return new NoopInstruction();
		}
		if (DataType.isPrimitive(varType)) {
			err.addError(PRIMITIVE_TYPE_NOT_ALLOWED, varType);
			return new NoopInstruction();
		}
		Mapper mapper = ctx.mapperMap.get(varType);

		// Check item field
		if (Check.nullOrEmpty(iDesc.getItemsField())) {
			err.addError(MISSING_ITEMS_FIELD);
			return new NoopInstruction();
		}
		FieldDescriptor field = mapper
				.getFieldDescriptor(iDesc.getItemsField());
		if (field == null) {
			err.addError(INVALID_FIELD, iDesc.getItemsField(),
					iDesc.getItemsVar(), varType);
			return new NoopInstruction();
		}
		String fieldType = field.getType();

		// Check variable
		if (Check.nullOrEmpty(iDesc.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			errorFound = true;
		} else if (ctx.variableTypeMap.containsKey(iDesc.getVariable())) {
			err.addError(DUPLICATE_VARIABLE);
			errorFound = true;
		}
		ctx.variableTypeMap.put(iDesc.getVariable(), fieldType);

		// Check body
		if (Check.nullOrEmpty(iDesc.getBody())) {
			err.addError(MISSING_FOREACH_BODY);
			return new NoopInstruction();
		}
		ScriptBuilder body = parseInstList(iDesc.getBody(), ctx, err);

		if (errorFound == true) {
			return new NoopInstruction();
		}

		if (Check.nullOrEmpty(iDesc.getIndex())) {
			return new ForeachInstruction(iDesc.getItemsVar(),
					iDesc.getItemsField(), iDesc.getVariable(), body.getCode());
		} else {
			return new ForeachInstruction(iDesc.getItemsVar(),
					iDesc.getItemsField(), iDesc.getVariable(),
					iDesc.getIndex(), body.getCode());
		}
	}

	private static Instruction parseIfInstruction(
			IfInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		ScriptBuilder thenBlock = null;
		ScriptBuilder elseBlock = null;
		boolean errorFound = false;

		// Check condition
		if (Check.nullOrEmpty(iDesc.getCondition())) {
			err.addError(MISSING_CONDITION_IF);
			errorFound = true;
		}

		// Parse then and else instruction lists
		thenBlock = parseInstList(iDesc.getThenInstructionList(), ctx, err);
		if (thenBlock == null) {
			err.addError(MISSING_THEN_IF);
			errorFound = true;
		}
		if (!Check.nullOrEmpty(iDesc.getElseInstructionList())) {
			elseBlock = parseInstList(iDesc.getElseInstructionList(), ctx, err);
		}

		if (errorFound) {
			return new NoopInstruction();
		}
		return new IfInstruction(iDesc.getCondition(), thenBlock.getCode(),
				elseBlock.getCode());
	}

	private static Instruction parsePutInstruction(
			PutInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check value
		if (Check.nullOrEmpty(iDesc.getExpression())) {
			err.addError(MISSING_EXPRESSION);
			errorFound = true;
		}

		// Check attribute
		if (Check.nullOrEmpty(iDesc.getAttribute())) {
			err.addError(MISSING_ATTRIBUTE);
			return new NoopInstruction();
		}
		AttributeDescriptor att = ctx.attributeMap.get(iDesc.getAttribute());
		if (att == null) {
			err.addError(INVALID_ATTRIBUTE_PUT, iDesc.getAttribute());
			return new NoopInstruction();
		} else if (att.getPermission() == AttributePermission.WRITE_ONLY) {
			err.addError(INVALID_PERMISSION_PUT, iDesc.getAttribute());
			errorFound = true;
		}

		if (errorFound) {
			return new NoopInstruction();
		}

		ctx.emitSet.add(new Attribute(att));
		return new PutInstruction(iDesc.getExpression(), att);
	}

	private static Instruction parseSetInstruction(
			SetInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		// Check variable
		if (Check.nullOrEmpty(iDesc.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			return new NoopInstruction();
		}
		String varType = ctx.variableTypeMap.get(iDesc.getVariable());
		if (Check.nullOrEmpty(varType)) {
			err.addError(UNDECLARED_VARIABLE, iDesc.getVariable());
			return new NoopInstruction();
		}

		// Check field
		if (DataType.isComplex(varType) && Check.nullOrEmpty(iDesc.getField())) {
			err.addError(MISSING_FIELD_SET, iDesc.getVariable(), varType);
			return new NoopInstruction();
		} else if (DataType.isPrimitive(varType)
				&& !Check.nullOrEmpty(iDesc.getField())) {
			err.addError(INVALID_FIELD_PRIMITIVE, iDesc.getVariable());
		}

		// Check value
		if (iDesc.getValue() == null) {
			err.addError(MISSING_EXPRESSION);
			return new NoopInstruction();
		}
		// Check if the current set instruction is taking data from a parameter
		// coming from the script caller. If so, add the current attribute to
		// the collection of attributes which are 'set' by this script
		extractAttributes(iDesc.getValue(), ctx, err);

		if (DataType.isPrimitive(varType)) {
			DataType type = DataType.valueOf(varType.toUpperCase());
			return new SetPrimitiveInstruction(iDesc.getVariable(),
					type.getJavaClass(), iDesc.getValue());

		} else {
			Mapper mapper = ctx.mapperMap.get(varType);
			FieldDescriptor field = mapper.getFieldDescriptor(iDesc.getField());
			if (field == null) {
				err.addError(INVALID_FIELD, iDesc.getField(),
						iDesc.getVariable(), varType);
				return new NoopInstruction();
			}
			// Extract field type
			Class<?> fieldType = null;
			if (!field.isList()) {
				fieldType = DataType.getJavaClass(field.getType());
			} else {
				fieldType = List.class;
			}
			return new SetComplexInstruction(iDesc.getVariable(),
					iDesc.getField(), fieldType, iDesc.getValue());
		}
	}

	private static void extractAttributes(String value, CompilerContext ctx,
			Errors err) {
		if (!value.matches(".*\\$\\{.*param\\['.*'\\].*\\}.*")) {
			return;
		}

		AttributeDescriptor att;
		Scanner sc = new Scanner(value);
		for (String s; (s = sc.findWithinHorizon("(?<=\\[').*?(?=\\'])", 0)) != null;) {
			att = ctx.attributeMap.get(s);
			if (att == null) {
				err.addError(UNDECLARED_ATTRIBUTE, s);
				continue;
			}
			if (att.getAccess() == AttributeAccessType.STATIC) {
				err.addError(STATIC_ATTRIBUTE_SET, s);
				continue;
			}
			if (att.getPermission() == AttributePermission.READ_ONLY) {
				err.addError(READ_ONLY_ATTRIBUTE, s);
				continue;
			}
			ctx.setSet.add(new Attribute(att));
		}
		sc.close();
	}

	private static Instruction parseSubmitInstruction(
			SubmitInstructionDescriptor iDesc, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check request
		if (Check.nullOrEmpty(iDesc.getRequest())) {
			err.addError(MISSING_REQUEST_SUBMIT);
			errorFound = true;
		}
		IORequestBuilder builder = ctx.requestBuilderMap
				.get(iDesc.getRequest());
		if (builder == null) {
			err.addError(INVALID_REQUEST_SUBMIT, iDesc.getRequest());
			// Return immediately, parsing cannot continue if builder is null
			return new NoopInstruction();
		}

		RequestParameter[] parameterArray = createRequestParameterArray(iDesc,
				ctx, err, builder);
		if (parameterArray == null) {
			errorFound = true;
		}

		Channel channel = ctx.channelMap.get(iDesc.getChannel());
		if (channel == null) {
			err.addError(INVALID_CHANNEL_ID_SUBMIT);
			errorFound = true;
		}

		Mapper returnHandler = null;
		if (Check.nullOrEmpty(iDesc.getVariable())
				&& !Check.nullOrEmpty(iDesc.getType())) {
			err.addError(MISSING_RETURN_VARIABLE_NAME);
			errorFound = true;

		} else if (!Check.nullOrEmpty(iDesc.getVariable())
				&& Check.nullOrEmpty(iDesc.getType())) {
			err.addError(MISSING_RETURN_MESSAGE_TYPE);
			errorFound = true;

		} else if (!Check.nullOrEmpty(iDesc.getType())
				&& DataType.isPrimitive(iDesc.getType())) {
			err.addError(PRIMITIVE_TYPE_NOT_ALLOWED, iDesc.getType());
			errorFound = true;

		} else if (!Check.nullOrEmpty(iDesc.getVariable())
				&& !Check.nullOrEmpty(iDesc.getType())) {
			ctx.variableTypeMap.put(iDesc.getVariable(), iDesc.getType());
			returnHandler = ctx.mapperMap.get(iDesc.getType());
		}

		if (errorFound) {
			return new NoopInstruction();
		}

		return new SubmitInstruction(builder, channel, parameterArray,
				iDesc.getVariable(), returnHandler);
	}

	private static RequestParameter[] createRequestParameterArray(
			SubmitInstructionDescriptor desc, CompilerContext ctx, Errors err,
			IORequestBuilder builder) {
		boolean errorFound = false;

		// Map the variable-parameter bindings declared in the descriptor by
		// parameter name, and check binding fields for basic errors
		Map<String, ParameterBinding> paramBindingMap = new HashMap<>();
		for (ParameterBinding binding : desc.getParameterList()) {
			if (Check.nullOrEmpty(binding.getName())) {
				err.addError(MISSING_PARAM_NAME_SUBMIT);
				errorFound = true;
				continue;
			}
			if (Check.nullOrEmpty(binding.getVariable())) {
				err.addError(MISSING_VARIABLE_NAME);
				errorFound = true;
				continue;
			}
			paramBindingMap.put(binding.getName(), binding);
		}

		// Create RequestParameter objects needed to execute the submit
		// instruction
		RequestParameter parameterArray[] = new RequestParameter[desc
				.getParameterList().size()];
		int i = 0;
		for (IORequestParameter param : builder.getParameterList()) {

			ParameterBinding binding = paramBindingMap.get(param.getName());
			if (!param.isMandatory() && binding == null) {
				// Avoid parsing an optional parameter if it hasn't been bound
				continue;
			} else if (param.isMandatory() && binding == null) {
				// Add an error if a mandatory parameter hasn't been bound
				err.addError(MISSING_PARAM_SUBMIT);
				continue;
			}

			parameterArray[i] = createRequestParameter(ctx, err, param, binding);
			if (parameterArray[i] == null && param.isMandatory()) {
				errorFound = true;
			}
			i += 1;
			paramBindingMap.remove(param.getName());
		}

		// Additional bindings not belonging to the request being submitted are
		// treated as errors
		for (ParameterBinding invalidBinding : paramBindingMap.values()) {
			err.addError(UNSUPPORTED_PARAMETER_BINDING_SUBMIT,
					invalidBinding.getName(), desc.getRequest());
			errorFound = true;
		}

		if (errorFound) {
			return null;
		}
		return parameterArray;
	}

	// Returns null in case of error
	private static RequestParameter createRequestParameter(CompilerContext ctx,
			Errors err, IORequestParameter param, ParameterBinding binding) {
		boolean errorFound = false;

		String variableType = ctx.variableTypeMap.get(binding.getVariable());
		if (variableType == null) {
			err.addError(UNDECLARED_VARIABLE, binding.getVariable());
			errorFound = true;
		}

		if (errorFound == true) {
			return null;
		}

		return new RequestParameter(binding.getName(), binding.getVariable(),
				ctx.mapperMap.get(variableType));
	}

	/**
	 * Convenience class for storing common information related to the
	 * <code>Script</code> being parsed.
	 * 
	 * @author Guido Rota (2014)
	 * 
	 */
	private static class CompilerContext {

		private final Map<String, AttributeDescriptor> attributeMap;
		private final Map<String, Mapper> mapperMap;
		private final Map<String, IORequestBuilder> requestBuilderMap;
		private final Map<String, Channel> channelMap;

		private final Set<Attribute> emitSet = new HashSet<>();
		private final Set<Attribute> setSet = new HashSet<>();
		private int instCount = 0;

		private final Map<String, String> variableTypeMap = new HashMap<>();

		private CompilerContext(Map<String, AttributeDescriptor> attributeMap,
				Map<String, Mapper> mapperMap,
				Map<String, IORequestBuilder> requestBuilderMap,
				Map<String, Channel> channelMap) {
			this.attributeMap = attributeMap;
			this.mapperMap = mapperMap;
			this.requestBuilderMap = requestBuilderMap;
			this.channelMap = channelMap;
		}

	}

	// Error messages
	private static final String MISSING_VARIABLE_NAME = "Missing or empty variable name";
	private static final String INVALID_PARAM_VARIABLE_NAME = "'param' is a reserved keyword and cannot be used as a variable names";
	private static final String MISSING_MESSAGE_TYPE = "Empty or missing message type";
	private static final String DUPLICATE_VARIABLE = "Duplicate instantiation of variable '%s'";
	private static final String INVALID_TYPE = "Invalid type '%s'";
	private static final String MISSING_ERROR_MESSAGE = "Missing error message";
	private static final String MISSING_ITEMS_VARIABLE = "Missing or empty item variable name";
	private static final String MISSING_ITEMS_FIELD = "Missing or empty item variable field";
	private static final String MISSING_FOREACH_BODY = "Missing body";
	private static final String MISSING_CONDITION_IF = "Missing or empty condition";
	private static final String MISSING_THEN_IF = "Missing or empty then clause";
	private static final String MISSING_EXPRESSION = "Missing or empty expression";
	private static final String MISSING_FIELD_SET = "Missing field for complex variable '%s' of type '%s'";
	private static final String INVALID_FIELD_PRIMITIVE = "Invalid field attribute for primitive variable '%s'";
	private static final String MISSING_ATTRIBUTE = "Missing or empty attribute";
	private static final String INVALID_ATTRIBUTE_PUT = "Invalid attempt to put non existing attribute '%s'";
	private static final String INVALID_PERMISSION_PUT = "Invalid attempt to put write-only attribute '%s'";
	private static final String UNDECLARED_VARIABLE = "Undeclared variable '%s'";
	private static final String PRIMITIVE_TYPE_NOT_ALLOWED = "Primitive variable of type '%s' is not allowed. Use a complex variable instead.";
	private static final String MISSING_FIELD = "Missing or empty field attribute";
	private static final String INVALID_FIELD = "Field '%s' does not exist in variable '%s' of type '%s'";
	private static final String UNDECLARED_ATTRIBUTE = "Undeclared attribute '%s'";
	private static final String STATIC_ATTRIBUTE_SET = "Static attribute '%s' cannot be used as data source.";
	private static final String READ_ONLY_ATTRIBUTE = "Read only attribute '%s' cannot be used as data source.";
	private static final String MISSING_REQUEST_SUBMIT = "Missing request identifier";
	private static final String INVALID_REQUEST_SUBMIT = "Invalid request '%s' identifier";
	private static final String INVALID_CHANNEL_ID_SUBMIT = "Invalid channel identifier '%s'";
	private static final String MISSING_PARAM_NAME_SUBMIT = "Missing or empty parameter name";
	private static final String MISSING_PARAM_SUBMIT = "Mandatory parameter '%s' has not been bound to any variable";
	private static final String UNSUPPORTED_PARAMETER_BINDING_SUBMIT = "Invalid parameter '%s' for request '%s'";
	private static final String MISSING_RETURN_VARIABLE_NAME = "Missing return variable name, add variable name or remove return message";
	private static final String MISSING_RETURN_MESSAGE_TYPE = "Missing return message type, add message type or remove return variable";

}
