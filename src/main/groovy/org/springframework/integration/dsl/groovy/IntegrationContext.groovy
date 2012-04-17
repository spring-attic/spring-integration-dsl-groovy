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
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
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
import org.springframework.integration.dsl.groovy.bean.TransformerBeanBuilder
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

	private logger = LogFactory.getLog(this.getClass())
	private config
	private applicationContext


	def IntegrationContext() {
		if (logger.isDebugEnabled()) logger.debug("Creating new IntegrationContext")
	}

	def send(String inputChannelName, Object msgOrPayload) {
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		doSend(inputChannel,msgOrPayload)
	}


	def sendAndReceive(String inputChannelName,String outputChannelName, Object msgOrPayload) {
		final result
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		def outputChannel = ac.getBean(outputChannelName)


		if (outputChannel instanceof SubscribableChannel) {
			if (outputChannel instanceof PublishSubscribeChannel || !outputChannel.dispatcher.handlerCount ) {
				outputChannel.subscribe(new MessageHandler() {
							void handleMessage(Message msg) {
								result = msg.payload
							}
						})
			}
		}

		doSend(inputChannel,msgOrPayload)
		result
	}

	def getMessageFlows() {
		components.findAll{it instanceof MessageFlow}
	}


	private def doSend(MessageChannel inputChannel, Object msgOrPayload) {
		if (msgOrPayload instanceof Message) {
			inputChannel.send(msgOrPayload)
		} else {
			inputChannel.send(new GenericMessage(msgOrPayload))
		}
	}


	ApplicationContext createApplicationContext(ApplicationContext parentContext = null) {
		if (!applicationContext){
			applicationContext = new  GenericXmlApplicationContext()
			if (parentContext){
				applicationContext.parentContext = parentContext
			}

			def xmlBuilder = new StreamingMarkupBuilder()
			def integrationMarkupSupport = new IntegrationMarkupSupport()
			def writer = xmlBuilder.bind { builder->
				namespaces << IntegrationMarkupSupport.coreNamespaces()
				beans( integrationMarkupSupport.schemaLocations() ) {
					components.each {component ->
						if (component instanceof MessageFlow) {
							'si:channel'(id:component.inputChannel)
							'si:channel'(id:component.outputChannel)

							'si:chain'('input-channel':component.inputChannel,
									'output-channel':component.outputChannel) {
									component.components.each {comp->
										logger.debug("instantiating component " + comp)
										integrationMarkupSupport.createEndpoint(applicationContext,builder,comp)
									}
							}
						}
						else if (component instanceof SimpleEndpoint) {
							'si:channel'(id:component.inputChannel)
							if (component.outputChannel) {
								'si:channel'(id:component.outputChannel)
							}
							integrationMarkupSupport.createEndpoint(applicationContext,builder,component)
						}
					}
				}
			}

			//def stringWriter = new StringWriter()
			//writer.writeTo(stringWriter)

			def xml =  XmlUtil.serialize(writer)

			if (logger.isDebugEnabled()) {
				logger.debug(xml)
			}

			applicationContext.load(new ByteArrayResource(xml.getBytes() ))
			applicationContext.refresh()
		}
		applicationContext
	}
}

class IntegrationConfig extends BaseIntegrationComposition {
}
