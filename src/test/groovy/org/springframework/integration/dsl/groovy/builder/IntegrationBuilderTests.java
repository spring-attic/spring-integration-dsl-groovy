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

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.Test;
import org.springframework.integration.dsl.groovy.MessageFlow;

/**
 * @author David Turanski
 *
 */
public class IntegrationBuilderTests {
    IntegrationBuilder builder = new IntegrationBuilder();
    
    @Test 
    public void test() throws CompilationFailedException, InstantiationException, IllegalAccessException {
    	String flowScript1 = "messageFlow(outputChannel:'outputChannel1') {transform(evaluate:{it.toUpperCase()})}";
     
    	MessageFlow flow1 = (MessageFlow)builder.build(new ByteArrayInputStream(flowScript1.getBytes()));
    	
    	String flowScript2 = ("messageFlow(inputChannel:'outputChannel1'){ filter(evaluate:{it.class == String})\ntransform(evaluate:{it.toLowerCase()})}");
    		
    	builder.build(new ByteArrayInputStream(flowScript2.getBytes()));
    	
		Object response = builder.getIntegrationContext().sendAndReceive(flow1.getInputChannel(),"hello");
		
		assertEquals("hello",response);
		
    }
}
