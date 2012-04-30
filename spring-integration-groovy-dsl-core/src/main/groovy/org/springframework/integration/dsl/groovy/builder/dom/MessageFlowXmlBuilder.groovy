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
import org.springframework.integration.dsl.groovy.Channel
import org.springframework.integration.dsl.groovy.FlowExecution
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.MessageProducingEndpoint
import org.springframework.integration.dsl.groovy.RouterComposition
import org.springframework.integration.dsl.groovy.SimpleEndpoint
import org.springframework.integration.dsl.groovy.AbstractChannel
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

/**
 * @author David Turanski
 *
 */
class MessageFlowXmlBuilder extends IntegrationComponentXmlBuilder {
	ChannelXmlBuilder channelBuilder
	SimpleEndpointXmlBuilder endpointBuilder
	
	MessageFlowXmlBuilder(IntegrationMarkupSupport integrationMarkupSupport) {
		this.integrationMarkupSupport = integrationMarkupSupport
	}
	
   @Override 
   public void build(builder, ApplicationContext applicationContext, Object messageFlow, Closure closure) {
	   SimpleEndpointXmlBuilder endpointBuilder = integrationMarkupSupport.xmlBuilder(SimpleEndpoint.class.name)
	   ChannelXmlBuilder channelBuilder = integrationMarkupSupport.xmlBuilder(AbstractChannel.class.name)
	   
		def previousComponent = null
		if (messageFlow.outputChannel) {
			channelBuilder.createDirectChannelIfNotDefined(builder, messageFlow.outputChannel)
		}
		messageFlow.components.each {component ->
			if (component instanceof FlowExecution ) {
				if (!previousComponent) {
					endpointBuilder.createBridge(builder,messageFlow.inputChannel, component.inputChannel)
				}
			}
			else if (component instanceof MessageFlow) {
				resolveMessageFlowChannels(component)
				build(builder, applicationContext, component)
			} else {
				integrationMarkupSupport.xmlBuilder(component).build(builder,applicationContext, component)
			}

			previousComponent = component
		}
	}

	def resolveMessageFlowChannels(messageFlow) {

		def first = messageFlow.components.first()
		first.inputChannel = first.inputChannel ?: messageFlow.inputChannel

		def last = messageFlow.components.last()

		if (last instanceof MessageProducingEndpoint){
			last.outputChannel = last.outputChannel ?: messageFlow.outputChannel
		}

		def outputChannel

		messageFlow.components.eachWithIndex {component, i->

			if (component instanceof SimpleEndpoint ) {
				component.inputChannel = component.inputChannel ?: outputChannel


				if (component instanceof MessageProducingEndpoint ) {
					if (component != last && component.linkToNext) {
						component.outputChannel = outputChannel = component.outputChannel ?: channelName(component,messageFlow.components[i+1])
						//If component is Flow execution in the midst of a messageFlow, it requires an outputChannel
						if (component instanceof FlowExecution ) {
							def c = component.messageFlow.components.last()
							component.messageFlow.components.last().outputChannel = outputChannel
						}
					}

				}
			}
			else if (component instanceof RouterComposition ){

			}
			if (logger.isDebugEnabled()){
				logger.debug("resolved channels for SI component $component with inputChannel ${component.inputChannel}" +
						(component instanceof MessageProducingEndpoint ? " outputChannel ${component.outputChannel}": ""))
			}
		}
	}
	
	private String channelName(from,to){
		to.inputChannel? to.inputChannel: "from.${from.name}.to.${to.name}"
	}

 

}
