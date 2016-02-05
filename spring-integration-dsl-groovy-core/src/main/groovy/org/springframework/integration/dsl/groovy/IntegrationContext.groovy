/*
 * Copyright 2002-2016 the original author or authors.
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

import groovy.transform.PackageScope

import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericXmlApplicationContext
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.ByteArrayResource
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationDomSupport
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage

/**
 * @author David Turanski
 *
 */
class IntegrationContext extends BaseIntegrationComposition {
	private Properties properties
	private ApplicationContext applicationContext
	@PackageScope List<AbstractIntegrationBuilderModuleSupport> moduleSupportInstances

	/**
	 * Send a message without expecting a reply
	 *
	 * @param inputChannelName
	 * @param msgOrPayload either a Message or an Object used as a Message payload
	 * @return true if the send succeeded
	 */
	def send(String inputChannelName, Object msgOrPayload) {
		def ac = createApplicationContext()
		def inputChannel = ac.getBean(inputChannelName)
		def messageToSend = (msgOrPayload instanceof Message) ? msgOrPayload :
				new GenericMessage(msgOrPayload)
		inputChannel.send(messageToSend)
	}

	/**
	 * Send a message and receive a reply
	 * @param inputChannelName
	 * @param msgOrPayload either a Message or an Object used as a Message payload
	 * @return the result. If the input object is a Message, also returns a Message
	 * otherwise the payload is returned
	 */
	//TODO: Add error routing
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

	Message<?> receive(String channelName, long timeout = 0) {

		def ac = createApplicationContext()
		def channel = ac.getBean(channelName)

		Message<?> msg
		if (timeout){
			msg = channel.receive(timeout)
		} else {
			msg = channel.receive()
		}

		msg
	}


	/**
	 *
	 * @return all defined MessageFlows
	 */
	List<MessageFlow> getMessageFlows() {
		components.findAll{it instanceof MessageFlow}
	}

	/**
	 * Find a messageFlow by its name
	 * @param name the messageFlow name
	 * @return the MessageFlow
	 */
	MessageFlow getMessageFlowByName(String name){
		messageFlows.find {it.name == name}
	}

	/**
	 * Get the application context
	 * @return
	 */
	ApplicationContext getApplicationContext() {
		this.applicationContext
	}

	void setProperties(Properties properties) {
		this.properties = properties
	}
	/**
	 * Create an application context
	 * @param parentContext optional parent application context
	 * @param integrationContextsToMerge a list of {@link IntegrationContext} from other builder instances to merge.
	 * @return the application context
	 */
	ApplicationContext createApplicationContext(ApplicationContext parentContext = null,
		List<IntegrationContext> integrationContextsToMerge = null) {
		if (!applicationContext){
			applicationContext = new  GenericXmlApplicationContext()
			if (parentContext){
				applicationContext.parent = parentContext
			}

			def integrationDomSupport = new IntegrationDomSupport(this.moduleSupportInstances)

			List<IntegrationContext> integrationContexts = [this];
			if (integrationContextsToMerge) {
				integrationContexts.addAll(integrationContextsToMerge)
			}

			ByteArrayResource[] resources = new ByteArrayResource[integrationContexts.size()]

			integrationContexts.eachWithIndex {ic, i ->
			  ic.applicationContext = applicationContext
			  def xml = integrationDomSupport.translateToXML(ic)
			  resources[i] = new ByteArrayResource(xml.getBytes())
			}

			applicationContext.load(resources)

			boolean propertyConfigurerPresent =
			this.applicationContext.getBeanDefinitionNames().find {name->
				name.startsWith("org.springframework.context.support.PropertySourcesPlaceholderConfigurer")
			}
			if (!propertyConfigurerPresent) {
				PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer()
				placeholderConfigurer.setEnvironment(applicationContext.environment)
				this.applicationContext.addBeanFactoryPostProcessor(placeholderConfigurer)
			}

			if ( properties ) {
				MutablePropertySources propertySources = this.applicationContext.environment.propertySources
				propertySources.addFirst(new PropertiesPropertySource("properties",properties))
			}
			applicationContext.refresh()
		}
		applicationContext
	}

	/**
	 * Create an application context
	 * @param integrationContextsToMerge a list of {@link IntegrationContext} from other builder instances to merge.
	 * @return the application context
	 */
	ApplicationContext createApplicationContext(List<IntegrationContext> integrationContextsToMerge) {
		this.createApplicationContext(null, integrationContextsToMerge)
	}
}
