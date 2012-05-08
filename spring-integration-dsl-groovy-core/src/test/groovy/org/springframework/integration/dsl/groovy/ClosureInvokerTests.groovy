package org.springframework.integration.dsl.groovy;
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
import static org.junit.Assert.*

import org.junit.Test
import org.springframework.integration.Message
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.handler.MessageProcessor
import org.springframework.integration.message.GenericMessage
 
/**
 * @author David Turanski
 *
 */
class ClosureInvokerTests {
	
	@Test
	void testSimpleMessageInvocation() {
		def messageClosureInvoker =  getSimpleMessageInvoker()
		Message message = new GenericMessage("hello")
		assertEquals("hello",messageClosureInvoker.processMessage(message))
	}
	
	@Test
	void testSimpleMessagePayloadInvocation() {
		def messageClosureInvoker = getSimpleMessagePayloadInvoker()
		Message message = new GenericMessage("hello")
		assertEquals("HELLO",messageClosureInvoker.processMessage(message))
	}
	
	@Test
	void testSimpleMessageHeadersInvocation() {
		def messageClosureInvoker = getSimpleMessageHeaderInvoker()
		Message message = MessageBuilder
		.withPayload("hello")
		.copyHeaders([foo:'foo',bar:'bar'])
		.build()
		assertEquals("foobar",messageClosureInvoker.processMessage(message))
	}
	
	//These also invoked from a Java Test 
	
	static ClosureInvokingMessageProcessor getSimpleMessageInvoker() {
		new ClosureInvokingMessageProcessor({Message m-> m.payload})
	} 
	
	static ClosureInvokingMessageProcessor getSimpleMessagePayloadInvoker() {
		new ClosureInvokingMessageProcessor({payload-> payload.toUpperCase()})
	}
	
	static ClosureInvokingMessageProcessor getSimpleMessageHeaderInvoker() {
		new ClosureInvokingMessageProcessor({Map headers-> headers.foo + headers.bar})
	}
	
}
