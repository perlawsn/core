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
	 * @param inst
	 *            List of {@link InstructionDescriptors}s to be compiled
	 * @param name
	 *            Script name
	 * @param atts
	 *            Map of {@link AttributeDescriptor}s, indexed by attribute name
	 * @param mappers
	 *            Map of message {@link Mapper}s, indexed by message id
	 * @param requests
	 *            Map of {@link IORequestBuilder}s, indexed by request id
	 * @param channels
	 *            Map of {@link Channel}s, indexed by channel id
	 * @return Compiled Script, ready for execution
	 * @throws InvalidDeviceDescriptorException
	 *             If the sequence of {@link InstructionDescriptor}s does not
	 *             correspond to a valid {@link Script}
	 */
	public static CompiledScript compile(
			List<InstructionDescriptor> inst, String name,
			Map<String, AttributeDescriptor> atts,
			Map<String, Mapper> mappers,
			Map<String, IORequestBuilder> requests,
			Map<String, Channel> channels)
			throws InvalidDeviceDescriptorException {
		CompilerContext ctx = new CompilerContext(atts, mappers,
				requests, channels);
		Errors err = new Errors("Script '%s'", name);

		ScriptBuilder bldr = parseInstList(inst, ctx, err);
		if (err.getErrorCount() != 0) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}
		return new CompiledScript(bldr.buildScript(name), ctx.emitSet,
				ctx.setSet);
	}

	private static ScriptBuilder parseInstList(
            List<InstructionDescriptor> descs,
            CompilerContext ctx, Errors err) {
		ScriptBuilder builder = ScriptBuilder.newScript();

		for (InstructionDescriptor d : descs) {
			Instruction i = parseInstruction(d, ctx,
					err.inContext("Instruction nr. " + ctx.instCount));
			builder.add(i);
			ctx.instCount += 1;
		}

		return builder;
	}

	private static Instruction parseInstruction(InstructionDescriptor d,
			CompilerContext ctx, Errors err) {
		Errors iErr;

		if (d instanceof AppendInstructionDescriptor) {
			iErr = err.inContext("Append instruction");
			return parseAppendInstruction((AppendInstructionDescriptor) d,
                    ctx, iErr);

		} else if (d instanceof BreakpointInstructionDescriptor) {
			return new BreakpointInstruction();

		} else if (d instanceof CreateInstructionDescriptor) {
			iErr = err.inContext("Create instruction");
			return parseCreateInstruction((CreateInstructionDescriptor) d,
					ctx, iErr);

		} else if (d instanceof EmitInstructionDescriptor) {
			return new EmitInstruction();

		} else if (d instanceof ErrorInstructionDescriptor) {
			iErr = err.inContext("Error instruction");
			return parseErrorInstruction((ErrorInstructionDescriptor) d,
					ctx, iErr);

		} else if (d instanceof ForeachInstructionDescriptor) {
			iErr = err.inContext("Foreach instruction");
			return parseForeachInstruction(
					(ForeachInstructionDescriptor) d, ctx, iErr);

		} else if (d instanceof IfInstructionDescriptor) {
			iErr = err.inContext("If instruction");
			return parseIfInstruction((IfInstructionDescriptor) d, ctx,
					iErr);

		} else if (d instanceof PutInstructionDescriptor) {

			iErr = err.inContext("Put instruction");
			return parsePutInstruction((PutInstructionDescriptor) d, ctx,
					iErr);

		} else if (d instanceof SetInstructionDescriptor) {
			iErr = err.inContext("Set instruction");
			return parseSetInstruction((SetInstructionDescriptor) d, ctx,
					iErr);

		} else if (d instanceof StopInstructionDescriptor) {
			return new StopInstruction();

		} else if (d instanceof SubmitInstructionDescriptor) {
			iErr = err.inContext("Submit instruction");
			return parseSubmitInstruction((SubmitInstructionDescriptor) d,
					ctx, iErr);

		} else {
			throw new RuntimeException("Cannot parse '"
					+ d.getClass().getSimpleName()
					+ "' instruction descriptor.");
		}
	}

	private static Instruction parseAppendInstruction(
			AppendInstructionDescriptor d, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check variable
		if (Check.nullOrEmpty(d.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			return new NoopInstruction();
		}
		String type = ctx.variableTypeMap.get(d.getVariable());
		if (Check.nullOrEmpty(type)) {
			err.addError(UNDECLARED_VARIABLE, d.getVariable());
			return new NoopInstruction();
		}

		// Check variable field
		if (Check.nullOrEmpty(d.getField())) {
			err.addError(MISSING_FIELD);
			return new NoopInstruction();
		}
		Mapper mapper = ctx.mappers.get(type);
		FieldDescriptor field = mapper.getFieldDescriptor(d.getField());
		if (field == null) {
			err.addError(INVALID_FIELD, d.getField(), d.getVariable(),
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
		if (d.getValue() == null) {
			err.addError(MISSING_EXPRESSION);
			errorFound = true;
		}

		if (errorFound == true) {
			return new NoopInstruction();
		}

		return new AppendInstruction(d.getVariable(), d.getField(),
				fieldClass, d.getValue());
	}

	private static Instruction parseCreateInstruction(
			CreateInstructionDescriptor d, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check variable field
		if (Check.nullOrEmpty(d.getName())) {
			err.addError(MISSING_VARIABLE_NAME);
			errorFound = true;
		} else if (d.getName().equals("param")) {
			err.addError(INVALID_PARAM_VARIABLE_NAME);
			errorFound = true;
		} else if (ctx.variableTypeMap.containsKey(d.getName())) {
			err.addError(DUPLICATE_VARIABLE, d.getName());
			errorFound = true;
		}
		ctx.variableTypeMap.put(d.getName(), d.getType());

		// Check variable type
		if (Check.nullOrEmpty(d.getType())) {
			err.addError(MISSING_MESSAGE_TYPE);
			return new NoopInstruction();
		}

		if (errorFound) {
			return new NoopInstruction();
		}

		if (DataType.isPrimitive(d.getType())) {
			DataType type = DataType.valueOf(d.getType().toUpperCase());
			return new CreatePrimitiveVarInstruction(d.getName(), type);
		} else {
			Mapper mapper = ctx.mappers.get(d.getType());
			if (mapper == null) {
				err.addError(INVALID_TYPE, d.getType());
				return new NoopInstruction();
			}
			return new CreateComplexVarInstruction(d.getName(), mapper);
		}
	}

	private static Instruction parseErrorInstruction(
			ErrorInstructionDescriptor d, CompilerContext ctx, Errors err) {

		if (Check.nullOrEmpty(d.getMessage())) {
			err.addError(MISSING_ERROR_MESSAGE);
			return new NoopInstruction();
		}

		return new ErrorInstruction(d.getMessage());
	}

	private static Instruction parseForeachInstruction(
			ForeachInstructionDescriptor d, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check item variable
		if (Check.nullOrEmpty(d.getItemsVar())) {
			err.addError(MISSING_ITEMS_VARIABLE);
			return new NoopInstruction();
		}
		String varType = ctx.variableTypeMap.get(d.getItemsVar());
		if (varType == null) {
			err.addError(UNDECLARED_VARIABLE, d.getItemsVar());
			return new NoopInstruction();
		}
		if (DataType.isPrimitive(varType)) {
			err.addError(PRIMITIVE_TYPE_NOT_ALLOWED, varType);
			return new NoopInstruction();
		}
		Mapper mapper = ctx.mappers.get(varType);

		// Check item field
		if (Check.nullOrEmpty(d.getItemsField())) {
			err.addError(MISSING_ITEMS_FIELD);
			return new NoopInstruction();
		}
		FieldDescriptor field = mapper
				.getFieldDescriptor(d.getItemsField());
		if (field == null) {
			err.addError(INVALID_FIELD, d.getItemsField(),
					d.getItemsVar(), varType);
			return new NoopInstruction();
		}
		String fieldType = field.getType();

		// Check variable
		if (Check.nullOrEmpty(d.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			errorFound = true;
		} else if (ctx.variableTypeMap.containsKey(d.getVariable())) {
			err.addError(DUPLICATE_VARIABLE);
			errorFound = true;
		}
		ctx.variableTypeMap.put(d.getVariable(), fieldType);

		// Check body
		if (Check.nullOrEmpty(d.getBody())) {
			err.addError(MISSING_FOREACH_BODY);
			return new NoopInstruction();
		}
		ScriptBuilder body = parseInstList(d.getBody(), ctx, err);

		if (errorFound == true) {
			return new NoopInstruction();
		}

		if (Check.nullOrEmpty(d.getIndex())) {
			return new ForeachInstruction(d.getItemsVar(),
					d.getItemsField(), d.getVariable(), body.getCode());
		} else {
			return new ForeachInstruction(d.getItemsVar(),
					d.getItemsField(), d.getVariable(),
					d.getIndex(), body.getCode());
		}
	}

	private static Instruction parseIfInstruction(
			IfInstructionDescriptor d, CompilerContext ctx, Errors err) {
		ScriptBuilder thenBlock = null;
		ScriptBuilder elseBlock = null;
		boolean errorFound = false;

		// Check condition
		if (Check.nullOrEmpty(d.getCondition())) {
			err.addError(MISSING_CONDITION_IF);
			errorFound = true;
		}

		// Parse then and else instruction lists
		thenBlock = parseInstList(d.getThenInstructionList(), ctx, err);
		if (thenBlock == null) {
			err.addError(MISSING_THEN_IF);
			errorFound = true;
		}
		if (!Check.nullOrEmpty(d.getElseInstructionList())) {
			elseBlock = parseInstList(d.getElseInstructionList(), ctx, err);
		}

		if (errorFound) {
			return new NoopInstruction();
		}
		return new IfInstruction(d.getCondition(), thenBlock.getCode(),
				elseBlock.getCode());
	}

	private static Instruction parsePutInstruction(
			PutInstructionDescriptor d, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check value
		if (Check.nullOrEmpty(d.getExpression())) {
			err.addError(MISSING_EXPRESSION);
			errorFound = true;
		}

		// Check attribute
		if (Check.nullOrEmpty(d.getAttribute())) {
			err.addError(MISSING_ATTRIBUTE);
			return new NoopInstruction();
		}
		AttributeDescriptor att = ctx.atts.get(d.getAttribute());
		if (att == null) {
			err.addError(INVALID_ATTRIBUTE_PUT, d.getAttribute());
			return new NoopInstruction();
		} else if (att.getPermission() == AttributePermission.WRITE_ONLY) {
			err.addError(INVALID_PERMISSION_PUT, d.getAttribute());
			errorFound = true;
		}

		if (errorFound) {
			return new NoopInstruction();
		}

        ctx.emitSet.add(Attribute.create(att));
		return new PutInstruction(d.getExpression(), att);
	}

	private static Instruction parseSetInstruction(
			SetInstructionDescriptor d, CompilerContext ctx, Errors err) {
		// Check variable
		if (Check.nullOrEmpty(d.getVariable())) {
			err.addError(MISSING_VARIABLE_NAME);
			return new NoopInstruction();
		}
		String varType = ctx.variableTypeMap.get(d.getVariable());
		if (Check.nullOrEmpty(varType)) {
			err.addError(UNDECLARED_VARIABLE, d.getVariable());
			return new NoopInstruction();
		}

		// Check field
		if (DataType.isComplex(varType) && Check.nullOrEmpty(d.getField())) {
			err.addError(MISSING_FIELD_SET, d.getVariable(), varType);
			return new NoopInstruction();
		} else if (DataType.isPrimitive(varType)
				&& !Check.nullOrEmpty(d.getField())) {
			err.addError(INVALID_FIELD_PRIMITIVE, d.getVariable());
		}

		// Check value
		if (d.getValue() == null) {
			err.addError(MISSING_EXPRESSION);
			return new NoopInstruction();
		}
		// Check if the current set instruction is taking data from a parameter
		// coming from the script caller. If so, add the current attribute to
		// the collection of attributes which are 'set' by this script
		extractAttributes(d.getValue(), ctx, err);

		if (DataType.isPrimitive(varType)) {
			DataType type = DataType.valueOf(varType.toUpperCase());
			return new SetPrimitiveInstruction(d.getVariable(),
					type.getJavaClass(), d.getValue());

		} else {
			Mapper mapper = ctx.mappers.get(varType);
			FieldDescriptor field = mapper.getFieldDescriptor(d.getField());
			if (field == null) {
				err.addError(INVALID_FIELD, d.getField(),
						d.getVariable(), varType);
				return new NoopInstruction();
			}
			// Extract field type
			Class<?> fieldType = null;
			if (!field.isList()) {
				fieldType = DataType.getJavaClass(field.getType());
			} else {
				fieldType = List.class;
			}
			return new SetComplexInstruction(d.getVariable(),
					d.getField(), fieldType, d.getValue());
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
			att = ctx.atts.get(s);
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
            ctx.setSet.add(Attribute.create(att));
		}
		sc.close();
	}

	private static Instruction parseSubmitInstruction(
			SubmitInstructionDescriptor d, CompilerContext ctx, Errors err) {
		boolean errorFound = false;

		// Check request
		if (Check.nullOrEmpty(d.getRequest())) {
			err.addError(MISSING_REQUEST_SUBMIT);
			errorFound = true;
		}
		IORequestBuilder bldr = ctx.requests.get(d.getRequest());
		if (bldr == null) {
			err.addError(INVALID_REQUEST_SUBMIT, d.getRequest());
			// Return immediately, parsing cannot continue if builder is null
			return new NoopInstruction();
		}

		RequestParameter[] parameterArray = createRequestParameterArray(d,
				ctx, err, bldr);
		if (parameterArray == null) {
			errorFound = true;
		}

		Channel channel = ctx.channels.get(d.getChannel());
		if (channel == null) {
			err.addError(INVALID_CHANNEL_ID_SUBMIT);
			errorFound = true;
		}

		Mapper returnHandler = null;
		if (Check.nullOrEmpty(d.getVariable())
				&& !Check.nullOrEmpty(d.getType())) {
			err.addError(MISSING_RETURN_VARIABLE_NAME);
			errorFound = true;

		} else if (!Check.nullOrEmpty(d.getVariable())
				&& Check.nullOrEmpty(d.getType())) {
			err.addError(MISSING_RETURN_MESSAGE_TYPE);
			errorFound = true;

		} else if (!Check.nullOrEmpty(d.getType())
				&& DataType.isPrimitive(d.getType())) {
			err.addError(PRIMITIVE_TYPE_NOT_ALLOWED, d.getType());
			errorFound = true;

		} else if (!Check.nullOrEmpty(d.getVariable())
				&& !Check.nullOrEmpty(d.getType())) {
			ctx.variableTypeMap.put(d.getVariable(), d.getType());
			returnHandler = ctx.mappers.get(d.getType());
		}

		if (errorFound) {
			return new NoopInstruction();
		}

		return new SubmitInstruction(bldr, channel, parameterArray,
				d.getVariable(), returnHandler);
	}

	private static RequestParameter[] createRequestParameterArray(
			SubmitInstructionDescriptor d, CompilerContext ctx, Errors err,
			IORequestBuilder bldr) {
		boolean errorFound = false;

		// Map the variable-parameter bindings declared in the driptor by
		// parameter name, and check binding fields for basic errors
		Map<String, ParameterBinding> paramBindingMap = new HashMap<>();
		for (ParameterBinding binding : d.getParameterList()) {
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
		RequestParameter parameterArray[] = new RequestParameter[d
				.getParameterList().size()];
		int i = 0;
		for (IORequestParameter param : bldr.getParameterList()) {

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
					invalidBinding.getName(), d.getRequest());
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
				ctx.mappers.get(variableType));
	}

	/**
	 * Convenience class for storing common information related to the
	 * <code>Script</code> being parsed.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class CompilerContext {

		private final Map<String, AttributeDescriptor> atts;
		private final Map<String, Mapper> mappers;
		private final Map<String, IORequestBuilder> requests;
		private final Map<String, Channel> channels;

		private final Set<Attribute> emitSet = new HashSet<>();
		private final Set<Attribute> setSet = new HashSet<>();
		private int instCount = 0;

		private final Map<String, String> variableTypeMap = new HashMap<>();

		private CompilerContext(Map<String, AttributeDescriptor> atts,
				Map<String, Mapper> mappers,
				Map<String, IORequestBuilder> requests,
				Map<String, Channel> channels) {
			this.atts = atts;
			this.mappers = mappers;
			this.requests = requests;
			this.channels = channels;
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
