package org.dei.perla.core.message.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;

import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.descriptor.FieldDescriptor;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.descriptor.MessageDescriptor;
import org.dei.perla.core.message.AbstractMapperFactory;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.Check;
import org.dei.perla.core.utils.Conditions;
import org.dei.perla.core.utils.Errors;

/**
 * A factory for generating <code>JsonMapper</code>s. This class dynamically
 * creates a <code>JsonMapper</code> from a high level JSON message
 * representation (<code>JsonObjectDescriptor</code>).
 *
 * @author Guido Rota (2014)
 *
 */
public class JsonMapperFactory extends AbstractMapperFactory {

	public JsonMapperFactory() {
		super(JsonObjectDescriptor.class);
	}

	public Mapper createMapper(MessageDescriptor descriptor,
			Map<String, Mapper> mapperMap, ClassPool classPool)
			throws InvalidDeviceDescriptorException {
		Conditions.checkNotNull(descriptor, "descriptor");
		Conditions.checkIllegalArgument(
				descriptor instanceof JsonObjectDescriptor, String.format(
						WRONG_DESCRIPTOR_CLASS, descriptor.getClass()
								.getCanonicalName()));

		JsonMessageContext ctx = new JsonMessageContext(descriptor, classPool,
				mapperMap);
		Errors err = new Errors(OBJECT_CONTEXT, descriptor.getId());
		JsonObjectDescriptor desc = (JsonObjectDescriptor) descriptor;

		Class<FpcMessage> msgClass = createObject(desc, ctx, err);
		if (msgClass == null || !err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}

		if (msgClass == null || !err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}
		return new JsonMapper(descriptor.getId(), msgClass,
				Collections.unmodifiableMap(ctx.msgFieldMap));
	}

	/*
	 * Creates a POJO object given the Json object description contained in the
	 * JsonObjectDescriptor passed as parameter.
	 *
	 * This method may be called recursively if the JsonObjectDescriptor
	 * contains several nested Json objects.
	 */
	private Class<FpcMessage> createObject(JsonObjectDescriptor desc,
			JsonMessageContext ctx, Errors err) {

		if (Check.nullOrEmpty(desc.getId())) {
			err.addError(MISSING_OBJECT_ID);
		}
		String className = "org.dei.perla.core.fpc." + desc.getId()
				+ System.currentTimeMillis();
		ctx.ctClass = ctx.classPool.makeClass(className);
		createFields(desc.getValueList(), ctx, err);

		try {
			ctx.constructorCode.append("}");
			CtConstructor constructor = new CtConstructor(null, ctx.ctClass);
			constructor.setBody(ctx.constructorCode.toString());
			ctx.ctClass.addConstructor(constructor);

			// Creating an constructor that has a single Object as a parameter,
			// needed to use the Javassist CtField.Initializer.byNew
			// initializer.
			CtConstructor initializerConstructor = new CtConstructor(
					new CtClass[] { ctx.classPool.get("java.lang.Object") },
					ctx.ctClass);
			initializerConstructor.setBody("{ this(); }");
			ctx.ctClass.addConstructor(initializerConstructor);

			ctx.ctClass.addInterface(ctx.classPool
					.get("org.dei.perla.core.message.FpcMessage"));
			addFpcMessageMethods(ctx);
			ctx.ctClass.toClass();
			// The implementation of this class guarantees that the cast is safe
			@SuppressWarnings("unchecked")
			Class<FpcMessage> fpcMessageClass = (Class<FpcMessage>) Class
					.forName(ctx.ctClass.getName());
			return fpcMessageClass;

		} catch (CannotCompileException e) {
			err.addError(e, GENERIC_STATIC_INITIALIZER_ERROR);
			return null;
		} catch (NotFoundException e) {
			err.addError(e, CONSTRUCTOR_CREATION_ERROR);
			return null;
		} catch (BadBytecode | ClassNotFoundException e) {
			err.addError(e, UNEXPECTED_ERROR);
			return null;
		}
	}

