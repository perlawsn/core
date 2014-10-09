package org.dei.perla.fpc.base;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.Task;
import org.dei.perla.fpc.TaskHandler;
import org.dei.perla.fpc.base.RecordPipeline.PipelineBuilder;
import org.dei.perla.fpc.descriptor.DataType;
import org.dei.perla.fpc.engine.EmitInstruction;
import org.dei.perla.fpc.engine.Record;
import org.dei.perla.fpc.engine.Script;
import org.dei.perla.fpc.engine.ScriptBuilder;
import org.dei.perla.fpc.engine.StopInstruction;
import org.dei.perla.utils.StopHandler;

public class BaseFpc implements Fpc {

	public static final Attribute TIMESTAMP_ATTRIBUTE = new Attribute(
			"timestamp", DataType.TIMESTAMP);
	public static final Attribute ID_ATTRIBUTE = new Attribute("id",
			DataType.ID);

	private final int id;
	private final Set<Attribute> attributeSet;
	private final Set<StaticAttribute> staticAttributeSet;
	private final ChannelManager channelMgr;
	private final OperationScheduler scheduler;

	// This Operation creates an empty record. It is used for scheduling the
	// periodic creation of empty records, to which additional static fields
	// may be appended, to satisfy a periodic request of static attributes
	private static final Operation emptyRecordOperation;

	static {
		Script doNothing = ScriptBuilder.newScript().add(new EmitInstruction())
				.add(new StopInstruction()).buildScript("empty");
		emptyRecordOperation = new SimulatedPeriodicOperation("_empty",
				Collections.emptySet(), doNothing);
	}

	protected BaseFpc(int id, Set<Attribute> attributeSet,
			Set<StaticAttribute> staticAttributeSet, ChannelManager channelMgr,
			OperationScheduler scheduler) {
		this.id = id;
		this.attributeSet = attributeSet;
		this.staticAttributeSet = staticAttributeSet;
		this.channelMgr = channelMgr;
		this.scheduler = scheduler;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Set<Attribute> getAttributes() {
		return attributeSet;
	}

	protected OperationScheduler getOperationScheduler() {
		return scheduler;
	}

	@Override
	public Task set(Map<Attribute, Object> valueMap, TaskHandler handler) {
		return scheduler.scheduleSet(valueMap, handler);
	}

	@Override
	public Task get(Collection<Attribute> attributes, TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();

		List<Attribute> dynamicAtts = new ArrayList<>();
		List<StaticAttribute> staticAtts = new ArrayList<>();
		filterAttributes(attributes, dynamicAtts, staticAtts);

		if (dynamicAtts.isEmpty() && !staticAtts.isEmpty()) {
			return forwardStaticAttributes(staticAtts, handler);

		} else if (!dynamicAtts.isEmpty() && !staticAtts.isEmpty()) {
			dynamicAtts = new ArrayList<>(attributes);
			dynamicAtts.removeAll(staticAtts);
			pBuilder.add(new RecordModifier.StaticAppender(staticAtts));
		}

		return scheduler.scheduleGet(dynamicAtts, handler, pBuilder);
	}

	public Task forwardStaticAttributes(Collection<StaticAttribute> attributes,
			TaskHandler handler) {
		Map<String, Object> fieldMap = new HashMap<>();
		attributes.stream().forEach(a -> fieldMap.put(a.getId(), a.getValue()));
		fieldMap.put("timestamp", ZonedDateTime.now());
		Record record = Record.from(fieldMap);

		Task task = new CompletedTask(attributes);
		handler.newRecord(task, record);
		handler.complete(task);
		return task;
	}

	@Override
	public Task get(Collection<Attribute> attributes, long periodMs,
			TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();

		List<Attribute> dynamicAtts = new ArrayList<>();
		List<StaticAttribute> staticAtts = new ArrayList<>();
		filterAttributes(attributes, dynamicAtts, staticAtts);

		if (!staticAtts.isEmpty()) {
			pBuilder.add(new RecordModifier.StaticAppender(staticAtts));
		}

		if (dynamicAtts.isEmpty()) {
			pBuilder.add(new RecordModifier.TimestampAppender());
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("period", periodMs);
			return emptyRecordOperation.schedule(paramMap, handler,
					pBuilder.create());

		} else {
			dynamicAtts = new ArrayList<>(attributes);
			dynamicAtts.removeAll(staticAtts);
			return scheduler.schedulePeriodic(dynamicAtts, periodMs, handler,
					pBuilder);
		}
	}

	@Override
	public Task async(Collection<Attribute> attributes, TaskHandler handler) {
		PipelineBuilder pBuilder = RecordPipeline.newBuilder();

		List<Attribute> dynamicAtts = new ArrayList<>();
		List<StaticAttribute> staticAtts = new ArrayList<>();
		filterAttributes(attributes, dynamicAtts, staticAtts);
		if (!staticAtts.isEmpty()) {
			return null;
		}

		return scheduler.scheduleAsync(attributes, handler, pBuilder);
	}

	public void filterAttributes(Collection<Attribute> source,
			Collection<Attribute> dynamicAtts,
			Collection<StaticAttribute> staticAtts) {
		dynamicAtts.addAll(source);
		for (StaticAttribute attribute : staticAttributeSet) {
			if (source.contains(attribute)) {
				staticAtts.add(attribute);
				dynamicAtts.remove(attribute);
			}
		}
	}

	@Override
	public void stop(final StopHandler<Fpc> handler) {
		scheduler.stop((Void) -> { 
			channelMgr.stop();
			handler.hasStopped(this);
		});
	}
	
	/**
	 * Implementation of a {@link Task} that terminates immediately. This class is
	 * mainly used as a return value for 'get' requests that only return static
	 * attributes, or for which the result was already available.
	 * 
	 * @author Guido Rota (2014)
	 *
	 */
	private class CompletedTask implements Task {

		private final Collection<? extends Attribute> attributes;

		public CompletedTask(Collection<? extends Attribute> attributes) {
			this.attributes = attributes;
		}

		@Override
		public boolean isRunning() {
			return false;
		}

		@Override
		public Collection<? extends Attribute> getAttributes() {
			return attributes;
		}

		@Override
		public void stop() {
		}

	}

}
