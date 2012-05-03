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
import org.junit.Test
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.channel.QueueChannel
import static org.junit.Assert.*
/**
 * @author David Turanski
 *
 */
 
public class ChannelInterceptorTests {
	IntegrationBuilder builder = new IntegrationBuilder()
	 
	@Test
	void testGlobalInterceptor() {
		def flow
	    builder.doWithSpringIntegration {
			interceptor(pattern:'*',preSend:{payload -> println "payload:$payload"; payload*2})
			flow = messageFlow {
				transform(evaluate:{it.toUpperCase()})
				transform(evaluate:{it.toLowerCase()})
				transform(evaluate:{it.capitalize()})
			}
		}
		
		def result = flow.sendAndReceive("hello")
		assert result == "Hellohellohellohellohellohellohellohello"

	}
	
	@Test
	void testChannelInterceptor() {
		def flow
		builder.doWithSpringIntegration {
			channel('inputChannel'){
				interceptor(preSend:{payload -> println "payload:$payload"; payload*2})
			}
			flow = messageFlow(inputChannel:'inputChannel') {
				transform(evaluate:{it.toUpperCase()})
				transform(evaluate:{it.toLowerCase()})
				transform(evaluate:{it.capitalize()})
			}
		}
		
		def result = flow.sendAndReceive("hello")
		assert result == "Hellohello"

	}
	
	@Test 
	void testWiretap() {
		def flow
		builder.doWithSpringIntegration {
			springXml {
				'si:logging-channel-adapter'(id:'logger',expression:"'----> ' + payload")
			}
			channel('inputChannel'){
				wiretap(channel:'logger')
			}
			flow = messageFlow(inputChannel:'inputChannel') {
				transform(evaluate:{it.toUpperCase()})
			}
		}
		
		def result = flow.sendAndReceive("hello")
	}
	
	@Test
	void testGlobalWiretap() {
		def flow
		def ic = builder.doWithSpringIntegration {
			
			wiretap(channel:'tapChannel',pattern:'input*')
			
			flow = messageFlow(inputChannel:'inputChannel') {
				transform(evaluate:{it.toUpperCase()})
			}
			
			queueChannel('tapChannel')
		}
		
		flow.sendAndReceive("hello")
		def tapChannel = ic.applicationContext.getBean('tapChannel')
		def msg = tapChannel.receive(100)
		assert msg.payload == "hello"	
	}
}	

