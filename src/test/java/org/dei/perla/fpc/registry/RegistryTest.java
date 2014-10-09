package org.dei.perla.fpc.registry;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.dei.perla.fpc.Attribute;
import org.dei.perla.fpc.Fpc;
import org.dei.perla.fpc.descriptor.DataType;
import org.junit.Test;

public class RegistryTest {

	private static final Attribute intAtt = new Attribute("integer",
			DataType.INTEGER);
	private static final Attribute floatAtt = new Attribute("float",
			DataType.FLOAT);
	private static final Attribute stringAtt = new Attribute("string",
			DataType.STRING);
	private static final Attribute tempAtt = new Attribute("temperature",
			DataType.INTEGER);
	private static final Attribute pressAtt = new Attribute("pressure",
			DataType.FLOAT);
	
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
