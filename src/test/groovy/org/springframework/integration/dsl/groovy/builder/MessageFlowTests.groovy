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
import org.springframework.integration.dsl.groovy.FlowExecution
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
		assert messageFlow.inputChannel == 'input1.inputChannel'
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
	
	@Test 
	void testSimpleChain() {
		def flow = builder.messageFlow(inputChannel:'inputChannel') {
			filter(evaluate:{it == "World"})
			transform(evaluate:{"Hello " + it})
			handle(evaluate:{println "****************** $it ***************" })
		}
		
		flow.send("World")
		assert flow.sendAndReceive("World",1) == null
		
	}
	
	@Test
	void testMultipleFlows() {
		def flow1 = builder.messageFlow('flow1',outputChannel:'outputChannel1') {
			transform(evaluate:{it.toUpperCase()})
		}
		def flow2 = builder.messageFlow('flow2',inputChannel:'outputChannel1') {
			filter(evaluate:{it.class == String})
			transform(evaluate:{it.toLowerCase()})
		}
		
		assert flow1.sendAndReceive("hello") == "hello"
		
		def response = builder.integrationContext.sendAndReceive(flow1.inputChannel, "hElLo")
		assert response == "hello", response
		
	}
	
	@Test
	void testMultipleFlows2() {
		def flow1
		def flow2
		
		def ic = builder.doWithSpringIntegration {
			flow1 = messageFlow(outputChannel:'flow2inputChannel') {
				transform(evaluate:{it.toUpperCase()})
			}
			flow2 = messageFlow(inputChannel:'flow2inputChannel',outputChannel:'flow2outputChannel') {
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()})
			}
			handle(inputChannel:flow2.outputChannel,evaluate:{println it}) 		
		}	
		
		ic.send(flow1.inputChannel,"hello")
		
	}
	
	@Test
	void testSubFlow() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow('sub'){
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()})
			}
			
			mainflow = messageFlow('main') {
				exec(subflow)
			}
		}
		
		assert mainflow.components.find{it instanceof FlowExecution}
		
		assert mainflow.sendAndReceive("Hello") == "hello"
		
	}
	
	@Test
	void testEmbeddedSubFlow() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow ('sub'){
				filter('f',evaluate:{it.class == String})
				transform('t1', evaluate:{it.toLowerCase()})
			}
			
			mainflow = messageFlow ('main'){
				transform('t2',evaluate:{it.toUpperCase()})
				exec(subflow)
				transform('t3',evaluate:{"${it.toUpperCase()}${it.toLowerCase()}"})
			}
		}
		
		assert mainflow.sendAndReceive("Hello") == "HELLOhello"
		
	}
	
	@Test
	void testEmbeddedSubFlowWithNamedChannel() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow ('sub',inputChannel:'sub.in', outputChannel:'sub.out'){
				filter('f',evaluate:{it.class == String})
				transform('t1', evaluate:{it.toLowerCase()})
			}
			
			mainflow = messageFlow ('main'){
				transform('t2',evaluate:{it.toUpperCase()})
				exec(subflow)
				transform('t3',evaluate:{"${it.toUpperCase()}${it.toLowerCase()}"})
			}
		}
		
		assert mainflow.sendAndReceive("Hello") == "HELLOhello"	
	}
	
	
	@Test
	void testChainedSubFlowsWithSomethingInBetween() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()+"one"})
			}
			
			def subflow2 = messageFlow {
				transform(evaluate:{it.toUpperCase()+"two"})
			}
			
			mainflow = messageFlow ('main'){
				exec(subflow1)
				handle(action:{it})
				exec(subflow2)
			}
		}
		assert mainflow.sendAndReceive("Hello") == "HELLOONEtwo"
	}
	
	@Test
	void testSubFlowFirst() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()+"one"})
			}
			
			mainflow = messageFlow ('main'){
				exec(subflow1)
				handle(action:{it})
			}
		}
		
		assert mainflow.sendAndReceive("Hello") == "helloone"		
	}
	
	
	@Test
	void testChainedSubFlows() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()+"one"})
			}
			
			def subflow2 = messageFlow {
				transform(evaluate:{it.toUpperCase()+"two"})
			}
			
			mainflow = messageFlow ('main'){ 
				exec(subflow1)
				exec(subflow2)
			}
		}
		
		assert mainflow.sendAndReceive("Hello") == "HELLOONEtwo"		
	}
}
