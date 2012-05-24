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
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.ClosureInvokingChannelInterceptor


/**
 * @author David Turanski
 *
 */
class ChannelInterceptorDomBuilder extends IntegrationComponentDomBuilder {

	ChannelInterceptorDomBuilder(IntegrationDomSupport integrationDomSupport) {
		this.integrationDomSupport = integrationDomSupport
	}

	/*
	 * Builds and registers a channel interceptor
	 */
	@Override
	public void doBuild(builder, ApplicationContext applicationContext, Object channelInterceptor, Map attributes, Closure closure) {

		def beanName = "${channelInterceptor.name}_closureInvokingChannelInterceptor"

		BeanDefinitionBuilder  handlerBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingChannelInterceptor)
		handlerBuilder.addConstructorArgValue(channelInterceptor)
		def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
		BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext)

		channelInterceptor.ref=beanName
		if (channelInterceptor.global){
			attributes.remove('id')
			builder."$siPrefix:channel-interceptor"(attributes)
		} else {
			builder.ref(bean:beanName)
		}
	}
}
