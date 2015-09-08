package org.dei.perla.core.engine;

import org.dei.perla.core.channel.Channel;
import org.dei.perla.core.channel.IORequestBuilder;
import org.dei.perla.core.channel.loopback.LoopbackChannel;
import org.dei.perla.core.channel.loopback.LoopbackIORequestBuilder;
import org.dei.perla.core.channel.loopback.TestFieldDescriptor;
import org.dei.perla.core.channel.loopback.TestMapper;
import org.dei.perla.core.descriptor.AttributeDescriptor;
import org.dei.perla.core.descriptor.AttributeDescriptor.AttributePermission;
import org.dei.perla.core.descriptor.FieldDescriptor.FieldQualifier;
import org.dei.perla.core.descriptor.InvalidDeviceDescriptorException;
import org.dei.perla.core.descriptor.instructions.*;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.core.message.Mapper;
import org.dei.perla.core.sample.Attribute;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class CompilerTest {

    private static final AttributeDescriptor intDesc =
            new AttributeDescriptor("integer", DataType.INTEGER.getId(),
                AttributePermission.READ_WRITE);
    private static final AttributeDescriptor stringDesc =
            new AttributeDescriptor("string", DataType.STRING.getId(),
                AttributePermission.READ_WRITE);
    private static AttributeDescriptor boolDesc =
            new AttributeDescriptor("bool", DataType.BOOLEAN.getId(),
                AttributePermission.READ_WRITE);

    private final static Map<String, AttributeDescriptor> attDescMap;
    static {
        Map<String, AttributeDescriptor> m = new HashMap<>();
        m.put(intDesc.getId(), intDesc);
        m.put(stringDesc.getId(), stringDesc);
        m.put(boolDesc.getId(), boolDesc);
        attDescMap = Collections.unmodifiableMap(m);
    }

    private final static Attribute intAtt =
            Attribute.create("integer", DataType.INTEGER);
    private final static Attribute stringAtt =
            Attribute.create("string", DataType.STRING);
    private final static Attribute boolAtt =
            Attribute.create("bool", DataType.BOOLEAN);
    private final static Map<String, Attribute> attMap;
    static {
        Map<String, Attribute> m = new HashMap<>();
        m.put("integer", intAtt);
        m.put("string", stringAtt);
        m.put("bool", boolAtt);
        attMap = Collections.unmodifiableMap(m);
    }

    private static final TestFieldDescriptor intField =
            new TestFieldDescriptor("integer", FieldQualifier.FIELD,
                "integer", null, null, null);
    private static final TestFieldDescriptor stringField =
            new TestFieldDescriptor("string", FieldQualifier.FIELD,
                "string", null, null, null);
    private static final TestFieldDescriptor listField =
            new TestFieldDescriptor("list", FieldQualifier.LIST,
                "string", null, null, null);

    private static final TestMapper mapper1;
    private static final TestMapper mapper2;
    private static final Map<String, Mapper> mapperMap;
    static {
        mapper1 = new TestMapper("message1");
        mapper1.addField(intField);
        mapper1.addField(stringField);
        mapper2 = new TestMapper("message2");
        mapper2.addField(intField);
        mapper2.addField(stringField);
        mapper2.addField(listField);

        Map<String, Mapper> m = new HashMap<>();
        m.put(mapper1.getMessageId(), mapper1);
        m.put(mapper2.getMessageId(), mapper2);
        mapperMap = Collections.unmodifiableMap(m);
    }

    private final static IORequestBuilder request1;
    private final static Map<String, IORequestBuilder> reqBldMap;
    static {
        request1 = new LoopbackIORequestBuilder("request1");
        Map<String, IORequestBuilder> m = new HashMap<>();
        m.put(request1.getRequestId(), request1);
        reqBldMap = Collections.unmodifiableMap(m);
    }

    private static final Channel channel;
    private static final Map<String, Channel> channelMap;
    static {
        channel = new LoopbackChannel();
        Map<String, Channel> m = new HashMap<>();
        m.put("loopback", channel);
        channelMap = Collections.unmodifiableMap(m);
    }

    @Test
    public void testAppendInstructionParse() throws Exception {
        Script script;
        Instruction i;
        List<InstructionDescriptor> iList = new ArrayList<>();
        iList.add(new CreateVarInstructionDescriptor("var", "message2"));
        iList.add(new AppendInstructionDescriptor("var", "list", "5"));

        script = Compiler.compile(iList, "append", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "breakpoint", attDescMap,
                attMap, mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "stop", attDescMap,
                attMap, mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "create", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
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

    @Test(expected = InvalidDeviceDescriptorException.class)
    public void testCreateTimestampParse() throws Exception {
        Instruction i;
        List<InstructionDescriptor> iList = new ArrayList<>();
        iList.add(new CreateVarInstructionDescriptor("var1", "timestamp"));

        Compiler.compile(iList, "create", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "set", attDescMap, attMap, mapperMap,
                reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "put", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
        assertThat(script, notNullValue());
        i = script.getCode();
        assertTrue(i instanceof CreateComplexVarInstruction);
        i = i.next();
        assertTrue(i instanceof SetComplexInstruction);

        i = i.next();
        assertTrue(i instanceof PutInstruction);
        put = (PutInstruction) i;
        assertThat(put.getExpression(), equalTo("${var.integer}"));
        assertThat(put.getType(), equalTo(Integer.class));
        assertThat(put.getIndex(), equalTo(0));

        i = i.next();
        assertTrue(i instanceof PutInstruction);
        put = (PutInstruction) i;
        assertThat(put.getExpression(), equalTo("${var.string}"));
        assertThat(put.getType(), equalTo(String.class));
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

        script = Compiler.compile(iList, "emit", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "emit", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "if", attDescMap, attMap, mapperMap,
                reqBldMap, channelMap);
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

        script = Compiler.compile(iList, "emit", attDescMap, attMap, mapperMap,
                reqBldMap, channelMap);
        assertThat(script, notNullValue());
        assertFalse(script.getEmit().isEmpty());
        assertTrue(script.getEmit().contains(intAtt));
        assertTrue(script.getEmit().contains(stringAtt));
        assertFalse(script.getEmit().contains(boolAtt));
        assertTrue(script.getSet().isEmpty());
    }

    @Test
    public void testAttIdx() throws Exception {
        Script script;
        List<InstructionDescriptor> iList = new ArrayList<>();
        iList.add(new CreateVarInstructionDescriptor("var", "message1"));
        iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
        iList.add(new PutInstructionDescriptor("${var.integer}", "integer"));
        iList.add(new PutInstructionDescriptor("${var.string}", "string"));
        iList.add(new EmitInstructionDescriptor());

        script = Compiler.compile(iList, "attidx", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
        assertThat(script, notNullValue());
        assertFalse(script.getEmit().isEmpty());
        assertThat(script.getEmit().size(), equalTo(2));

        Integer idx1 = script.getEmit().indexOf(intAtt);
        assertThat(idx1, equalTo(0));

        Integer idx2 = script.getEmit().indexOf(stringAtt);
        assertThat(idx2, equalTo(1));

        Instruction in = script.getCode();
        assertThat(in, notNullValue());
        assertTrue(in instanceof CreateComplexVarInstruction);

        in = in.next();
        assertThat(in, notNullValue());
        assertTrue(in instanceof PutInstruction);
        PutInstruction put = (PutInstruction) in;
        assertThat(put.getIndex(), equalTo(0));

        in = in.next();
        assertThat(in, notNullValue());
        assertTrue(in instanceof PutInstruction);
        put = (PutInstruction) in;
        assertThat(put.getIndex(), equalTo(0));

        in = in.next();
        assertThat(in, notNullValue());
        assertTrue(in instanceof PutInstruction);
        put = (PutInstruction) in;
        assertThat(put.getIndex(), equalTo(1));
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

        script = Compiler.compile(iList, "emit", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
        assertThat(script, notNullValue());
        assertTrue(script.getEmit().isEmpty());
        assertFalse(script.getSet().isEmpty());
        assertTrue(script.getSet().contains(intAtt));
        assertTrue(script.getSet().contains(stringAtt));
        assertFalse(script.getSet().contains(boolAtt));
    }

    @Test
    public void testError() throws Exception {
        Script script;
        List<InstructionDescriptor> iList = new ArrayList<>();
        iList.add(new ErrorInstructionDescriptor("error"));

        script = Compiler.compile(iList, "error", attDescMap, attMap,
                mapperMap, reqBldMap, channelMap);
        assertThat(script, notNullValue());
        Instruction i = script.getCode();
        assertThat(i, notNullValue());
        assertTrue(i instanceof ErrorInstruction);
        ErrorInstruction err = (ErrorInstruction) i;
        assertThat(err.getMessage(), equalTo("error"));
    }

    @Test
    public void testUnsupportedPeriod() throws Exception {
        Script script;
		List<InstructionDescriptor> iList = new ArrayList<>();
		iList.add(new UnsupportedPeriodInstructionDescriptor("suggested"));

		script = Compiler.compile(iList, "unsupported", attDescMap,
				attMap, mapperMap, reqBldMap, channelMap);
		assertThat(script, notNullValue());
		Instruction i = script.getCode();
		assertThat(i, notNullValue());
		assertTrue(i instanceof UnsupportedPeriodInstruction);
		UnsupportedPeriodInstruction uns = (UnsupportedPeriodInstruction) i;
		assertThat(uns.getSuggestedExpr(), equalTo("suggested"));
    }

}
