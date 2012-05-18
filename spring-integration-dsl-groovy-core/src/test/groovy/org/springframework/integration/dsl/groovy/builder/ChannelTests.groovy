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
 
public class ChannelTests {
	IntegrationBuilder builder = new IntegrationBuilder()
	 
	@Test
	void testDirectChannel() {
		 def ic = builder.doWithSpringIntegration {
			 channel('direct1')
			 channel('direct2')
		 }
		 
		 def ac = ic.createApplicationContext()
		 def channel = ac.getBean('direct1')
		 assert (channel instanceof DirectChannel)
		 assert channel.componentName == 'direct1'
	}
	
	@Test
	void testPubSubChannel() {
		def ic = builder.doWithSpringIntegration {
			pubSubChannel('pubsub1')
		}
		def ac = ic.createApplicationContext()
		def channel = ac.getBean('pubsub1')
		assert (channel instanceof PublishSubscribeChannel)
		assert channel.componentName == 'pubsub1' 
	}
	
	@Test
	void testQueueChannel() {
		def ic = builder.doWithSpringIntegration {
			queueChannel('queue1')
			queueChannel('queue2',capacity:10)
		}
		def ac = ic.createApplicationContext()
		def channel = ac.getBean('queue1')
		assert (channel instanceof QueueChannel)
		assert channel.componentName == 'queue1'
		
		QueueChannel queue2 = ac.getBean('queue2')
		assert (queue2 instanceof QueueChannel)
		assert queue2.componentName == 'queue2'
		assert queue2.remainingCapacity == 10
	}
	
	@Test
	void testChannelAlreadyDefined() {
		def ic = builder.doWithSpringIntegration {
			pubSubChannel('flow1.outputChannel')
			messageFlow('flow1',outputChannel:'flow1.outputChannel') {
			 transform {payload}
			} 
		}
		
		def ac = ic.createApplicationContext()
		def channel = ac.getBean('flow1.outputChannel')
		assert channel instanceof PublishSubscribeChannel
	}
	
	@Test
	void testChannelOverride() {
		def ic = builder.doWithSpringIntegration {
			
			messageFlow('flow1',outputChannel:'flow1.outputChannel') {
			 pubSubChannel('flow1.outputChannel')
			 transform {payload}
			}
		}
		
		def ac = ic.createApplicationContext()
		def channel = ac.getBean('flow1.outputChannel')
		assert channel instanceof PublishSubscribeChannel
	}
	
}
