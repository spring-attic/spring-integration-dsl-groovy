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

import java.lang.IllegalStateException

import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.GenericXmlApplicationContext
import org.springframework.core.io.ByteArrayResource
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.core.MessageHandler
import org.springframework.integration.core.SubscribableChannel
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationMarkupSupport
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.Message
import org.springframework.integration.MessageChannel
import org.springframework.util.CollectionUtils
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.util.StringUtils
import org.springframework.integration.MessagingException

/**
 * @author David Turanski
 *
 */
class IntegrationContext extends BaseIntegrationComposition {

	private logger = LogFactory.getLog(IntegrationContext)
	private applicationContext

	def send(String inputChannelName, Object msgOrPayload) {
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		doSend(inputChannel,msgOrPayload)
	}
	//TODO: Add errorFlow
	def sendAndReceive(String inputChannelName, Object msgOrPayload, long timeout = 0) {
	   def result
	   def ac = createApplicationContext()
	   def inputChannel = ac.getBean(inputChannelName)
	   def replyChannel = new QueueChannel()
	   def messageToSend = MessageBuilder.withPayload(msgOrPayload).setReplyChannel(replyChannel).build()
	   inputChannel.send(messageToSend)
	   if (timeout){
		   result = replyChannel.receive(timeout)
	   } else {
	   	   result = replyChannel.receive()
	   }
	   
	   return (msgOrPayload instanceof Message) ? result : result?.payload   
	} 
	
	def getMessageFlows() {
		components.findAll{it instanceof MessageFlow}
	}

	ApplicationContext createApplicationContext(ApplicationContext parentContext = null) {
		if (!applicationContext){
			applicationContext = new  GenericXmlApplicationContext()
			if (parentContext){
				applicationContext.parentContext = parentContext
			}
			
			def integrationMarkupSupport = new IntegrationMarkupSupport()

		    def xml = integrationMarkupSupport.translateToXML(this)

			applicationContext.load(new ByteArrayResource(xml.getBytes() ))
			applicationContext.refresh()
		}
		applicationContext
	}
	
	private def doSend(MessageChannel inputChannel, Object msgOrPayload) {
		if (msgOrPayload instanceof Message) {
			inputChannel.send(msgOrPayload)
		} else {
			inputChannel.send(new GenericMessage(msgOrPayload))
		}
	}
	
}


class IntegrationConfig extends BaseIntegrationComposition {
}
