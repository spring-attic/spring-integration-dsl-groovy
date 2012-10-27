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
package org.springframework.integration.dsl.groovy.http.builder.dom

import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.ClosureInvokingMessageProcessor
import org.springframework.integration.dsl.groovy.IntegrationComponent
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder

/**
 * @author David Turanski
 *
 */
class HttpOutboundDomBuilder extends IntegrationComponentDomBuilder {

	@Override
	void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent component, Closure closure) {

		def attributes = component.attributes
		
		def httpMethod = 'GET'
		if (component.builderName == 'httpPut') {
			httpMethod = 'PUT'
		}

		if (component.requestChannel && !attributes.containsKey('request-channel')){
			attributes.'request-channel' = component.requestChannel
		}

		if (component.replyChannel && !attributes.containsKey('reply-channel')){
			attributes.'reply-channel' = component.replyChannel
		}

		component.'http-method' = httpMethod

		if (component.responseType) {
			attributes.'expected-response-type' =component.responseType.name
		}

		Closure urlExpression

		attributes.url = component.url
		if (component.url instanceof Closure) {
			urlExpression = component.url
			attributes.url = '{url}'
		}


		builder.'int-http:outbound-gateway'(attributes) {
			if (urlExpression){
				def beanName = "${component.name}_closureInvokingHandler"
				BeanDefinitionBuilder  handlerBuilder =
						BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
				handlerBuilder.addConstructorArgValue(urlExpression)
				def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
				BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)

				'int-http:uri-variable'(name:'url', expression:"@${beanName}.processMessage(#this)")
			}
		}
	}
}
