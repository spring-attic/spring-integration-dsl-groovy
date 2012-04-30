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

import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.FlowExecution
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.MessageProducingEndpoint
import org.springframework.integration.dsl.groovy.OtherwiseCondition
import org.springframework.integration.dsl.groovy.RouterComposition
import org.springframework.integration.dsl.groovy.RouterCondition
import org.springframework.integration.dsl.groovy.SimpleEndpoint
import org.springframework.integration.dsl.groovy.WhenCondition
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

/**
 * @author David Turanski
 *
 */
class RouterXmlBuilder extends IntegrationComponentXmlBuilder {
 
	
	RouterXmlBuilder(IntegrationMarkupSupport integrationMarkupSupport) {
		this.integrationMarkupSupport = integrationMarkupSupport
	}
	
   @Override 
   public void build(builder, ApplicationContext applicationContext, Object routerComposition, Closure closure) {
	   SimpleEndpointXmlBuilder endpointBuilder = integrationMarkupSupport.xmlBuilder(SimpleEndpoint.class.name)
		if (logger.isDebugEnabled()){
			logger.debug("Building router composition")
		}

		def otherwise = routerComposition.components.find{it instanceof OtherwiseCondition}
		if (otherwise){
			routerComposition."default-output-channel" =
					otherwise.components.first().inputChannel  = "${otherwise.name}.inputChannel"
		}
		endpointBuilder.build(builder, applicationContext, routerComposition) {
			routerComposition.channelMap?.each {value,channel ->
				"$siPrefix:mapping"(value:value, channel:channel)
			}

			routerComposition.components.each {component ->
				if (logger.isDebugEnabled()){
					logger.debug("building component $component")
				}

				if (component instanceof WhenCondition) {
					"$siPrefix:mapping"(value:component.value, channel:component.components.first().inputChannel)
				}
			}
		}

		routerComposition.components.findAll{it instanceof RouterCondition}.each {component ->
			integrationMarkupSupport.xmlBuilder(MessageFlow.class.name).build(builder,applicationContext,component)
		}
	}
}
