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
package org.springframework.integration.dsl.groovy
import java.lang.IllegalStateException

import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.dsl.groovy.bean.TransformerBeanBuilder
import org.springframework.integration.support.MessageBuilder
import org.springframework.integration.Message
import org.springframework.integration.MessageChannel
import org.springframework.util.CollectionUtils
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.util.StringUtils
import org.springframework.integration.MessagingException

/**
 * @author David Turanski
 *
 */
class IntegrationContext {

	private logger = LogFactory.getLog(this.getClass())
	private messageFlows = []
	private config

	def IntegrationContext() {
		if (logger.isDebugEnabled()) logger.debug("Creating new IntegrationContext")
	}

	ApplicationContext createApplicationContext(ApplicationContext parentContext = null) {
		def applicationContext = new GenericApplicationContext()
	
		messageFlows.each {messageFlow ->
			messageFlow.components.each {component->
					logger.debug("instantiating component " + component)
					buildBeanForComponent(applicationContext,component)
			}
		}	
		applicationContext.refresh()
		applicationContext
	}
	
	
	private buildBeanForComponent(applicationContext,component) {
		if (component instanceof Transformer) {
			new TransformerBeanBuilder().build(applicationContext, component)
		}
		else if (component instanceof SimpleEndpoint){
			logger.debug("building a simple endpoint $component")
			if (!component.inputChannel){
				logger.debug("need to wire an input channel")
			} 
		} else {
			logger.debug("building a bean for type ${component.class}")
		}
	}
}

class IntegrationConfig extends BaseIntegrationComposition {
	
}
