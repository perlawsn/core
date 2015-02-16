package org.dei.perla.core.engine;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.loopback.LoopbackChannel;
import org.dei.perla.core.channel.loopback.LoopbackIORequestBuilder;
import org.dei.perla.core.channel.loopback.TestFieldDescriptor;
import org.dei.perla.core.channel.loopback.TestMapper;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.core.descriptor.DataType;
import org.dei.perla.core.descriptor.FieldDescriptor.FieldQualifier;
import org.dei.perla.core.descriptor.instructions.AppendInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.BreakpointInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.CreateVarInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.EmitInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.ErrorInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.IfInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.InstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.PutInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.SetInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.StopInstructionDescriptor;
import org.dei.perla.core.descriptor.instructions.SubmitInstructionDescriptor;
import org.dei.perla.core.message.Mapper;
import org.junit.BeforeClass;
import org.junit.Test;

public class CompilerTest {

	private static AttributeDescriptor integer;
	private static AttributeDescriptor string;
	private static AttributeDescriptor bool;

	private static TestFieldDescriptor integerField;
	private static TestFieldDescriptor stringField;
	private static TestFieldDescriptor listField;
	private static TestMapper mapper1;
	private static TestMapper mapper2;

	private static IORequestBuilder request1;
	private static Channel channel;

	private static Map<String, AttributeDescriptor> attributeMap;
	private static Map<String, Mapper> mapperMap;
	private static Map<String, IORequestBuilder> requestBuilderMap;
	private static Map<String, Channel> channelMap;

	@BeforeClass
	public static void setup() {
		integer = new AttributeDescriptor("integer", DataType.INTEGER,
				AttributePermission.READ_WRITE);
		string = new AttributeDescriptor("string", DataType.STRING,
				AttributePermission.READ_WRITE);
		bool = new AttributeDescriptor("bool", DataType.BOOLEAN,
				AttributePermission.READ_WRITE);
		attributeMap = new HashMap<>();
		attributeMap.put(integer.getId(), integer);
		attributeMap.put(string.getId(), string);
		attributeMap.put(bool.getId(), bool);

		integerField = new TestFieldDescriptor("integer", FieldQualifier.FIELD,
				"integer", null, null, null);
		stringField = new TestFieldDescriptor("string", FieldQualifier.FIELD,
				"string", null, null, null);
		listField = new TestFieldDescriptor("list", FieldQualifier.LIST,
				"string", null, null, null);
		mapper1 = new TestMapper("message1");
		mapper1.addField(integerField);
		mapper1.addField(stringField);
		mapper2 = new TestMapper("message2");
		mapper2.addField(integerField);
		mapper2.addField(stringField);
		mapper2.addField(listField);
		mapperMap = new HashMap<>();
		mapperMap.put(mapper1.getMessageId(), mapper1);
		mapperMap.put(mapper2.getMessageId(), mapper2);

		request1 = new LoopbackIORequestBuilder("request1");
		requestBuilderMap = new HashMap<>();
		requestBuilderMap.put(request1.getRequestId(), request1);

		channel = new LoopbackChannel();
		channelMap = new HashMap<>();
		channelMap.put("loopback", channel);
	}

	@Test
	public void testAppendInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message2"));
		iList.add(new AppendInstructionDescriptor("var", "list", "5"));

		script = Compiler.compile(iList, "append", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		i = i.next();
		assertTrue(i instanceof AppendInstruction);
		AppendInstruction app = (AppendInstruction) i;
		assertThat(app.getVariable(), equalTo("var"));
		assertThat(app.getField(), equalTo("list"));
		assertThat(app.getValue(), equalTo("5"));
	}

