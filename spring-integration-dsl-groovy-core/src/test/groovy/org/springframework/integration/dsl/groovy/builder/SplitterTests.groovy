/*
 * Copyright 2002-2014 the original author or authors.
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

import org.springframework.messaging.support.GenericMessage
/**
 * @author David Turanski
 *
 */

class SplitterTests {

	IntegrationBuilder builder = new IntegrationBuilder()

	@Test
	void testDefaultSplitter() {
		def flow
		def ic = builder.doWithSpringIntegration {
			queueChannel('queueChannel')
			flow = messageFlow(outputChannel:'queueChannel') { split() }
		}

		def ac = ic.createApplicationContext()
		def queueChannel = ac.getBean('queueChannel')
		flow.send(['hello', 'world'])

		def result = queueChannel.receive()
		assert result.payload == 'hello'
		result = queueChannel.receive()
		assert result.payload == 'world'
	}

	@Test
	void testCustomSplitter() {
		def ic = builder.doWithSpringIntegration {
			queueChannel('queueChannel')
			split inputChannel:'splitChannel',{ it.split(',')}, outputChannel:'queueChannel'
		}

		def ac = ic.createApplicationContext()
		def queueChannel = ac.getBean('queueChannel')
		def inputChannel = ac.getBean('splitChannel')
		inputChannel.send(new GenericMessage('hello,world'))
		def result = queueChannel.receive()
		assert result.payload == 'hello'
		result = queueChannel.receive()
		assert result.payload == 'world'
	}
}
