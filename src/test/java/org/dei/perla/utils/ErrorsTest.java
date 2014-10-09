package org.dei.perla.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ErrorsTest {

	@Test
	public void creationTest() {
		Errors errors = new Errors();
		
		assertThat(errors, notNullValue());
		assertTrue(errors.isEmpty());
		assertThat(errors.getErrorCount(), equalTo(0));
	}
	
	@Test
	public void creationWithContextTest() {
		Errors errors = new Errors("test source");
		
		assertThat(errors, notNullValue());
		assertTrue(errors.isEmpty());
		assertThat(errors.getErrorCount(), equalTo(0));
	}
	
	@Test
	public void errorAdditionTest() {
		Errors errors = new Errors();
		
		errors.addError("Test error 1");
		assertFalse(errors.isEmpty());
		assertThat(errors.getErrorCount(), equalTo(1));
		
		errors.addError("Test error 2");
		assertThat(errors.getErrorCount(), equalTo(2));
		
		for (int i = 0; i < 500; i++) {
			errors.addError("Test error %d", i);
		}
		assertThat(errors.getErrorCount(), equalTo(502));
	}
	
	@Test
	public void throwableAdditionTest() {
		Errors errors = new Errors();
		
		for (int i = 0; i < 500; i++) {
			errors.addError(new RuntimeException());
		}
		assertThat(errors.getErrorCount(), equalTo(500));
	}
	
	@Test
	public void errorInContextTest() {
		Errors main = new Errors();
		
		for (int i = 0; i < 500; i++) {
			main.addError("Test error %d", i);
		}
		assertThat(main.getErrorCount(), equalTo(500));
		
		Errors sub1 = main.inContext("sub1");
		for (int i = 0; i < 500; i++) {
			sub1.addError("Test error %d", i);
		}
		assertThat(main.getErrorCount(), equalTo(1000));
		assertThat(sub1.getErrorCount(), equalTo(500));
		
		Errors sub2 = main.inContext("sub2");
		for (int i = 0; i < 500; i++) {
			sub2.addError("Test error %d", i);
		}
		assertThat(main.getErrorCount(), equalTo(1500));
		assertThat(sub1.getErrorCount(), equalTo(500));
		assertThat(sub2.getErrorCount(), equalTo(500));
		
		Errors sub21 = sub2.inContext("sub21");
		for (int i = 0; i < 500; i++) {
			sub21.addError("Test error %d", i);
		}
		assertThat(main.getErrorCount(), equalTo(2000));
		assertThat(sub1.getErrorCount(), equalTo(500));
		assertThat(sub2.getErrorCount(), equalTo(1000));
		assertThat(sub21.getErrorCount(), equalTo(500));
	}
	
}