	private void createFields(List<JsonValueDescriptor> fieldList,
			JsonMessageContext ctx, Errors err) {
		for (JsonValueDescriptor field : fieldList) {
			try {
				ctx.msgFieldMap.put(field.getName(), field);
				Errors fieldErr = err.inContext(FIELD_CONTEXT, field.getName());
				addField(field, ctx, fieldErr);
			} catch (CannotCompileException e) {
				err.addError(e, GENERIC_FIELD_ERROR, field.getName());
			}
		}
	}

	/*
	 * Adds a field to the Pojo, and creates the corollary getter and setter
	 * methods
	 */
	private void addField(JsonValueDescriptor field, JsonMessageContext ctx,
			Errors err) throws CannotCompileException {
		CtClass fieldClass = null;
		if (field.isList()) {
			fieldClass = addListFieldClass(field, ctx, err);
		} else if (DataType.isPrimitive(field.getType())) {
			fieldClass = addScalarField(field, ctx, err);
		} else {
			fieldClass = addComplexFieldClass(field, ctx, err);
		}

		if (fieldClass == null) {
			return;
		}

		String fieldName = field.getName();

		String getterName = "get" + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
		CtMethod getter = new CtMethod(fieldClass, getterName, null,
				ctx.ctClass);
		getter.setBody("{ return " + fieldName + "; }");
		ctx.ctClass.addMethod(getter);

		String setterName = "set" + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
		CtMethod setter = new CtMethod(CtClass.voidType, setterName,
				new CtClass[] { fieldClass }, ctx.ctClass);
		setter.setBody("{ $0." + fieldName + " = $1; }");
		ctx.ctClass.addMethod(setter);
	}

	private CtClass addComplexFieldClass(JsonValueDescriptor field,
			JsonMessageContext ctx, Errors err) throws CannotCompileException {
		Mapper mapper = ctx.mapperMap.get(field.getType());
		if (mapper == null) {
			err.addError(UNDECLARED_COMPLEX_TYPE, field.getType());
			return null;
		}
		if (!(mapper instanceof JsonMapper)) {
			err.addError(INVALID_COMPLEX_TYPE, field.getType());
			return null;
		}
		String className = ((JsonMapper) mapper).getMessageClassName();
		CtClass fieldClass = createCtClass(className, ctx, err);
		addAttributeGetSetCode(field, ctx, className);
		ctx.validatorCode.append("if (!$0." + field.getName()
				+ ".validate()) {\n\t return false; }\n");

		CtField ctField = new CtField(fieldClass, field.getName(), ctx.ctClass);
		ctField.setModifiers(Modifier.PUBLIC);
		ctx.ctClass.addField(ctField, CtField.Initializer.byNew(fieldClass));

		return fieldClass;
	}

	private CtClass addListFieldClass(JsonValueDescriptor field,
			JsonMessageContext ctx, Errors err) throws CannotCompileException {
		String className = "java.util.ArrayList";
		CtClass fieldClass = createCtClass(className, ctx, err);
		addAttributeGetSetCode(field, ctx, className);

		// Validate all elements whose type is complex
		ctx.validatorCode
				.append("for (int i = 0; i < $0."
						+ field.getName()
						+ ".size(); i++) {\n\t "
						+ "if (!($0."
						+ field.getName()
						+ ".get(i) instanceof org.dei.perla.core.message.FpcMessage)) { continue; }"
						+ "org.dei.perla.core.message.FpcMessage m = (org.dei.perla.core.message.FpcMessage) $0."
						+ field.getName()
						+ ".get(i);\n"
						+ "if (m instanceof org.dei.perla.core.message.FpcMessage && !m.validate()) {\n\t\t return false; \n\t } \n}");

		// Append method
		ctx.appendElementCode.append("if (\"" + field.getName()
					+ "\".equals($1)) { $0." + field.getName() + ".add($2); return; }");

		CtField ctField = new CtField(fieldClass, field.getName(), ctx.ctClass);
		ctField.setModifiers(Modifier.PUBLIC);
		ctx.ctClass.addField(ctField);
		ctx.constructorCode.append("$0." + field.getName()
				+ " = new java.util.ArrayList();\n");

		return fieldClass;
	}

