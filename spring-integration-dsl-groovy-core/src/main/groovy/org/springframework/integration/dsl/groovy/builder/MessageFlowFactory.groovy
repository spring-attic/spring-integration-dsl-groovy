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

import org.springframework.integration.dsl.groovy.IntegrationContext
import org.springframework.integration.dsl.groovy.MessageFlow
/**
 * @author David Turanski
 *
 */
class MessageFlowFactory extends IntegrationComponentFactory {

	public Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes){
		def messageFlow = new MessageFlow(attributes)
		if (!messageFlow.inputChannel) {
			messageFlow.inputChannel = "${messageFlow.name}.inputChannel"
		}
		messageFlow.integrationContext = builder.integrationContext
		messageFlow
	}

	boolean isLeaf() {
		false
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object messageFlow ) {

		if (!parent){
			if (logger.isDebugEnabled()) {
				logger.debug("adding root message flow ${messageFlow.name} to integration context")
			}
			builder.integrationContext.add(messageFlow)
			if (builder.autoCreateApplicationContext) {
				builder.integrationContext.createApplicationContext(builder.parentContext)
			}
		} else {

			if (parent instanceof MessageFlow){
				if (logger.isDebugEnabled()) {
					logger.debug("creating nested message flow ${messageFlow.name} parent: ${parent.name}")
				}
			} else if (parent instanceof IntegrationContext) {
				if (logger.isDebugEnabled()) {
					logger.debug("adding message flow ${messageFlow.name} to integration context")
				}
			} else {
				throw new IllegalArgumentException('parent must be of type IntegrationContext or MessageFlow')
			}
			parent.add(messageFlow)
		}
	}
}