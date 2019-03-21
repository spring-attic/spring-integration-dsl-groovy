/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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
class SimpleEndpointDomBuilder extends IntegrationComponentDomBuilder {

	SimpleEndpointDomBuilder(IntegrationDomSupport integrationDomSupport){
		this.integrationDomSupport = integrationDomSupport
	}

	@Override
	void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent endpoint, Closure closure) {
        def name = endpoint.name
        assert endpoint.name, "name cannot be null for object $endpoint"

        def attributes = endpoint.attributes

        //Fix for https://jira.springsource.org/browse/INTDSLGROOVY-11
        if (endpoint.hasProperty("outputChannel") && endpoint.outputChannel?.startsWith('from.') ) {
            ChannelDomBuilder channelBuilder = integrationDomSupport.domBuilder(new Channel())
            channelBuilder.createDirectChannelIfNotDefined(builder,endpoint.outputChannel)
        }

        attributes = buildAttributes(attributes, endpoint)
		if (endpoint.hasProperty('action') && endpoint.action) {
			assert !(attributes.containsKey('ref')), 'endpoint cannot provide a bean reference and a closure'
			attributes.method='processMessage'
			def beanName = "${name}_closureInvokingHandler"
			attributes.ref = beanName

			BeanDefinitionBuilder  handlerBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
			handlerBuilder.addConstructorArgValue(endpoint.action)
			def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
		}

		if (endpoint instanceof Transformer) {
			buildEndpoint(builder,endpoint,attributes,'transformer')
		}
		else if (endpoint instanceof Filter) {
			buildEndpoint(builder,endpoint,attributes,'filter')
		}
		else if (endpoint instanceof ServiceActivator) {
			buildEndpoint(builder,endpoint,attributes,'service-activator')
		}
		else if (endpoint instanceof Bridge) {
			buildEndpoint(builder,endpoint,attributes, 'bridge')
		}
		else if (endpoint instanceof Splitter) {
			buildEndpoint(builder,endpoint,attributes,'splitter')
		}
		else if (endpoint instanceof RouterComposition) {
			if (closure) {
				closure.delegate = builder
			}
			buildEndpoint(builder,endpoint,attributes,'router',closure)
		}
	}

	private buildEndpoint(builder,endpoint, attributes, methodName ,closure = null ) {
		builder."$siPrefix:$methodName"(attributes) {
			if (endpoint.poller) {
				if (endpoint.poller instanceof Poller) {
					"$siPrefix:poller"(endpoint.poller.attributes)
				} else if (endpoint.poller instanceof String) {
					"$siPrefix:poller"(ref:endpoint.poller)
				}
			}
			if (closure) {
				closure.call()
			}
		}
	}

	private  buildAttributes(attributes,endpoint) {

		if (endpoint.hasProperty('inputChannel')) {
			attributes.'input-channel' = endpoint.inputChannel
		}
		if (endpoint.hasProperty('outputChannel') && endpoint.outputChannel) {
			attributes.'output-channel' = endpoint.outputChannel
		}

		attributes
	}

	def createBridge(builder,inputChannel,outputChannel){
		builder."$siPrefix:bridge"('input-channel':inputChannel,'output-channel':outputChannel)
	}
}
