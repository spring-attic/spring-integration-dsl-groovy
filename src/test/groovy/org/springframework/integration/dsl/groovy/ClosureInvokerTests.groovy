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
class ClosureInvokerTests extends GroovyObjectSupport {
	@Test
	void testClosureInvoker() {
		def closureInvoker = new ClosureInvoker({"hello"})
		assertEquals("hello",closureInvoker.invoke("hello"));
	}
	
	@Test
	void testTypeSafeInvocation() {
		try {
		def closureInvoker = new ClosureInvoker({String it-> println it})
        	closureInvoker.typeSafeInvoke(1)
			fail('should throw an exception')
		} catch (e){
		}
	}
	
	@Test
	void testSimpleMessageInvocation() {
		def messageClosureInvoker =  getSimpleMessageInvoker()
		Message message = new GenericMessage("hello")
		assertEquals("hello",messageClosureInvoker.transform(message).payload)
	}
	
	@Test
	void testSimpleMessagePayloadInvocation() {
		def messageClosureInvoker = getSimpleMessagePayloadInvoker()
		Message message = new GenericMessage("hello")
		assertEquals("HELLO",messageClosureInvoker.transform(message).payload)
	}
	
	@Test
	void testSimpleMessageHeadersInvocation() {
		def messageClosureInvoker = getSimpleMessageHeaderInvoker()
		Message message = MessageBuilder
		.withPayload("hello")
		.copyHeaders([foo:'foo',bar:'bar'])
		.build()
		assertEquals("foobar",messageClosureInvoker.transform(message).payload)
	}
	
	//These also invoked from a Java Test 
	
	static org.springframework.integration.transformer.Transformer getSimpleMessageInvoker() {
		new ClosureInvokingTransformer({Message m-> m.payload})
	} 
	
	static org.springframework.integration.transformer.Transformer getSimpleMessagePayloadInvoker() {
		new ClosureInvokingTransformer({payload-> payload.toUpperCase()})
	}
	
	static org.springframework.integration.transformer.Transformer getSimpleMessageHeaderInvoker() {
		new ClosureInvokingTransformer({Map headers-> headers.foo + headers.bar})
	}
	
}
