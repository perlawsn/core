package org.dei.perla.core.engine;

import org.dei.perla.core.message.FpcMessage;
import org.dei.perla.core.utils.Check;

import javax.el.*;
import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * A collection of components and data structures used for a single
 * <code>Script</code> execution.
 * </p>
 *
 * <p>
 * This class is also used for creating and storing data samples, i.e. the
 * results of a <code>Script</code> execution. Each data sample is a simple
 * name-value map that is populated through various calls to the
 * <code>putAttribute</code> method. A single <code>Script</code> may create any
 * number of data samples. Once all attributes have been put in the samples, a
 * <code>Script</code> must invoke the <code>emitSample</code> method to output
 * the current sample content.
 * </p>
 *
 * <p>
 * Emitting a sample does not clear the content of the Current Sample. This
 * allows <code>Script</code>s to unroll array messages into different data
 * samples while preserving attributes that are constant for each emitted
 * sample.
 * </p>
 *
 * <p>
 * <code>ExecutionContext</code>s are reuseable objects; once a
 * <code>Script</code> is done the corresponding <code>ExecutionContext</code>
 * can be cleared and repurposed to be used by a different <code>Script</code>
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public class ExecutionContext {

	private final Map<Integer, Object> instructionLocalMap = new HashMap<>();

	private final Map<String, Object> variableMap = new HashMap<>();
	private final ScriptEngineELContext elContext;
	private final FpcEngineVariableMapper elVariableMapper = new FpcEngineVariableMapper();

	private Object[] sample;
	private List<Object[]> samples;

	protected ExecutionContext() {
		elContext = new ScriptEngineELContext(elVariableMapper);
	}

    protected void init(int sampleSize, ScriptParameter[] params) {
        sample = new Object[sampleSize];
        setParameters(params);
    }

	private void setParameters(ScriptParameter[] params) {
		// Register FpcMessage parameters as variables
		for (ScriptParameter p : params) {
			if (p.getValue() instanceof FpcMessage) {
				setVariable(p.getName(), (FpcMessage) p.getValue());
			}
		}
		// Set the param array in the elContext, so that all parameters are
		// available in EL expressions as well
		elContext.setParameterArray(params);
	}

	/**
	 * Returns the <code>ELContext</code> to be used for evaluating EL
	 * expressions found in various <code>Script Instruction</code>s.
	 *
	 * @return <code>ELContext</code>
	 */
	protected ELContext getELContext() {
		return elContext;
	}

	/**
	 * Sets a new variable in the current <code>ExecutionContext</code>.
	 * <code>Script</code> variables are wrapped in a EL
	 * <code>ValueExpression</code> and added to the <code>ELContext</code> to
	 * allow referencing from EL expressions.
	 *
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 */
	protected void setVariable(String name, Object value) {
		variableMap.put(name, value);
		elVariableMapper.setVariable(name,
                Executor.createValueExpression(value, value.getClass()));
	}

	/**
	 * Search and return a variable by name
	 *
	 * @param name
	 *            Name of the variable
	 * @return Variable objects, null if no variable with the specified name is
	 *         found
	 */
	protected Object getVariable(String name) {
		return variableMap.get(name);
	}

	/**
	 * Adds an attribute to the current sample. Invoking this method on an
	 * attribute previously set will overwrite the old value with the new one.
	 *
	 * @param idx
	 *            Attribute index
	 * @param value
	 *            Attribute value
	 */
	protected void putAttribute(int idx, Object value) {
        sample[idx] = value;
	}

	/**
	 * Persists the current samples in the main sample {@link List}. All
	 * samples emitted can be collected after {@code Script} execution
	 * using the {@code getSamples()} method.
	 */
	protected void emitSample() {
        Object[] s = Arrays.copyOf(sample, sample.length);
		samples.add(s);
	}

	/**
	 * Retrieves the list of all the samples emitted by the {@link Script}.
	 * Returns an empty list if the script did not emit any sample.
	 *
	 * @return List of emitted samples
	 */
	protected List<Object[]> getSamples() {
		if (samples.isEmpty()) {
			return Collections.emptyList();
		}

        List<Object[]> res = samples;
        samples = new ArrayList<>(res.size());
		return Collections.unmodifiableList(res);
	}

	/**
	 * Clears the information contained in this <code>ExecutionContext</code>
	 * object so that it can be reused for future <code>Script</code>
	 * executions.
	 */
	protected void clear() {
		instructionLocalMap.clear();
		variableMap.clear();
		elVariableMapper.clear();
        samples = new ArrayList<>();
		elContext.clearParameterMap();
	}

	/**
	 * <p>
	 * A class for storing instruction-local variables, i.e. a variable whose
	 * value instance is local to a certain {@link ExecutionContext}. The same
	 * {@code InstructionLocal} variable, when used from different
	 * {@link ExecutionContext}s, will access different values.
	 * </p>
	 *
	 * <p>
	 * This methanism allows the same instruction to store information that can
	 * be retained throughout different invocations of the same instruction, but
	 * that is not shared among other {@link ExecutionContext}s, i.e. the same
	 * instruction being executed by different {@link Runner}s will access a
	 * different instance when reaching for the {@code InstructionLocal}
	 * internal value.
	 * </p>
	 *
	 * @author Guido Rota (2014)
	 *
	 * @param <E>
	 *            Type of the value contained in the InstructionLocal object
	 */
	public static class InstructionLocal<E> {

		private static final AtomicInteger idGenerator = new AtomicInteger();

		private int id;
		private E initialValue;

		/**
		 * Creates a new {@code InstructionLocal} variable with the specified
		 * initial value
		 *
		 * @param value
		 *            Initial value
		 */
		public InstructionLocal(E value) {
			this.initialValue = value;
			this.id = idGenerator.incrementAndGet();
		}

		/**
		 * Sets a new value for the {@code InstructionLocal} variable in the
		 * current {@link ExecutionContext}, overwriting the previous value.
		 *
		 * @param runner
		 *            {@link Runner} currently executing the instruction
		 * @param value
		 *            value to set
		 */
		public void setValue(Runner runner, E value) {
			ExecutionContext ctx = runner.ctx;
			ctx.instructionLocalMap.put(id, value);
		}

		/**
		 * Retrieves the {@code InstructionLocal} variable value for the current
		 * {@link ExecutionContext}.
		 *
		 * @param runner
		 *            {@link Runner} currently executing the instruction
		 * @return value of the variable stored in the {@code InstructionLocal}
		 *         for the current {@link ExecutionContext}
		 */
		public E getValue(Runner runner) {
			ExecutionContext ctx = runner.ctx;

			if (!ctx.instructionLocalMap.containsKey(id)) {
				ctx.instructionLocalMap.put(id, initialValue);
				return initialValue;
			}

			@SuppressWarnings("unchecked")
			E value = (E) ctx.instructionLocalMap.get(id);
			return value;
		}

	}

	/**
	 * Custom <code>ElContext</code> implementation for the
	 * <code>ScriptEngine</code>. This class acts as a bridge between the Java
	 * Execution Engine and the FPC, allowing PerLa users to access various
	 * PerLa variables and facilities directly from EL expressions.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class ScriptEngineELContext extends ELContext {

		private final VariableMapper variableMapper;
		private final CompositeELResolver resolver;
		private final ParameterELResolver paramResolver;

		public ScriptEngineELContext(VariableMapper variableMapper) {
			this.variableMapper = variableMapper;
			resolver = new CompositeELResolver();
			paramResolver = new ParameterELResolver();
			resolver.add(paramResolver);
			resolver.add(new MapELResolver(true));
			resolver.add(new AttributeELResolver());
			resolver.add(new ListELResolver());
		}

		/**
		 * Clears the content of the map used to store <code>Script</code>
		 * parameters
		 */
		private void clearParameterMap() {
			paramResolver.clearParameterMap();
		}

		/**
		 * Populates the internal data structures with the <code>Script</code>
		 * parameters
		 *
		 * @param paramArray
		 *            Parameter array
		 */
		private void setParameterArray(ScriptParameter[] paramArray) {
			paramResolver.setParameterArray(paramArray);
		}

		@Override
		public ELResolver getELResolver() {
			return resolver;
		}

		@Override
		public FunctionMapper getFunctionMapper() {
			return null; // Not used
		}

		@Override
		public VariableMapper getVariableMapper() {
			return variableMapper;
		}

	}

	/**
	 * Custom <code>VariableMapper</code> implementation for the
	 * <code>FpcEngine</code>. This class provides a variable repository to
	 * allow the creation and manipulation of variables inside a
	 * <code>Script</code>.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class FpcEngineVariableMapper extends VariableMapper {

		private final Map<String, ValueExpression> variableMap = new HashMap<>();

		@Override
		public ValueExpression resolveVariable(String variable) {
			return variableMap.get(variable);
		}

		@Override
		public ValueExpression setVariable(String variable,
				ValueExpression expression) {
			return variableMap.put(variable, expression);
		}

		public void clear() {
			variableMap.clear();
		}

	}

	/**
	 * Custom <code>ELResolver</code> implementation for resolving the
	 * <code>param</code> keyword. This keyword can be used into EL expressions
	 * to access the <code>Script</code> parameters.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class ParameterELResolver extends ELResolver {

		private Map<String, Object> parameterMap = new HashMap<>();

		/**
		 * Clears the content of the map used to store <code>Script</code>
		 * parameters
		 */
		private void clearParameterMap() {
			parameterMap.clear();
		}

		/**
		 * Populates the internal data structures with the <code>Script</code>
		 * parameters
		 *
		 * @param paramArray
		 *            Parameter array
		 */
		private void setParameterArray(ScriptParameter[] paramArray) {
			if (Check.nullOrEmpty(paramArray)) {
				return;
			}

			for (ScriptParameter param : paramArray) {
				parameterMap.put(param.getName(), param.getValue());
			}
		}

		// This function returns the internal parameter map when base == null
		// and property == "param". The values inside the parameter map are then
		// resolved using a MapELResolver (see SCriptEngineELContext).
		@Override
		public Object getValue(ELContext context, Object base, Object property) {
			if (base != null || property == null
					|| !(property instanceof String)
					|| !property.equals("param")) {
				context.setPropertyResolved(false);
				return null;
			}

			context.setPropertyResolved(true);
			return parameterMap;
		}

		@Override
		public Class<?> getType(ELContext context, Object base, Object property) {
			return parameterMap.getClass();
		}

		@Override
		public void setValue(ELContext context, Object base, Object property,
				Object value) {
		}

		@Override
		public boolean isReadOnly(ELContext context, Object base,
				Object property) {
			return true;
		}

		@Override
		public Iterator<FeatureDescriptor> getFeatureDescriptors(
				ELContext context, Object base) {
			return null;
		}

		@Override
		public Class<?> getCommonPropertyType(ELContext context, Object base) {
			return String.class;
		}

	}

	/**
	 * Custom <code>ELResolver</code> implementation for resolving attributes on
	 * PerLa <code>FpcMessage</code> instances.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private class AttributeELResolver extends ELResolver {

		private boolean checkParameters(Object base, Object property) {
			if (base == null || property == null
					|| !(base instanceof FpcMessage)
					|| !(property instanceof String)
					|| !((FpcMessage) base).hasField(property.toString())) {
				return false;
			}
			return true;
		}

		@Override
		public Object getValue(ELContext context, Object base, Object property) {
			if (!checkParameters(base, property)) {
				context.setPropertyResolved(false);
				return null;
			}

			Object value = ((FpcMessage) base)
					.getField(property.toString());
			context.setPropertyResolved(true);
			return value;
		}

		@Override
		public Class<?> getType(ELContext context, Object base, Object property) {
			if (!checkParameters(base, property)) {
				context.setPropertyResolved(false);
				return null;
			}

			context.setPropertyResolved(true);
			return ((FpcMessage) base).getField(property.toString())
					.getClass();
		}

		@Override
		public void setValue(ELContext context, Object base, Object property,
				Object value) {
			if (!checkParameters(base, property)) {
				return;
			}

			((FpcMessage) base).setField(property.toString(), value);
		}

		@Override
		public boolean isReadOnly(ELContext context, Object base,
				Object property) {
			return false;
		}

		@Override
		public Iterator<FeatureDescriptor> getFeatureDescriptors(
				ELContext context, Object base) {
			return null;
		}

		@Override
		public Class<?> getCommonPropertyType(ELContext context, Object base) {
			return String.class;
		}

	}

}
