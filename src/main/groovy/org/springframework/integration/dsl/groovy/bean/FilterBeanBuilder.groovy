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
package org.springframework.integration.dsl.groovy.bean

import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.dsl.groovy.ClosureInvokingTransformer;
import org.springframework.integration.dsl.groovy.Transformer;
import org.springframework.integration.transformer.MessageTransformingHandler;

/**
 * @author David Turanski
 *
 */
class FilterBeanBuilder {
	def build(BeanDefinitionRegistry registry, Transformer transformer) {


		//TODO: Handle default and explicit channel creation
		BeanDefinitionBuilder inputChannelDef = BeanDefinitionBuilder.genericBeanDefinition(
				org.springframework.integration.channel.DirectChannel);
		BeanDefinitionHolder holder = new BeanDefinitionHolder(inputChannelDef.beanDefinition, "inputChannel");
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);

		BeanDefinitionBuilder outputChannelDef = BeanDefinitionBuilder.genericBeanDefinition(
				org.springframework.integration.channel.DirectChannel);
		holder = new BeanDefinitionHolder(outputChannelDef.beanDefinition, "outputChannel");
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);

		BeanDefinitionBuilder  consumerEndpointBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(org.springframework.integration.config.ConsumerEndpointFactoryBean)
		consumerEndpointBuilder.addPropertyReference("inputChannel","inputChannel")


		ClosureInvokingTransformer ciTransformer = new ClosureInvokingTransformer(transformer.action)

		BeanDefinitionBuilder  handlerBuilder =
				BeanDefinitionBuilder.genericBeanDefinition(org.springframework.integration.transformer.MessageTransformingHandler)
		
		handlerBuilder.addConstructorArgValue(ciTransformer)
		handlerBuilder.addPropertyReference("outputChannel", "outputChannel")
		
		String handlerName = BeanDefinitionReaderUtils.registerWithGeneratedName(handlerBuilder.beanDefinition,registry)
		 
		consumerEndpointBuilder.addPropertyReference("handler",handlerName)
		
		holder = new BeanDefinitionHolder(consumerEndpointBuilder.beanDefinition, transformer.name);
		
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry)
	}
}
