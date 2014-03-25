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
package org.springframework.integration.dsl.groovy

import static org.junit.Assert.*

import org.junit.Test

import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage
/**
 * @author David Turanski
 *
 */
class MultiMessageParameterTransformerTests {
	MultiMessageParameterTransformer multiMessageParameterTransformer
	def messageList = [
		new GenericMessage('1'),
		new GenericMessage('2')
	]
	@Test
	void testMapClosureArgListOfMessages() {
		def closure = { List<Message> messages -> messages}
		multiMessageParameterTransformer = new MultiMessageParameterTransformer(closure)
		def arg = multiMessageParameterTransformer.mapClosureArg(messageList)
		assertSame(messageList, arg)
	}

	@Test
	void testMapClosureArgMessageCollection() {
		def closure = { Collection<Message> messages -> messages}
		multiMessageParameterTransformer = new MultiMessageParameterTransformer(closure)
		def arg = multiMessageParameterTransformer.mapClosureArg(messageList)
		assertSame(messageList, arg)
	}

	@Test
	void testMapClosureArgUnParameterizedCollection() {
		def closure = { Collection items -> items}
		multiMessageParameterTransformer = new MultiMessageParameterTransformer(closure)
		def arg = multiMessageParameterTransformer.mapClosureArg(messageList)
		assert arg == ['1', '2']
		closure.call(arg)
	}

	@Test
	void testMapClosureArgToMessageArray() {
		def closure = {Message[] messages ->
			(1..messages.length).each { i-> assert messages[i-1].payload as int == i}
		}


		multiMessageParameterTransformer = new MultiMessageParameterTransformer(closure)
		def arg = multiMessageParameterTransformer.mapClosureArg(messageList)
		assert arg instanceof Message[]
		assert arg == messageList as Message[]
		closure.call(arg)
	}
}
