package org.dei.perla.message.urlencoded;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javassist.ClassPool;

import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.descriptor.FieldDescriptor;
import org.dei.perla.fpc.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.fpc.descriptor.MessageDescriptor;
import org.dei.perla.message.AbstractMapperFactory;
import org.dei.perla.message.Mapper;
import org.dei.perla.utils.Conditions;
import org.dei.perla.utils.Errors;

public class UrlEncodedMapperFactory extends AbstractMapperFactory {

	public UrlEncodedMapperFactory() {
		super(UrlEncodedMessageDescriptor.class);
	}

	@Override
	public Mapper createMapper(MessageDescriptor descriptor,
			Map<String, Mapper> mapperMap, ClassPool classPool)
			throws InvalidDeviceDescriptorException {
		Errors err;
		UrlEncodedMessageDescriptor ueMsgDesc;
		ParsingContext ctx;

		Conditions.checkNotNull(descriptor, "descriptor");
		Conditions.checkIllegalArgument(
				descriptor instanceof UrlEncodedMessageDescriptor,
				"Cannot create UrlEncodedMapper: expected "
						+ UrlEncodedMessageDescriptor.class.getCanonicalName()
						+ " but received "
						+ descriptor.getClass().getCanonicalName() + ". ");

		err = new Errors("UrlEncoded message '" + descriptor.getId() + "'");
		ueMsgDesc = (UrlEncodedMessageDescriptor) descriptor;

		if (ueMsgDesc.getParameterList().size() == 0) {
			throw new InvalidDeviceDescriptorException(
					"No parameters found in UrlEncoded message '"
							+ ueMsgDesc.getId() + "'");
		}

		ctx = new ParsingContext();
		for (UrlEncodedParameter param : ueMsgDesc.getParameterList()) {
			Errors paramErr = err.inContext("Parameter '" + param.getName()
					+ "'");
			parseParameter(paramErr, ctx, param);
		}

		if (!err.isEmpty()) {
			throw new InvalidDeviceDescriptorException(err.asString());
		}

		return new UrlEncodedMapper(ueMsgDesc.getId(), ctx.fieldMap,
				ctx.staticFieldMap, ctx.dateFormatterMap);
	}

	private void parseParameter(Errors err, ParsingContext ctx,
			UrlEncodedParameter param) {
		if (!DataType.isPrimitive(param.getType())) {
			err.addError("Cannot create parameter with complex type '"
					+ param.getType()
					+ "'. Only primitive types are supported.");
			return;
		}

		ctx.fieldMap.put(param.getName(), param);
		if (param.isStatic()) {
			ctx.staticFieldMap.put(param.getName(), param.getValue());
		}
		if (DataType.TIMESTAMP.is(param.getType())) {
			ctx.fieldMap.put(param.getName(), param);
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern(param
					.getFormat()).withLocale(Locale.ENGLISH);
			if (fmt.getZone() == null) {
				fmt = fmt.withZone(ZoneId.systemDefault());
			}
			ctx.dateFormatterMap.put(param.getName(), fmt);
		}
	}

	/**
	 * Simple convenience class to used to store common information needed at
	 * various stages of the HttpQueryMessageDescriptor parsing procedure.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class ParsingContext {

		private final Map<String, FieldDescriptor> fieldMap = new HashMap<>();
		private final Map<String, String> staticFieldMap = new HashMap<>();
		private final Map<String, DateTimeFormatter> dateFormatterMap = new HashMap<>();

	}

}
