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
package org.springframework.integration.dsl.groovy.amqp.builder.dom

import groovy.lang.Closure
import java.util.Map
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder
import org.springframework.integration.dsl.groovy.ClosureInvokingMessageProcessor
import org.springframework.integration.dsl.groovy.IntegrationComponent

/**
 * @author David Turanski
 *
 */
class AmqpOutboundDomBuilder extends IntegrationComponentDomBuilder {

	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder#doBuild(java.lang.Object, org.springframework.context.ApplicationContext, java.lang.Object, java.util.Map, groovy.lang.Closure)
	 */
	@Override
	protected void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent component, Closure closure) {
		def routingKey
		def beanName = "${component.name}_closureInvokingHandler"
		def attributes = component.attributes
		
		if (attributes.containsKey('routing-key')){
			routingKey = attributes['routing-key']
			if (routingKey instanceof Closure) {
				BeanDefinitionBuilder  handlerBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
				handlerBuilder.addConstructorArgValue(routingKey)
				def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
				BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)
				attributes.remove['routing-key']
				attributes['routing-key-expression'] = "@${beanName}.processMessage(#this)"
			}	
		}
		
		if (component.oneWay) {
			if (component.requestChannel){
				attributes.'channel' = component.requestChannel
			}
			builder.'int-amqp:outbound-channel-adapter'(attributes)
		} else {

			if (component.requestChannel){
				attributes.'request-channel' = component.requestChannel
			}

			if (component.replyChannel){
				attributes.'reply-channel' = component.replyChannel
			}
			builder.'int-amqp:outbound-gateway'(attributes)
		}
	}
}