	/*
	 * Adds a scalar method to the Pojo
	 */
	private CtClass addScalarField(JsonValueDescriptor field,
			JsonMessageContext ctx, Errors err) throws CannotCompileException {
		DataType type = DataType.valueOf(field.getType().toUpperCase());
		CtClass fieldClass = getScalarFieldClass(type, ctx, err);
		if (fieldClass == null) {
			return null;
		}

		if (type == DataType.TIMESTAMP) {
			addTimestampAttributeGetSetCode(field, ctx, err);
		} else {
			addAttributeGetSetCode(field, ctx, fieldClass.getName());
		}

		if (field.isStatic()) {
			addValidationCode(field, type, ctx, err);
		}

		CtField ctField = new CtField(fieldClass, field.getName(), ctx.ctClass);
		ctField.setModifiers(Modifier.PUBLIC);
		ctx.ctClass.addField(ctField);

		return fieldClass;
	}

	private CtClass getScalarFieldClass(DataType type, JsonMessageContext ctx,
			Errors err) {
		switch (type) {
		case ID:
		case INTEGER:
			return createCtClass("java.lang.Integer", ctx, err);
		case FLOAT:
			return createCtClass("java.lang.Float", ctx, err);
		case STRING:
			return createCtClass("java.lang.String", ctx, err);
		case BOOLEAN:
			return createCtClass("java.lang.Boolean", ctx, err);
		case TIMESTAMP:
			return createCtClass("java.lang.String", ctx, err);
		default:
			throw new RuntimeException("Unexpected '" + type + "' DataType");
		}
	}

	private CtClass createCtClass(String className, JsonMessageContext ctx,
			Errors err) {
		try {
			return ctx.classPool.get(className);
		} catch (NotFoundException e) {
			err.addError(CANNOT_GET_CTCLASS, className);
			return null;
		}
	}

	/*
	 * Adds the necessary source code which will be used by the FpcMessage
	 * validate() method.
	 *
	 * This method performs 3 basic operations: a) Checks if the
	 * JsonObjectDescriptor is correct. b) Creates the necessary validation code
	 * for the current field. c) Creates the necessary initialization code for
	 * the current field, based on the static constructor code specified in the
	 * Device Descriptor
	 */
	private void addValidationCode(JsonValueDescriptor field, DataType type,
			JsonMessageContext ctx, Errors err) {
		ctx.validatorCode.append("if (!$0." + field.getName() + ".equals("
				+ "template." + field.getName()
				+ ")) { \n\t return false; \n}\n");
		addStaticValue(field, type, ctx, err);
	}

	private void addStaticValue(JsonValueDescriptor field,
			DataType type, JsonMessageContext ctx, Errors errors) {
		switch (type) {
		case STRING:
			ctx.constructorCode.append(field.getName() + " = \"" + field.getValue()
					+ "\";");
			break;
		case INTEGER:
			ctx.constructorCode.append(field.getName() + " = new java.lang.Integer("
					+ field.getValue() + ");");
			break;
		case FLOAT:
			ctx.constructorCode.append(field.getName() + " = new java.lang.Float("
					+ field.getValue() + "f);");
			break;
		case BOOLEAN:
			ctx.constructorCode.append(field.getName() + " = new java.lang.Boolean("
					+ field.getValue() + ");");
			break;
		case TIMESTAMP:
			ctx.constructorCode.append("java.time.format.DateTimeFormatter fmt;"
					+ "fmt = java.time.format.DateTimeFormatter.ofPattern(\""
					+ field.getFormat()
					+ "\").withLocale(java.util.Locale.ENGLISH);"
					+ "$0."
					+ field.getName()
					+ " = org.dei.perla.core.utils.DateUtils.format(fmt, " + field.getValue() + ");");
			break;
		default:
			errors.addError(MISPLACED_STATIC_QUALIFIER,
					field.getType());
		}
	}