	@Test
	public void testBreakpointInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new BreakpointInstructionDescriptor());

		script = Compiler.compile(iList, "breakpoint", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof BreakpointInstruction);
	}

	@Test
	public void testStopInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new StopInstructionDescriptor());

		script = Compiler.compile(iList, "stop", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof StopInstruction);
	}

	@Test
	public void testCreateInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var1", "message1"));
		iList.add(new CreateVarInstructionDescriptor("var2", "string"));

		script = Compiler.compile(iList, "create", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());

		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		CreateComplexVarInstruction comp = (CreateComplexVarInstruction) i;
		assertThat(comp.getName(), equalTo("var1"));
		assertThat(comp.getMapper(), equalTo(mapper1));

		i = i.next();
		assertTrue(i instanceof CreatePrimitiveVarInstruction);
		CreatePrimitiveVarInstruction prim = (CreatePrimitiveVarInstruction) i;
		assertThat(prim.getVariable(), equalTo("var2"));
		assertThat(prim.getType(), equalTo(DataType.STRING));
	}

	@Test
	public void testSetInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var1", "message1"));
		iList.add(new SetInstructionDescriptor("var1", "integer", "5"));
		iList.add(new CreateVarInstructionDescriptor("var2", "integer"));
		iList.add(new SetInstructionDescriptor("var2", "10"));

		script = Compiler.compile(iList, "set", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);

		i = i.next();
		assertTrue(i instanceof SetComplexInstruction);
		SetComplexInstruction comp = (SetComplexInstruction) i;
		assertThat(comp.getVariable(), equalTo("var1"));
		assertThat(comp.getField(), equalTo("integer"));
		assertThat(comp.getFieldType(), equalTo(Integer.class));
		assertThat(comp.getValue(), equalTo("5"));

		i = i.next();
		assertTrue(i instanceof CreatePrimitiveVarInstruction);
		i = i.next();
		assertTrue(i instanceof SetPrimitiveInstruction);
		SetPrimitiveInstruction prim = (SetPrimitiveInstruction) i;
		assertThat(prim.getVariable(), equalTo("var2"));
		assertThat(prim.getType(), equalTo(Integer.class));
		assertThat(prim.getValue(), equalTo("10"));
	}

	@Test
	public void testPutInstructionParse() throws Exception {
		Script script;
		Instruction i;
        PutInstruction put;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new SetInstructionDescriptor("var", "integer", "5"));
		iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
        iList.add(new PutInstructionDescriptor("${var.string}", "string"));

		script = Compiler.compile(iList, "put", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		i = i.next();
		assertTrue(i instanceof SetComplexInstruction);

		i = i.next();
		assertTrue(i instanceof PutInstruction);
		put = (PutInstruction) i;
		assertThat(put.getExpression(), equalTo("${var.integer}"));
		assertThat(put.getAttribute(), equalTo(integer));
        assertThat(put.getIndex(), equalTo(0));

        i = i.next();
        assertTrue(i instanceof PutInstruction);
        put = (PutInstruction) i;
        assertThat(put.getExpression(), equalTo("${var.string}"));
        assertThat(put.getAttribute(), equalTo(string));
        assertThat(put.getIndex(), equalTo(1));
	}

	@Test
	public void testEmitInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new SetInstructionDescriptor("var", "integer", "5"));
		iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
		iList.add(new EmitInstructionDescriptor());

		script = Compiler.compile(iList, "emit", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		i = i.next();
		assertTrue(i instanceof SetComplexInstruction);
		i = i.next();
		assertTrue(i instanceof PutInstruction);
		i = i.next();
		assertTrue(i instanceof EmitInstruction);
	}

	@Test
	public void testSubmitInstructionParse() throws Exception {
		Script script;
		Instruction i;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new SetInstructionDescriptor("var", "integer", "5"));
		iList.add(new SubmitInstructionDescriptor("request1", "loopback",
				"result", "message2"));

		script = Compiler.compile(iList, "emit", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		i = i.next();
		assertTrue(i instanceof SetComplexInstruction);
		i = i.next();
		assertTrue(i instanceof SubmitInstruction);
		SubmitInstruction submit = (SubmitInstruction) i;
		assertThat(submit.getBuilder(), equalTo(request1));
		assertThat(submit.getChannel(), equalTo(channel));
		assertThat(submit.getResultVar(), equalTo("result"));
		assertThat(submit.getResultMapper(), equalTo(mapper2));
	}

	@Test
	public void testIfInstructionParse() throws Exception {
		Script script;
		Instruction i;

		List<InstructionDescriptor> thenList = new ArrayList<>();
		thenList.add(new SetInstructionDescriptor("var", "integer", "0"));

		List<InstructionDescriptor> elseList = new ArrayList<>();
		elseList.add(new SetInstructionDescriptor("var", "integer", "1"));

		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new IfInstructionDescriptor("true", thenList, elseList));
		iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
		iList.add(new EmitInstructionDescriptor());

		script = Compiler.compile(iList, "if", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		i = script.getCode();
		assertTrue(i instanceof CreateComplexVarInstruction);
		i = i.next();
		assertTrue(i instanceof IfInstruction);
		IfInstruction ifInst = (IfInstruction) i;
		i = ifInst.getThenBlock();
		assertTrue(i instanceof SetComplexInstruction);
		SetComplexInstruction set = (SetComplexInstruction) i;
		assertThat(set.getVariable(), equalTo("var"));
		assertThat(set.getField(), equalTo("integer"));
		assertThat(set.getFieldType(), equalTo(Integer.class));
		assertThat(set.getValue(), equalTo("0"));
		i = ifInst.getElseBlock();
		assertTrue(i instanceof SetComplexInstruction);
		set = (SetComplexInstruction) i;
		assertThat(set.getVariable(), equalTo("var"));
		assertThat(set.getField(), equalTo("integer"));
		assertThat(set.getFieldType(), equalTo(Integer.class));
		assertThat(set.getValue(), equalTo("1"));
		i = ifInst.next();
		assertTrue(i instanceof PutInstruction);
		i = i.next();
		assertTrue(i instanceof EmitInstruction);
	}

	@Test
	public void testEmitSet() throws Exception {
		Script script;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new SetInstructionDescriptor("var", "integer", "5"));
		iList.add(new SetInstructionDescriptor("var", "string", "test"));
		iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
		iList.add(new PutInstructionDescriptor("${var.string}", "string"));
		iList.add(new EmitInstructionDescriptor());

		script = Compiler.compile(iList, "emit", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		assertFalse(script.getEmit().isEmpty());
		assertTrue(script.getEmit().contains(Attribute.create(integer)));
		assertTrue(script.getEmit().contains(Attribute.create(string)));
		assertFalse(script.getEmit().contains(Attribute.create(bool)));
		assertTrue(script.getSet().isEmpty());
	}

    @Test
    public void testAttIdx() throws Exception {
        Script script;
        List<InstructionDescriptor> iList = new ArrayList<>();
        iList.add(new CreateVarInstructionDescriptor("var", "message1"));
        iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
        iList.add(new PutInstructionDescriptor("${var.string}", "string"));
        iList.add(new EmitInstructionDescriptor());

        script = Compiler.compile(iList, "attidx", attributeMap, mapperMap,
                requestBuilderMap, channelMap);
        assertThat(script, notNullValue());
        assertFalse(script.getEmit().isEmpty());
        assertFalse(script.getIndexes().isEmpty());
        assertThat(script.getIndexes().size(), equalTo(2));

        Integer idx1 = script.getIndexes().get(Attribute.create(integer));
        assertThat(idx1, equalTo(0));

        Integer idx2 = script.getIndexes().get(Attribute.create(string));
        assertThat(idx2, equalTo(1));
    }

	@Test
	public void testSetSet() throws Exception {
		Script script;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new CreateVarInstructionDescriptor("var", "message1"));
		iList.add(new SetInstructionDescriptor("var", "integer",
				"${param['integer']}"));
		iList.add(new SetInstructionDescriptor("var", "string",
				"${param['string']}"));

		script = Compiler.compile(iList, "emit", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		assertTrue(script.getEmit().isEmpty());
		assertFalse(script.getSet().isEmpty());
		assertTrue(script.getSet().contains(Attribute.create(integer)));
		assertTrue(script.getSet().contains(Attribute.create(string)));
		assertFalse(script.getSet().contains(Attribute.create(bool)));
	}

	@Test
	public void testError() throws Exception {
		Script script;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new ErrorInstructionDescriptor("error"));

		script = Compiler.compile(iList, "error", attributeMap, mapperMap,
				requestBuilderMap, channelMap);
		assertThat(script, notNullValue());
		Instruction i = script.getCode();
		assertThat(i, notNullValue());
		assertTrue(i instanceof ErrorInstruction);
		ErrorInstruction err = (ErrorInstruction) i;
		assertThat(err.getMessage(), equalTo("error"));
	}

}
