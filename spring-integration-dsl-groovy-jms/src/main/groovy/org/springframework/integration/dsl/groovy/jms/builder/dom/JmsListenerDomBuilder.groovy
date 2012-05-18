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
package org.springframework.integration.dsl.groovy.jms.builder.dom

import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder
import org.springframework.integration.dsl.groovy.ClosureInvokingMessageProcessor

/**
 * @author David Turanski
 *
 */
class JmsListenerDomBuilder extends IntegrationComponentDomBuilder {

	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder#build(java.lang.Object, org.springframework.context.ApplicationContext, java.lang.Object, groovy.lang.Closure)
	 */
	@Override
	public void build(Object builder, ApplicationContext applicationContext, Object component, Closure closure) {
		
		def attributes = component.componentProperties
		attributes.'connection-factory' = component.connectionFactory?:'connectionFactory'
		
		if (component.destinationName){
			attributes.'destination-name' = component.destinationName
		}
		
		builder.'int-jms:message-driven-channel-adapter'(attributes)  
	 
	}
}
