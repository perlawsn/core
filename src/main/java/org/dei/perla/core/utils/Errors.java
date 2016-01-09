package org.dei.perla.core.utils;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * A class for storing errors and their associated context. <code>Errors</code>
 * instances can be used to store several error messages and exceptions thrown
 * during program execution.</p
 *
 * <p>
 * The main purpose of this class is storing various error messages and
 * exceptions for deferred evaluation. <code>Errors</code> is particularly
 * useful when the execution logic of the program being implemented is required
 * to continue even in presence of errors or warnings.
 * </p>
 *
 * <p>
 * A Parser is a typical example of such a program: the parsing procedure needs
 * to continue even in presence of errors to collect the massimum amount of
 * problems possible. Error management is deferred until the input is completely
 * consumed. Only then the list of errors and warnings is analyzed and shown to
 * the user.
 * </p>
 *
 * <p>
 * Additional context information can be attached to an <code>Errors</code>
 * instance to better indicate the circumstances that caused one or more errors.
 * This additional data is used to contextualize the error messages printed by
 * the <code>asString()</code> method. There are 2 different ways of adding
 * context information:
 * <ul>
 * <li>Instantiate a new <code>Errors</code> object passing a context string as
 * parameter</li>
 * <li>Create a child <code>Errors</code> instance using the
 * <code>inContext()</code> method. The child will inherit parent context
 * information in addition to the context passed as parameter by the
 * <code>inContext()</code> method</li>
 * </ul>
 * New <code>Errors</code> objects created using the <code>inContext()</code>
 * method are connected to the <code>Errors</code> instance from which they were
 * generated. Adding an error to the child instance will increase the parent's
 * error count. Moreover, invocations of the <code>asString()</code> method will
 * retrieve all error messages in the current <code>Errors</code> object and all
 * child instances generated from it.
 * </p>
 * <p>
 * It is important to note that child <code>Errors</code> instances have no
 * access to the messages stored in their parents.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public final class Errors {

	private Errors parent;
	private int errorCount = 0;
	private List<Message> errorList;
	private String context;

	/**
	 * Creates a new empty <code>Errors</code> instance with no context
	 * information.
	 */
	public Errors() {
		parent = null;
		context = null;
	}

	/**
	 * Creates a new empty <code>Errors</code> instance. The context information
	 * is set to the formatted String passed as parameters.
	 *
	 * @param format
	 *            String format used to create the context information
	 * @param objects
	 *            Objects referenced in the format String
	 */
	public Errors(String format, Object... objects) {
		this(String.format(format, objects));
	}

	/**
	 * Creates a new empty <code>Errors</code> instance. The String passed as
	 * parameter is used as context information.
	 *
	 * @param context
	 *            Context information
	 */
	public Errors(String context) {
		this.context = context;
		parent = null;
	}

	/**
	 * Creates a new child <code>Errors</code> instance. This method copies the
	 * parent's context information (if any), appending the context parameter to
	 * it.
	 *
	 * @param parent
	 *            Parent of the new <code>Errors</code> object
	 * @param context
	 *            Context information
	 */
	protected Errors(Errors parent, String context) {
		this.parent = parent;
		this.context = context;
	}

	/**
	 * <p>
	 * Creates a new child <code>Errors</code> instance. This new object is
	 * connected to the parent from which it stems from: all errors added to the
	 * returned <code>Errors</code> are accessible by the parent.
	 * </p>
	 *
	 * <p>
	 * Parent information is used in addition to the explicit data passed as
	 * parameter to create the context information of the returned object.
	 * </p>
	 *
	 *
	 * @param format
	 *            String format used to create the context information
	 * @param objects
	 *            Objects referenced in the format String
	 * @return New child <code>Errors</code> instance
	 */
	public Errors inContext(String format, Object... objects) {
		return inContext(String.format(format, objects));
	}

	/**
	 * <p>
	 * Creates a new child <code>Errors</code> instance. This new object is
	 * connected to the parent from which it stems from: all errors added to the
	 * returned <code>Errors</code> are accessible by the parent.
	 * </p>
	 *
	 * <p>
	 * Parent information is used in addition to the explicit data passed as
	 * parameter to create the context information of the returned object.
	 * </p>
	 *
	 * @param context
	 *            Context information
	 * @return New child <code>Errors</code> instance
	 */
	public Errors inContext(String context) {
		Check.notNull(context, "context");
		Errors child = new Errors(this, context);
		if (errorList == null) {
			errorList = new ArrayList<>();
		}
		errorList.add(new ErrorsMessage(child));
		return child;
	}

	/**
	 * Adds a new error identified by the <code>Throwable</code> passed as
	 * parameter.
	 *
	 * @param cause
	 *            Cause of the error
	 */
	public void addError(Throwable cause) {
		addError(Check.notNull(cause), null);
	}

	/**
	 * Adds a new error identified by the <code>Throwable</code> passed as
	 * parameter. Objects passed as parameter will be formatted according to the
	 * format parameter and appended to the error message.
	 *
	 * @param cause
	 *            Cause of the error
	 * @param format
	 *            String format used to create the error information
	 * @param objects
	 *            Objects referenced in the format String
	 */
	public void addError(Throwable cause, String format, Object... objects) {
		addError(
				Check.notNull(cause),
				String.format(Check.notNull(format), objects)
		);
	}

	/**
	 * Adds a new error identified by the <code>Throwable</code> passed as
	 * parameter. The string passed as parameter is used to provide addition
	 * information about the error.
	 *
	 * @param cause
	 *            Cause of the error
	 * @param message
	 *            Error information
	 */
	public void addError(Throwable cause, String message) {
		addMessageObject(new ThrowableMessage(Check.notNull(cause), message));
	}

	/**
	 * Adds a new error.
	 *
	 * @param message
	 *            String format used to create the error information
	 * @param objects
	 *            Objects referenced in the format String
	 */
	public void addError(String message, Object... objects) {
		addError(String.format(Check.notNull(message), objects));
	}

	/**
	 * Adds a new error.
	 *
	 * @param message
	 *            Error information
	 */
	public void addError(String message) {
		addMessageObject(new StringMessage(Check.notNull(message)));
	}

	private void addMessageObject(Message message) {
		if (errorList == null) {
			errorList = new ArrayList<>();
		}
		errorList.add(message);
		incrementErrorCount();
	}

	private void incrementErrorCount() {
		errorCount += 1;
		if (parent != null) {
			parent.incrementErrorCount();
		}
	}

	/**
	 * Indicates if the <code>Errors</code> object does not contain any error.
	 *
	 * @return true if the <code>Errors</code> object does not contain any
	 *         error, false otherwise.
	 */
	public boolean isEmpty() {
		return errorList == null || errorCount == 0;
	}

	/**
	 * Retrieves the number of errors stored in this object and any of its
	 * children.
	 *
	 * @return Sum of the errors contained in this <code>Errors</code> object
	 *         and all its children.
	 */
	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * <p>
	 * Creates a String representation of all error messages in this
	 * <code>Errors</code> and its children. Returns <code>null</code> if there
	 * are no error.
	 * </p>
	 *
	 * <p>
	 * Context information is automatically appended when present.
	 * </p>
	 *
	 * @return String representation of the errors, null if the object contains
	 *         no errors.
	 */
	public String asString() {
		return asString(null, null);
	}

	/**
	 * <p>
	 * Creates a String representation of all error messages in this
	 * <code>Errors</code> and its children. Returns <code>null</code> if there
	 * are no error.
	 * </p>
	 *
	 * <p>
	 * Context information is automatically appended when present.
	 * </p>
	 *
	 * @param header
	 *            Error header, added to the returned String before the error
	 *            list.
	 * @return String representation of the errors, null if the object contains
	 *         no errors.
	 */
	public String asString(String header) {
		return asString(header, null);
	}

	/**
	 * <p>
	 * Creates a String representation of all error messages in this
	 * <code>Errors</code> and its children. Returns <code>null</code> if there
	 * are no error.
	 * </p>
	 *
	 * <p>
	 * Context information is automatically appended when present.
	 * </p>
	 *
	 * @param header
	 *            Error header, added to the returned String before the error
	 *            list.
	 * @param contextPrefix
	 *            Context prefix
	 * @return String representation of the errors, null if the object contains
	 *         no errors.
	 */
	private String asString(String header, String contextPrefix) {
		if (errorList == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Formatter fmt = new Formatter(builder);
		String suffix;
		if (header != null) {
			fmt.format("%s%n", header);
		}
		if (Check.nullOrEmpty(contextPrefix)) {
			suffix = context == null ? "" : " in " + context;
		} else {
			suffix = context == null ? contextPrefix : contextPrefix + ", "
					+ context;
		}
		Iterator<Message> it = errorList.iterator();
		while (it.hasNext()) {
			String message = it.next().asString(suffix);
			if (Check.nullOrEmpty(message)) {
				continue;
			}
			fmt.format(message);
			if (it.hasNext()) {
				fmt.format("%n");
			}
		}
		fmt.close();
		return builder.toString();
	}

	/**
	 * A generic interface for accessing error information in String format
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private interface Message {

		/**
		 * Returns a String representation of the error message.
		 *
		 * @param suffix
		 *            Suffix to append
		 * @return String representation of the error message
		 */
		public String asString(String suffix);

	}

	/**
	 * Simple <code>Message</code> implementation with a backing String data
	 * structure.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class StringMessage implements Message {

		private String message;

		public StringMessage(String message) {
			this.message = message;
		}

		@Override
		public String asString(String suffix) {
			return String.format("%s%s", message, suffix);
		}

	}

	/**
	 * <code>Message</code> implementation for wrapping an <code>Errors</code>
	 * instance.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class ErrorsMessage implements Message {

		private Errors errors;

		public ErrorsMessage(Errors errors) {
			this.errors = errors;
		}

		@Override
		public String asString(String suffix) {
			return errors.asString(null, suffix);
		}

	}

	/**
	 * <code>Message</code> implementation for storing a <code>Throwable</code>
	 * error.
	 *
	 * @author Guido Rota (2014)
	 *
	 */
	private static class ThrowableMessage implements Message {

		private Throwable cause;
		private String message;

		public ThrowableMessage(Throwable cause, String message) {
			this.cause = cause;
			this.message = message;
		}

		@Override
		public String asString(String suffix) {
			StringBuilder builder = new StringBuilder();
			Formatter fmt = new Formatter(builder);
			if (message != null) {
				fmt.format("%s%s%n", message, suffix);
			} else {
				fmt.format("Error %s%n", suffix);
			}
			fmt.format("%s%n", cause.getMessage());
			for (StackTraceElement elem : cause.getStackTrace()) {
				fmt.format("%s%n", elem.toString());
			}
			fmt.close();
			return builder.toString();
		}

	}

}
