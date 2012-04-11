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
package org.springframework.integration.dsl.groovy.builder

import groovy.util.AbstractFactory
import groovy.util.FactoryBuilderSupport
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.integration.dsl.groovy.MessageFlow
/**
 * @author David Turanski
 *
 */
class MessageFlowFactory extends AbstractFactory {
	private static Log logger = LogFactory.getLog(MessageFlowFactory.class)
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	def newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		
		assert !(attributes.containsKey('name') && value), "messageFlow cannot accept both a default value and a 'name' attribute" 
		
		if (logger.isDebugEnabled()) {
			logger.debug("creating new MessageFlow $value")
		}
		
		attributes = attributes ?: [:]
		
		if (!attributes.containsKey('name') && value){
			attributes.name = value
		}
		if (!attributes.containsKey('inputChannel') && value){
			attributes.inputChannel = "${value}#inputChannel"
		}
		

		new MessageFlow(attributes)
	
	}

	boolean isLeaf() {
		false
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object messageFlow ) {
		
		//Validate inputChannel
		
		if (!messageFlow.inputChannel) {
			assert messageFlow.components[0].hasProperty('inputChannel') && messageFlow.components[0].inputChannel, 
			"Either the messageFlow or its initial component ${messageFlow.components[0]} must provide an 'inputChannel' attribute"   
			messageFlow.inputChannel = messageFlow.components[0].inputChannel
		}
		
		
		
		if (parent == null) {
			builder.integrationContext.messageFlows << messageFlow
		} else {
			assert parent instanceof MessageFlow
		
			parent.add(messageFlow)
			
			logger.debug("creating nested message flow ${messageFlow.name} parent: ${parent.name}")
		}
		
	}
}