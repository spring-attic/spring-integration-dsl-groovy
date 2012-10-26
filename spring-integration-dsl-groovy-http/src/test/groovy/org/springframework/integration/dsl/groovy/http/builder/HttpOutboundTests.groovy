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
package org.springframework.integration.dsl.groovy.http.builder

import org.springframework.integration.Message
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder;
import org.springframework.integration.message.GenericMessage
import org.junit.Test
import org.junit.Ignore
/**
 * @author David Turanski
 *
 */
class HttpOutboundTests {
	IntegrationBuilder builder = new IntegrationBuilder('http')
	
	@Test
	void testSimpleHttpOutboundGateway() {
		builder.doWithSpringIntegration {
			channel('httpRequestChannel')
			channel('httpReplyChannel')
			httpGet(requestChannel:'httpRequestChannel',replyChannel:'httpReplyChannel',url:'http://www.foo.com')
		}
	}
	
	@Test
	@Ignore
	void testHttpOutboundInMessageFlow() {
		def flow = builder.messageFlow {
			httpGet(url:'http://www.google.com/finance/info?q=VMW',responseType:String)
		}		
		def result = flow.sendAndReceive('')
		println result 
	}
	
	@Test
	@Ignore
	void testHttpOutboundWithClosure() {
		def flow = builder.messageFlow {
			httpGet(url:{"http://www.google.com/finance/info?q=$it"},responseType:String)
		}
		def result = flow.sendAndReceive('vmw')
		println result
	}
}
