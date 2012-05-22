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

import static org.junit.Assert.*
import org.junit.Test
import org.springframework.integration.dsl.groovy.FlowExecution


/**
 * @author David Turanski
 *
 */
class MessageFlowTests {
	IntegrationBuilder builder = new IntegrationBuilder()

	@Test
	void testDefaultFlowValues() {
		builder.setAutoCreateApplicationContext(false)
		builder.messageFlow('input1')

		def messageFlow = builder.messageFlows[0]
		assert messageFlow.inputChannel == 'input1.inputChannel'
		assert messageFlow.name == 'input1'
	}


	@Test
	void testNamedFlows() {
		builder.setAutoCreateApplicationContext(false)
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
			filter {it == 'World'}
			transform {'Hello ' + it}
			handle {println "****************** $it ***************" }
		}

		flow.send('World')
		assert flow.sendAndReceive('World',1) == null
	}

	@Test
	void testMultipleFlows() {
		builder.setAutoCreateApplicationContext(false)
		def flow1 = builder.messageFlow('flow1',outputChannel:'outputChannel1') {
			transform {it.toUpperCase()}
		}
		def flow2 = builder.messageFlow('flow2',inputChannel:'outputChannel1') {
			filter {it.class == String}
			transform {it.toLowerCase()}
		}

		assert flow1.sendAndReceive('hello') == 'hello'

		def response = builder.integrationContext.sendAndReceive(flow1.inputChannel, 'hElLo')
		assert response == 'hello', response
	}

	@Test
	void testMultipleFlows2() {
		def flow1
		def flow2

		def ic = builder.doWithSpringIntegration {
			flow1 = messageFlow(outputChannel:'flow2inputChannel') {
				transform {it.toUpperCase()}
			}
			flow2 = messageFlow(inputChannel:'flow2inputChannel',outputChannel:'flow2outputChannel') {
				filter {it.class == String}
				transform {it.toLowerCase()}
			}
			handle(inputChannel:flow2.outputChannel,{println it})
		}

		ic.send(flow1.inputChannel,'hello')
	}

	@Test
	void testExecSubFlow() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow('sub'){
				filter {it.class == String}
				transform {it.toLowerCase()}
			}

			mainflow = messageFlow('main') { exec(subflow) }
		}

		assert mainflow.components.find{it instanceof FlowExecution}

		assert mainflow.sendAndReceive('Hello') == 'hello'
	}

	@Test
	void testEmbeddedSubFlow() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow ('sub'){
				filter('f', {it.class == String})
				transform('t1', {it.toLowerCase()})
			}

			mainflow = messageFlow ('main'){
				transform('t2',{it.toUpperCase()})
				exec(subflow)
				transform('t3',{"${it.toUpperCase()}${it.toLowerCase()}"})
			}
		}

		assert mainflow.sendAndReceive('Hello') == 'HELLOhello'
	}

	@Test
	void testEmbeddedSubFlowWithNamedChannel() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow = messageFlow ('sub',inputChannel:'sub.in', outputChannel:'sub.out'){
				filter('f',{it.class == String})
				transform('t1',{it.toLowerCase()})
			}

			mainflow = messageFlow ('main'){
				transform('t2',{it.toUpperCase()})
				exec(subflow)
				transform('t3',{"${it.toUpperCase()}${it.toLowerCase()}"})
			}
		}

		assert mainflow.sendAndReceive('Hello') == 'HELLOhello'
	}


	@Test
	void testChainedSubFlowsWithSomethingInBetween() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter {it.class == String}
				transform {it.toLowerCase()+'one'}
			}

			def subflow2 = messageFlow {
				transform {it.toUpperCase()+'two'}
			}

			mainflow = messageFlow ('main'){
				exec(subflow1)
				handle {it}
				exec(subflow2)
			}
		}
		assert mainflow.sendAndReceive('Hello') == 'HELLOONEtwo'
	}

	@Test
	void testSubFlowFirst() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter {it.class == String}
				transform {it.toLowerCase()+'one'}
			}

			mainflow = messageFlow ('main'){
				exec(subflow1)
				handle {it}
			}
		}

		assert mainflow.sendAndReceive('Hello') == 'helloone'
	}


	@Test
	void testChainedSubFlows() {
		def mainflow
		def ic = builder.doWithSpringIntegration {
			def subflow1 = messageFlow {
				filter {it.class == String}
				transform {it.toLowerCase()+'one'}
			}

			def subflow2 = messageFlow {
				transform {it.toUpperCase()+'two'}
			}

			mainflow = messageFlow ('main'){
				exec(subflow1)
				exec(subflow2)
			}
		}

		assert mainflow.sendAndReceive('Hello') == 'HELLOONEtwo'
	}

	@Test
	void testNestedFlows() {
		def flow = builder.messageFlow {
			handle {payload -> payload.toUpperCase()}
			messageFlow {
				transform {it*2}
				messageFlow {
					transform {payload->payload.toLowerCase()}
				}
			}
		}

		assert flow.sendAndReceive('Hello') == 'hellohello'
	}
}