	/*
	 * Adds the source code required to set and retrieve an attribute value
	 * using the FpcMessage setAttribute() and getAttribute() interface methods.
	 */
	private void addAttributeGetSetCode(JsonValueDescriptor field,
			JsonMessageContext ctx, String castType) {

		ctx.getFieldCode.append("if (\"" + field.getName() + "\".equals($1)) "
				+ "{\n\t return $0." + field.getName() + ";\n}\n");

		if (!field.isStatic() && !field.isList()) {
			ctx.setFieldCode.append("if (\"" + field.getName()
					+ "\".equals($1)) " + "{\n\t $0." + field.getName()
					+ " = (" + castType + ")$2; \n\t return; \n}\n");
		}

		ctx.hasFieldCode.append("if (\"" + field.getName()
				+ "\".equals($1)) {\n\t return true; \n}\n");
	}

	private void addTimestampAttributeGetSetCode(JsonValueDescriptor field,
			JsonMessageContext context, Errors err) {

		context.getFieldCode.append("if (\"" + field.getName()
				+ "\".equals($1)) "
				+ "{ java.time.format.DateTimeFormatter fmt;"
				+ "fmt = java.time.format.DateTimeFormatter.ofPattern(\""
				+ field.getFormat() + "\")"
                + ".withLocale(java.util.Locale.ENGLISH);"
				+ "return org.dei.perla.core.utils.DateUtils.parse(fmt, $0."
				+ field.getName() + "); }");

		if (!field.isStatic()) {
			context.setFieldCode
					.append("if (\""
							+ field.getName()
							+ "\".equals($1)) { java.time.format.DateTimeFormatter fmt;"
							+ "fmt = java.time.format.DateTimeFormatter.ofPattern(\""
							+ field.getFormat()
							+ "\").withLocale(java.util.Locale.ENGLISH);"
							+ "$0."
							+ field.getName()
							+ " = org.dei.perla.core.utils.DateUtils.format(fmt, $2); return; }");
		}

		context.hasFieldCode.append("if (\"" + field.getName()
				+ "\".equals($1)) { return true; }");
	}

	/*
	 * Adds all methods of the FpcMessage interface to the Pojo created by the
	 * JsonMapperFactory.
	 *
	 * The source code of the methods added here is built while parsing the
	 * JsonMessageDescriptor object.
	 */
	private void addFpcMessageMethods(JsonMessageContext ctx)
			throws CannotCompileException, NotFoundException, BadBytecode {

		String className = ctx.ctClass.getName();
		CtClass stringClass = ctx.classPool.get("java.lang.String");
		CtClass objectClass = ctx.classPool.get("java.lang.Object");

		// validate
		CtMethod validationMethod = new CtMethod(CtClass.booleanType,
				"validate", null, ctx.ctClass);

		String validateBody = "{ " + className + " template = (" + className
				+ ")" + className + ".class.newInstance();\n"
				+ ctx.validatorCode + "\nreturn true; \n}";
		validationMethod.setBody(validateBody);
		ctx.ctClass.addMethod(validationMethod);

		// appendElement
		CtMethod appendElement = new CtMethod(CtClass.voidType,
				"appendElement", new CtClass[] { stringClass, objectClass },
				ctx.ctClass);
		String appendBody = "{\n"
				+ ctx.appendElementCode
				+ "\n"
				+ "throw new java.lang.IllegalArgumentException(\"Cannot append, Invalid field name '\" + $1 + \"'\");\n}";
		appendElement.setBody(appendBody);
		ctx.ctClass.addMethod(appendElement);

		// setAttribute
		CtMethod setField = new CtMethod(CtClass.voidType,
				"setField", new CtClass[] { stringClass, objectClass },
				ctx.ctClass);
		String setBody = "{\n"
				+ ctx.setFieldCode
				+ "\n"
				+ "throw new java.lang.IllegalArgumentException(\"Invalid field name '\" + $1 + \"'\");\n}";
		setField.setBody(setBody);
		ctx.ctClass.addMethod(setField);

		// getAttribute
		CtMethod getField = new CtMethod(objectClass, "getField",
				new CtClass[] { stringClass }, ctx.ctClass);
		String getBody = "{ \n"
				+ ctx.getFieldCode
				+ "\n"
				+ "throw new java.lang.IllegalArgumentException(\"Invalid field name '\" + $1 + \"'\");\n}";
		getField.setBody(getBody);
		ctx.ctClass.addMethod(getField);

		// hasAttribute
		CtMethod hasField = new CtMethod(CtClass.booleanType,
				"hasField", new CtClass[] { stringClass }, ctx.ctClass);
		String hasBody = "{\n" + ctx.hasFieldCode + "return false; \n}";
		hasField.setBody(hasBody);
		ctx.ctClass.addMethod(hasField);

		// getId
		CtMethod getId = new CtMethod(stringClass, "getId", null,
				ctx.ctClass);
		getId.setBody("return \"" + ctx.msgDesc.getId() + "\";");
		ctx.ctClass.addMethod(getId);
	}

