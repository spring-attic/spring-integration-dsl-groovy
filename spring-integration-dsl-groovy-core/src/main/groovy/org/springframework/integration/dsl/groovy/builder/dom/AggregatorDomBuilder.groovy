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
class AggregatorDomBuilder extends IntegrationComponentDomBuilder {

	AggregatorDomBuilder(IntegrationDomSupport integrationDomSupport){
		this.integrationDomSupport = integrationDomSupport
	}

	@Override
	public void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent endpoint, Closure closure) {

		def name = endpoint.name
		assert endpoint.name, "name cannot be null for object $endpoint"

        //Fix for https://jira.springsource.org/browse/INTDSLGROOVY-11
		/*if (endpoint.hasProperty("outputChannel") && endpoint.outputChannel ) {
			channelBuilder.createDirectChannelIfNotDefined(builder,endpoint.outputChannel)
		}*/

		def attributes = buildAttributes(endpoint.attributes, endpoint)
		
		if (endpoint.hasProperty('action') && endpoint.action) {
			assert !(attributes.containsKey('ref')), 'endoint cannot provide a bean reference and a closure'
			attributes.method='processList'
			def beanName = "${name}_closureInvokingHandler"
			attributes.ref = beanName

			BeanDefinitionBuilder  handlerBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingListProcessor)
			handlerBuilder.addConstructorArgValue(endpoint.action)
			def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
		}

		if (endpoint.releaseStrategy){
			assert !(attributes.containsKey('release-strategy')), 'endoint cannot provide a release-strategy reference and a closure'
			attributes.'release-strategy-method'='canRelease'
			def beanName = "${endpoint.name}#releaseStrategyHandler"
			attributes.'release-strategy' = beanName


			BeanDefinitionBuilder  handlerBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingReleaseStrategy)
			handlerBuilder.addConstructorArgValue(endpoint.releaseStrategy)
			def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
		}
		if (endpoint.correlationStrategy){
			assert !(attributes.containsKey('correlation-strategy')), 'endoint cannot provide a correlation-strategy reference and a closure'
			attributes.'correlation-strategy-method'='processMessage'
			def beanName = "${endpoint.name}#correlationStrategyHandler"
			attributes.'correlation-strategy' = beanName


			BeanDefinitionBuilder  handlerBuilder =
					BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
			handlerBuilder.addConstructorArgValue(endpoint.correlationStrategy)
			def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
		}

		buildEndpoint(builder,endpoint,attributes,"aggregator")
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
			if (closure)closure.call()
		}
	}

	private  buildAttributes(attributes, endpoint) {
		if (endpoint.hasProperty('inputChannel')) {
			attributes.'input-channel' = endpoint.inputChannel
		}
		if (endpoint.hasProperty('outputChannel') && endpoint.outputChannel) {
			attributes.'output-channel' = endpoint.outputChannel
		}
		attributes
	}
}
