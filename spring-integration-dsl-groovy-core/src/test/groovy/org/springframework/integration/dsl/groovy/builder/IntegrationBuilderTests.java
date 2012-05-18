/*
 * Copyright 2002-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.dsl.groovy.builder;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;

import org.springframework.integration.dsl.groovy.IntegrationContext;
import org.springframework.integration.dsl.groovy.MessageFlow;

/**
 * @author David Turanski
 *
 */
public class IntegrationBuilderTests {
	IntegrationBuilder builder = new IntegrationBuilder();

	@Test
	public void testInvokeFlowFromJava() throws CompilationFailedException, InstantiationException,
			IllegalAccessException {
		builder.setAutoCreateApplicationContext(false);
		String flowScript1 = "messageFlow(outputChannel:'outputChannel1') {transform({it.toUpperCase()})}";

		MessageFlow flow1 = (MessageFlow) builder.build(new ByteArrayInputStream(flowScript1.getBytes()));

		String flowScript2 = ("messageFlow(inputChannel:'outputChannel1'){ filter({it.class == String})\ntransform({it.toLowerCase()})}");

		builder.build(new ByteArrayInputStream(flowScript2.getBytes()));

		Object response = builder.getIntegrationContext().sendAndReceive(flow1.getInputChannel(), "hello");

		assertEquals("hello", response);
	}

	@Test
	public void testInvokeFlowFromScript() throws IOException {
		IntegrationContext ic = (IntegrationContext) builder.build(new FileInputStream(
				"src/test/resources/messageflow1.groovy"));
		MessageFlow flow1 = ic.getMessageFlowByName("flow1");
		flow1.send("hello");
	}

	@Test(expected = ClassNotFoundException.class)
	public void testWithInitializers() {
		new IntegrationBuilder(new String[] { "foo" });
	}
}
