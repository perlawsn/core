package org.dei.perla.core.registry;

import org.dei.perla.core.engine.Attribute;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.descriptor.DataType;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class RegistryTest {

	private static final Attribute intAtt =
            Attribute.create("integer", DataType.INTEGER);
	private static final Attribute floatAtt =
            Attribute.create("float", DataType.FLOAT);
	private static final Attribute stringAtt =
            Attribute.create("string", DataType.STRING);
	private static final Attribute tempAtt =
            Attribute.create("temperature", DataType.INTEGER);
	private static final Attribute pressAtt =
            Attribute.create("pressure", DataType.FLOAT);

	@Test
	public void singleAddition() {
		Registry registry = new TreeRegistry();
		Collection<Fpc> result;
		Set<Attribute> withSet = new HashSet<>();
		Set<Attribute> withoutSet = new HashSet<>();

		Set<Attribute> attributeSet = new TreeSet<>();
		attributeSet.add(intAtt);
		attributeSet.add(floatAtt);
		attributeSet.add(stringAtt);
		attributeSet.add(tempAtt);
		Fpc fpc = new FakeFpc(attributeSet);

		registry.add(fpc);

		// All fpcs with 'integer'
		withSet.clear();
		withSet.add(intAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));

		// All fpcs with 'float'
		withSet.clear();
		withSet.add(pressAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));

		// All fpcs with 'integer' and 'temperature'
		withSet.clear();
		withSet.add(intAtt);
		withSet.add(tempAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));

		// All fpcs with 'integer' and 'pressure'
		withSet.clear();
		withSet.add(intAtt);
		withSet.add(pressAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));

		// All fpcs with 'integer' but without 'temperature'
		withSet.clear();
		withSet.add(intAtt);
		withoutSet.clear();
		withoutSet.add(tempAtt);
		result = registry.getByAttribute(withSet, withoutSet);
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));

		// All fpcs with 'integer' but without 'pressure'
		withSet.clear();
		withSet.add(intAtt);
		withoutSet.clear();
		withoutSet.add(pressAtt);
		result = registry.getByAttribute(withSet, withoutSet);
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
	}

	@Test
	public void multipleAdditions() {
		Registry registry = new TreeRegistry();
		Collection<Fpc> result;
		Set<Attribute> withSet = new HashSet<>();
		Set<Attribute> withoutSet = new HashSet<>();

		Set<Attribute> attributeSet = new TreeSet<>();
		attributeSet.add(intAtt);
		attributeSet.add(floatAtt);
		attributeSet.add(stringAtt);
		attributeSet.add(tempAtt);
		Fpc fpc1 = new FakeFpc(attributeSet);
		registry.add(fpc1);

		attributeSet.clear();
		attributeSet.add(intAtt);
		attributeSet.add(floatAtt);
		attributeSet.add(pressAtt);
		Fpc fpc2 = new FakeFpc(attributeSet);
		registry.add(fpc2);

		withSet.add(intAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(2));

		withSet.clear();
		withSet.add(stringAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		result.forEach(fpc -> assertThat(fpc, equalTo(fpc1)));

		withSet.clear();
		withoutSet.clear();
		withSet.add(intAtt);
		withoutSet.add(pressAtt);
		result = registry.getByAttribute(withSet, withoutSet);
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		result.forEach(fpc -> assertThat(fpc, equalTo(fpc1)));
	}

	@Test
	public void testRemove() {
		Registry registry = new TreeRegistry();
		Collection<Fpc> result;
		Set<Attribute> withSet = new HashSet<>();

		Set<Attribute> attributeSet = new TreeSet<>();
		attributeSet.add(intAtt);
		attributeSet.add(floatAtt);
		attributeSet.add(stringAtt);
		attributeSet.add(tempAtt);
		Fpc fpc1 = new FakeFpc(attributeSet);
		registry.add(fpc1);

		attributeSet.clear();
		attributeSet.add(intAtt);
		attributeSet.add(floatAtt);
		attributeSet.add(pressAtt);
		Fpc fpc2 = new FakeFpc(attributeSet);
		registry.add(fpc2);

		withSet.add(intAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(2));

		registry.remove(fpc1);

		withSet.add(intAtt);
		result = registry.getByAttribute(withSet, Collections.emptyList());
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
	}

}
