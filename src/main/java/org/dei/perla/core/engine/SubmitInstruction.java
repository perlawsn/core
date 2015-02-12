package org.dei.perla.core.engine;

import java.util.Optional;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IOHandler;
import org.dei.perla.core.channel.IORequest;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.Payload;
import org.dei.perla.core.engine.ExecutionContext.InstructionLocal;
import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.utils.Conditions;

/**
 * Creates and submits an <code>IORequest</code>. The I/O operation is performed
 * asynchronously: the script is suspended after the request is submitted, and
 * is resumed after the {@code IOTask} is complete.
 *
 * @author Guido Rota (2014)
 *
 */
public class SubmitInstruction implements Instruction {

	// Instruction parameters
	private final IORequestBuilder builder;
	private final Channel channel;
	private final RequestParameter parameterArray[];
	private final String resultVar;
	private final Mapper resultMapper;

	// Instruction status
	private Instruction next;
	private InstructionLocal<Boolean> submitted;

	// Channel response
	private Optional<Payload> channelResponse = null;
	private Throwable channelError = null;

	public SubmitInstruction(IORequestBuilder builder, Channel channel,
			RequestParameter parameterArray[], String resultVar,
			Mapper resultMapper) {
		this.builder = builder;
		this.channel = channel;
		this.parameterArray = parameterArray;
		this.resultVar = resultVar;
		this.resultMapper = resultMapper;
		this.submitted = new InstructionLocal<>(false);
	}

	protected IORequestBuilder getBuilder() {
		return builder;
	}

	protected Channel getChannel() {
		return channel;
	}

	protected RequestParameter[] getParameterArray() {
		return parameterArray;
	}

	protected String getResultVar() {
		return resultVar;
	}

	protected Mapper getResultMapper() {
		return resultMapper;
	}

	@Override
	public void setNext(Instruction instruction) {
		this.next = Conditions.checkNotNull(instruction, "next");
	}

	@Override
	public Instruction next() {
		return next;
	}

	@Override
	public Instruction run(Runner runner) throws ScriptException {
		if (!submitted.getValue(runner)) {
			submitted.setValue(runner, true);
			submitRequest(runner);
			return this;
		} else {
			handleResponse(runner);
			submitted.setValue(runner, false);
			return next;
		}
	}

	private void submitRequest(final Runner runner) throws ScriptException {
		IORequest req = createRequest(runner, builder);

		runner.suspend();
		channel.submit(req, new IOHandler() {
			@Override
			public void complete(IORequest request, Optional<Payload> result) {
				channelResponse = result;
				Executor.resume(runner);
			}

			@Override
			public void error(IORequest request, Throwable cause) {
				channelError = cause;
				Executor.resume(runner);
			}
		});
	}

	private IORequest createRequest(Runner runner, IORequestBuilder builder)
			throws ScriptException {
		ExecutionContext ctx = runner.ctx;

		IORequest req = builder.create();
		check(req != null, "Unexpected error while creating request '"
				+ builder.getRequestId() + "'.");

		for (RequestParameter param : parameterArray) {

			Object msg = ctx.getVariable(param.variable);
			if (!(msg instanceof FpcMessage)) {
				throw new ScriptException("Primitive parameter '"
						+ param.variable
						+ "' is not allowed. Use a complex type (FpcMessage).");
			}
			check(msg != null, "Variable '" + param.variable + "' not found.");
			req.setParameter(param.name, param.mapper.marshal((FpcMessage) msg));
		}

		return req;
	}

	private void handleResponse(Runner runner) throws ScriptException {
		ExecutionContext ctx = runner.ctx;

		if (channelError != null) {
			throw new ScriptException("IO request '" + builder.getRequestId()
					+ "' error in submit instruction.", channelError);
		}

		if (resultVar != null) {
			check(channelResponse.isPresent(),
					"Request '" + builder.getRequestId()
							+ "' did not return any result.");

			FpcMessage msg = resultMapper.unmarshal(channelResponse.get());
			check(msg != null, "Error unmarshalling return value in request '"
					+ builder.getRequestId() + "'.");
			ctx.setVariable(resultVar, msg);
		}
	}

	/**
	 * Throws an exception if the condition doesn't hold
	 *
	 * @param condition
	 *            Condition to check
	 * @param message
	 *            Failure message (added to the exception if thrown)
	 * @throws ScriptException
	 */
	private void check(boolean condition, String message)
			throws ScriptException {
		if (!condition) {
			throw new ScriptException(message);
		}
	}

	/**
	 * Simple object representing a request parameter
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	public static class RequestParameter {

		private final String name;
		private final String variable;
		private final Mapper mapper;

		public RequestParameter(String name, String variable, Mapper mapper) {
			this.name = name;
			this.variable = variable;
			this.mapper = mapper;
		}

	}

}
