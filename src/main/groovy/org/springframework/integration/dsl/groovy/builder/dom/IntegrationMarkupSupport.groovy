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

import groovy.xml.StreamingMarkupBuilder
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.support.BeanDefinitionRegistry
 
import org.springframework.integration.dsl.groovy.*

/**
 * @author David Turanski
 *
 */
class IntegrationMarkupSupport {
	def schemaLocations = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd "+
		"http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd"
	
	static coreNamespaces() {
		['': 'http://www.springframework.org/schema/beans',
		 xsi: 'http://www.w3.org/2001/XMLSchema-instance',
		 si: 'http://www.springframework.org/schema/integration'
		]
	}
	
	def addIntegrationNamespace(prefix) {
		def namespace = "http://www.springframework.org/schema/integration/$prefix"
		addSchemaLocation(namespace,namespace+"/spring-integration-${prefix}.xsd")
		[prefix:namespace]
	}
	
	def schemaLocations() {
		['xsi:schemaLocation':schemaLocations]	    
	}
	
    def addSchemaLocation(namespace, xsd) {
		schemaLocations += " $namespace $xsd"
	}
	
	def createEndpoint(applicationContext, builder, SimpleEndpoint endpoint) {
		def name = endpoint.name
		assert endpoint.name, 'name cannot be null'
	
		
		def attributes = buildAttributes(endpoint)
		if (endpoint.action) {
			 assert !(attributes.containsKey('ref')), 'endoint cannot provide a bean reference and a closure'
			 attributes.method='processMessage'
			 def beanName = "${name}#handler"
			 attributes.ref = beanName
			 BeanDefinitionBuilder  handlerBuilder =
			 BeanDefinitionBuilder.genericBeanDefinition(ClosureInvokingMessageProcessor)
			 handlerBuilder.addConstructorArgValue(endpoint.action)
			 def bdh = new BeanDefinitionHolder(handlerBuilder.getBeanDefinition(),beanName)
			 BeanDefinitionReaderUtils.registerBeanDefinition(bdh, (BeanDefinitionRegistry) applicationContext);
		}
		if (endpoint.class == Transformer) {
			builder.'si:transformer'(attributes)
		}
		else if (endpoint.class == Filter) {
			builder.'si:filter'(attributes)
		}
		else if (endpoint.class == ServiceActivator) {
			builder.'si:service-activator'(attributes)
		}
		
	}	
	
	def buildAttributes(SimpleEndpoint endpoint) {
		def attributes = endpoint.componentProperties
		
		if (endpoint.inputChannel) {
			attributes.'input-channel' = endpoint.inputChannel
		}
		if (endpoint.outputChannel) {
			attributes.'output-channel' = endpoint.outputChannel
		}
		
		if (endpoint.properties.ref) {
			attributes.ref = endpoint.ref
		}
		
		if (endpoint.properties.method) {
			attributes.method = endpoint.method
		}
		
		attributes
	}
	
}
