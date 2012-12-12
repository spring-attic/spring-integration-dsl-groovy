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
import org.springframework.integration.dsl.groovy.GatewayEndpoint
import org.springframework.integration.dsl.groovy.IntegrationComponent
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.MessageProducingEndpoint
import org.springframework.integration.dsl.groovy.SimpleEndpoint
import org.springframework.integration.dsl.groovy.AbstractChannel


/**
 * @author David Turanski
 *
 */
class MessageFlowDomBuilder extends IntegrationComponentDomBuilder {
	ChannelDomBuilder channelBuilder
	SimpleEndpointDomBuilder endpointBuilder

	MessageFlowDomBuilder(IntegrationDomSupport integrationDomSupport) {
		this.integrationDomSupport = integrationDomSupport
	}

	@Override
	void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent messageFlow, Closure closure) {
		
		SimpleEndpointDomBuilder endpointBuilder = integrationDomSupport.domBuilder(SimpleEndpoint.class)
		ChannelDomBuilder channelBuilder = integrationDomSupport.domBuilder(AbstractChannel.class)

		def previousComponent = null

        //Fix for https://jira.springsource.org/browse/INTDSLGROOVY-11
		/*if (messageFlow.outputChannel) {
			channelBuilder.createDirectChannelIfNotDefined(builder, messageFlow.outputChannel)
		}*/

		for(component in messageFlow.components){
			if (component instanceof FlowExecution ) {
				if (!previousComponent) {
					endpointBuilder.createBridge(builder,messageFlow.inputChannel, component.inputChannel)
				}
			}
			else if (component instanceof MessageFlow) {
				resolveMessageFlowChannels(component)
				build(builder, applicationContext, component)
			} else {
				integrationDomSupport.domBuilder(component).build(builder,applicationContext, component)
			}

			previousComponent = component
		}
        //Fix for https://jira.springsource.org/browse/INTDSLGROOVY-11
		if (messageFlow.inputChannel?.startsWith('$mflw')){
            channelBuilder.createDirectChannelIfNotDefined(builder, messageFlow.inputChannel)
        }
	}

	def resolveMessageFlowChannels(messageFlow) {

		def endpoints = messageFlow.components.findAll{it.hasProperty('inputChannel') || it.hasProperty('requestChannel')}
		def first = endpoints?.first()

		if (first.hasProperty('inputChannel')){
			first.inputChannel = first.inputChannel ?: messageFlow.inputChannel
		} else if (first.hasProperty('requestChannel')) {
			first.requestChannel = first.requestChannel ?: messageFlow.inputChannel
		}

		def last = endpoints?.last()

		if (last instanceof MessageProducingEndpoint){
			last.outputChannel = last.outputChannel ?: messageFlow.outputChannel
		}

		if (last instanceof GatewayEndpoint){
			last.replyChannel = last.replyChannel ?: messageFlow.outputChannel
		}

		def outputChannel = null

		endpoints.eachWithIndex {component, i->

			if (component instanceof SimpleEndpoint ) {
				if (component != first && endpoints[i-1] instanceof GatewayEndpoint ) {
					component.inputChannel = component.inputChannel ?: endpoints[i-1].requestChannel
				}
				component.inputChannel = component.inputChannel ?: outputChannel

				if (component instanceof MessageProducingEndpoint ) {
					if (component != last && component.linkToNext) {
						component.outputChannel = outputChannel = component.outputChannel ?: channelName(component,endpoints[i+1])
						//If component is Flow execution in the midst of a messageFlow, it requires an outputChannel
						if (component instanceof FlowExecution ) {
							def c = component.messageFlow.components.last()
							component.messageFlow.components.last().outputChannel = outputChannel
						}
					}

				}
			}
			else if (component instanceof GatewayEndpoint ){
				component.requestChannel = component.requestChannel ?: outputChannel
				if (component != last && component.linkToNext) {
					component.replyChannel = outputChannel = component.replyChannel ?: channelName(component,endpoints[i+1])
				}

			}

		}
	}

	private String channelName(from,to){
		String channelName = getInboundChannelPropertyIfNull(to,"from.${from.name}.to.${to.name}")
	}

	private getInboundChannelPropertyIfNull(component, value) {
		def channelName
		if (component.hasProperty('inputChannel')){
			channelName = component.inputChannel? component.inputChannel: value
		}
		else
		if (component.hasProperty('requestChannel')){
			channelName = component.requestChannel? component.requestChannel: value
		}
		channelName
	}

	private getOutboundChannelPropertyIfNull(component, value) {
		def channelName
		if (component.hasProperty('outputChannel')){
			channelName = component.outputChannel? component.outputChannel: value
		}
		else
		if (component.hasProperty('replyChannel')){
			channelName = component.replyChannel? component.replyChannel: value
		}
		channelName
	}
}
