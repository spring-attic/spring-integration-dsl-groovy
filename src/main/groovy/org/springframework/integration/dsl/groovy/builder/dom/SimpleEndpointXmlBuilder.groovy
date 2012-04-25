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
package org.springframework.integration.dsl.groovy.builder.dom
 
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext

import org.springframework.integration.dsl.groovy.*


/**
 * @author David Turanski
 *
 */
class SimpleEndpointXmlBuilder extends IntegrationComponentXmlBuilder {
	ChannelXmlBuilder channelBuilder
	
	SimpleEndpointXmlBuilder(IntegrationMarkupSupport integrationMarkupSupport){
		this.integrationMarkupSupport = integrationMarkupSupport
	}
	
	@Override
	public void build(Object builder, ApplicationContext applicationContext, Object endpoint, Closure closure) {
		
		ChannelXmlBuilder channelBuilder = integrationMarkupSupport.xmlBuilder(new Channel())
		def name = endpoint.name
		assert endpoint.name, 'name cannot be null'

		if (closure) {
			closure.delegate = builder
		}
		
		channelBuilder.createDirectChannelIfNotDefined(builder, endpoint.inputChannel)
		
		if (endpoint.hasProperty("outputChannel") && endpoint.outputChannel ) {
			 channelBuilder.createDirectChannelIfNotDefined(builder,endpoint.outputChannel)
		}

		def attributes = buildAttributes(endpoint)
		if (endpoint.hasProperty('action')) {
			assert !(attributes.containsKey('ref')), 'endoint cannot provide a bean reference and a closure'
			attributes.method='processMessage'
			def beanName = "${name}#closureInvokingHandler"
			attributes.ref = beanName

			BeanDefinitionBuilder  handlerBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
			handlerBuilder.addConstructorArgValue(endpoint.action)
			def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
		}
		
		if (endpoint.class == Transformer) {
			buildEndpoint(builder,endpoint,attributes,"transformer")
		}
		else if (endpoint.class == Filter) {
			buildEndpoint(builder,endpoint,attributes,"filter")
		}
		else if (endpoint.class == ServiceActivator) {
			buildEndpoint(builder,endpoint,attributes,"service-activator")
		}
		else if (endpoint.class == Bridge) {
			buildEndpoint(builder,endpoint,attributes,"bridge")
		}
		else if (endpoint.class == RouterComposition) {
			buildEndpoint(builder,endpoint,attributes,"router",closure)
		}
	}
	
	private buildEndpoint(builder,endpoint, attributes, methodName ,closure = null ) {
		builder."$siPrefix:$methodName"(attributes) {
			if (endpoint.poller) {
				if (endpoint.poller instanceof Poller) {
					"$siPrefix:poller"(endpoint.poller.componentProperties)
				} else if (endpoint.poller instanceof String) {
					"$siPrefix:poller"(ref:endpoint.poller)
				}
			}
			if (closure)closure.call()
		}
	}

	private  buildAttributes(endpoint) {
		def attributes = endpoint.componentProperties

		if (endpoint.hasProperty('inputChannel')) {
			attributes.'input-channel' = endpoint.inputChannel
		}
		if (endpoint.hasProperty('outputChannel') && endpoint.outputChannel) {
			attributes.'output-channel' = endpoint.outputChannel
		}

		if (endpoint.hasProperty('ref')) {
			attributes.ref = endpoint.ref
		}

		if (endpoint.hasProperty('method')) {
			attributes.method = endpoint.method
		}

		attributes.id = endpoint.name

		attributes
	}

	def createBridge(builder,inputChannel,outputChannel){
		builder."$siPrefix:bridge"('input-channel':inputChannel,'output-channel':outputChannel)
	}
}
