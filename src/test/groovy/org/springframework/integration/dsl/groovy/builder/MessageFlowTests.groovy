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
package org.springframework.integration.dsl.groovy.builder
import org.junit.Test
import static org.junit.Assert.*
/**
 * @author David Turanski
 *
 */
public class MessageFlowTests {
	IntegrationBuilder builder = new IntegrationBuilder()
	
	@Test 
	void testDefaultFlowValues() {
		builder.messageFlow('input1')
		
		def messageFlow = builder.messageFlows[0]
		assert messageFlow.inputChannel == 'input1#inputChannel'
		assert messageFlow.name == 'input1'
	}
	
	
	
	@Test
	void testNamedFlows() {
		builder.messageFlow(name:'flow1',inputChannel:'input1')
		builder.messageFlow('flow2',inputChannel:'input1')
		
		def messageFlow = builder.messageFlows[0]
		assert messageFlow.inputChannel == 'input1'
		assert messageFlow.name == 'flow1'
		
		messageFlow = builder.messageFlows.find {it.name == 'flow2'}
		assert messageFlow
	}
	
	@Test
	void testInvalidFlows() {
		try {
		  builder.messageFlow('flow1',name:'flow2')
		  fail('should throw an assertion error')
		} catch (Error e) {
		} 
	}
}
