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
package org.springframework.integration.dsl.groovy.jms.builder

import javax.jms.Session
import javax.jms.Message
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
import org.junit.Test
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.apache.activemq.command.ActiveMQQueue

/**
 * @author David Turanski
 *
 */
class JmsListenerTests {
	def builder = new IntegrationBuilder("jms")
	
	@Test
	public void testJmsListener() {
		builder.doWithSpringIntegration {
			springXml {
				bean(id:'connectionFactory','class':'org.apache.activemq.ActiveMQConnectionFactory'){
					property(name:'brokerURL',value:'vm://localhost')
				}
				bean(id:'request','class':'org.apache.activemq.command.ActiveMQQueue') {
					'constructor-arg'(value:'myQueue')
				} 
			}
			
			queueChannel('outputChannel')
			jmsListen(channel:'inputChannel',destinationName:'myQueue')
		    transform(inputChannel:'inputChannel',outputChannel:'outputChannel',action:{it.toUpperCase()})
			
		}
	
		def connectionFactory = builder.applicationContext.getBean('connectionFactory')
		def request = builder.applicationContext.getBean('request')
		def outputChannel =  builder.applicationContext.getBean('outputChannel')
		
		def jmsTemplate = new JmsTemplate(connectionFactory)
		
		jmsTemplate.send(request, new MessageCreator() {
			Message createMessage(Session session){
				def message = session.createTextMessage()
				message.setText("Hello from JMS")
				message
			}
		})
		
		def msg = outputChannel.receive(100)
		assert msg?.payload == "HELLO FROM JMS"
		
	}
	
 
}