	/**
	 * Simple convenience class to used to store common information needed at
	 * various stages of the <code>JsonMessageDescriptor</code> parsing
	 * procedure.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class JsonMessageContext {

		private final ClassPool classPool;
		private final MessageDescriptor msgDesc;
		private final Map<String, Mapper> mapperMap;

		private CtClass ctClass;
		private final Map<String, FieldDescriptor> msgFieldMap = new HashMap<>();

		private final StringBuilder constructorCode = new StringBuilder(
				"{ super();");
		private final StringBuilder validatorCode = new StringBuilder();
		private final StringBuilder getFieldCode = new StringBuilder();
		private final StringBuilder setFieldCode = new StringBuilder();
		private final StringBuilder hasFieldCode = new StringBuilder();
		private final StringBuilder appendElementCode = new StringBuilder();

		protected JsonMessageContext(MessageDescriptor messageDescriptor,
				ClassPool classPool, Map<String, Mapper> mapperMap) {
			this.classPool = classPool;
			this.msgDesc = messageDescriptor;
			this.mapperMap = mapperMap;
		}

	}

	/* Error messages */
	private static final String WRONG_DESCRIPTOR_CLASS = "Cannot create JsonMapper: expected "
			+ JsonObjectDescriptor.class.getCanonicalName()
			+ " but received '%s'.";
	private static final String MISSING_OBJECT_ID = "Missing JSON object id.";
	private static final String OBJECT_CONTEXT = "JSON Object descriptor '%s'";
	private static final String FIELD_CONTEXT = "Field '%s'";
	private static final String UNEXPECTED_ERROR = "Unexpected error while creating JsonMapper";
	private static final String GENERIC_FIELD_ERROR = "Unexpected error found near field '%s'";
	private static final String GENERIC_STATIC_INITIALIZER_ERROR = "Unexpected error while creating object. "
			+ "Check initializers code syntax for STATIC qualified fields.";
	private static final String CONSTRUCTOR_CREATION_ERROR = "Unexpected error while creating constructor method.";
	private static final String MISPLACED_STATIC_QUALIFIER = "STATIC qualifier forbidden for type '%s'";
	private static final String UNDECLARED_COMPLEX_TYPE = "Complex type '%s' has not been declared";
	private static final String INVALID_COMPLEX_TYPE = "Invalid complex type '%s', only JSON objects are allowed";
	private static final String CANNOT_GET_CTCLASS = "Cannot get '%s' CtClass. Check class name and package";

}
