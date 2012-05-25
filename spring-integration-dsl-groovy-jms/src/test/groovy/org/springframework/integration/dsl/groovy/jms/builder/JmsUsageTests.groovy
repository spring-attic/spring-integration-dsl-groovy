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

import java.util.concurrent.CountDownLatch

import javax.jms.Message
import javax.jms.Session
import org.junit.Test
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import java.util.concurrent.TimeUnit



/**
 * @author David Turanski
 *
 */
class JmsUsageTests {
	def builder = new IntegrationBuilder('jms')

	@Test
	//@Ignore
	public void testJmsListener() {
		def flow = builder.messageFlow {
			springXml {
				bean(id:'connectionFactory','class':'org.apache.activemq.ActiveMQConnectionFactory'){
					property(name:'brokerURL',value:'vm://localhost?broker.persistent=false&broker.useJmx=false')
				}
			}

			jmsListen destinationName:'myRequest',autoStartup:false
			transform {it.toUpperCase()}

		}

		def replyQ = new org.apache.activemq.command.ActiveMQQueue('myReply')
		def requestQ = new org.apache.activemq.command.ActiveMQQueue('myRequest')
		def connectionFactory = builder.applicationContext.getBean('connectionFactory')

		def jmsTemplate = new JmsTemplate(connectionFactory)

		flow.start()

		jmsTemplate.send(requestQ, new MessageCreator() {
					Message createMessage(Session session){
						def message = session.createTextMessage()
						message.setText('Hello from JMS')
						message.setJMSReplyTo(replyQ)
						message
					}
				})

		def msg = jmsTemplate.receive(replyQ)
		assert msg.text  == 'HELLO FROM JMS'

		flow.stop()
	}

	@Test
	void jmsOutboundGatewayWithReply() {
		builder.autoCreateApplicationContext= false
		def sendingMessageFlow
		def receivingMessageFlow
		def integrationContext = builder.doWithSpringIntegration {
			springXml {
				bean(id:'connectionFactory','class':'org.apache.activemq.ActiveMQConnectionFactory'){
					property(name:'brokerURL',value:'vm://localhost?broker.persistent=false&broker.useJmx=false')
				}
			}
			sendingMessageFlow = messageFlow {
				transform { it.toUpperCase() }
				jmsSendAndReceive(destinationName: 'myQueue')
			}

			receivingMessageFlow = messageFlow {
				jmsListen(destinationName:'myQueue')
				handle {println("received $it" ); "REPLY: $it"}
			}
		}

		integrationContext.createApplicationContext()

		def reply = sendingMessageFlow.sendAndReceive('Hello JMS!')
		assert reply == 'REPLY: HELLO JMS!'
	}

	@Test
	void jmsOutboundGatewayWithNoReply() {
		builder.autoCreateApplicationContext= false
		final int messageCount = 0
		final CountDownLatch cdl = new CountDownLatch(1)
		println cdl
		def sendingMessageFlow
		def receivingMessageFlow

		def integrationContext = builder.doWithSpringIntegration {
			springXml {
				bean(id:'connectionFactory','class':'org.apache.activemq.ActiveMQConnectionFactory'){
					property(name:'brokerURL',value:'vm://localhost?broker.persistent=false&broker.useJmx=false')
				}
			}
			sendingMessageFlow = messageFlow {
				transform { it.toUpperCase() }
				jmsSend(destinationName: 'myQueue1')
			}

			receivingMessageFlow = messageFlow {
				jmsListen destinationName:'myQueue1'
				handle { messageCount++; cdl.countDown(); return null}
			}
		}

		integrationContext.createApplicationContext()
		sendingMessageFlow.send('Hello JMS!')
		cdl.await(3, TimeUnit.SECONDS)
		assert messageCount == 1

	}

}