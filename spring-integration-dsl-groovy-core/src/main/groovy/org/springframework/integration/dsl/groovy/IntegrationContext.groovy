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
package org.springframework.integration.dsl.groovy

import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericXmlApplicationContext
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationDomSupport
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.Message
import org.springframework.core.io.ByteArrayResource

/**
 * @author David Turanski
 *
 */
class IntegrationContext extends BaseIntegrationComposition {

	private applicationContext
	private List<AbstractIntegrationBuilderModuleSupport> moduleSupportInstances

	def send(String inputChannelName, Object msgOrPayload) {
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		def messageToSend = (msgOrPayload instanceof Message) ? msgOrPayload :
				new GenericMessage(msgOrPayload)
		inputChannel.send(messageToSend)
	}
	//TODO: Add errorFlow
	def sendAndReceive(String inputChannelName, Object msgOrPayload, long timeout = 0) {
		def result
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		def replyChannel = new QueueChannel()

		def messageToSend = (msgOrPayload instanceof Message) ?
				MessageBuilder.fromMessage(msgOrPayload).setReplyChannel(replyChannel).build() :
				MessageBuilder.withPayload(msgOrPayload).setReplyChannel(replyChannel).build()
		inputChannel.send(messageToSend)

		if (timeout){
			result = replyChannel.receive(timeout)
		} else {
			result = replyChannel.receive()
		}

		(msgOrPayload instanceof Message) ? result : result?.payload
	}

	List<MessageFlow> getMessageFlows() {
		components.findAll{it instanceof MessageFlow}
	}

	MessageFlow getMessageFlowByName(String name){
		messageFlows.find {it.name = name}
	}

	ApplicationContext getApplicationContext() {
		this.applicationContext
	}


	ApplicationContext createApplicationContext(ApplicationContext parentContext = null) {
		if (!applicationContext){
			applicationContext = new  GenericXmlApplicationContext()
			if (parentContext){
				applicationContext.parentContext = parentContext
			}

			def integrationDomSupport = new IntegrationDomSupport(this.moduleSupportInstances)

			def xml = integrationDomSupport.translateToXML(this)

			applicationContext.load(new ByteArrayResource(xml.getBytes() ))
			applicationContext.refresh()
		}
		applicationContext
	}
}