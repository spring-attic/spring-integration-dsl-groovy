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
import org.springframework.integration.dsl.groovy.PubSubChannel
import org.springframework.integration.dsl.groovy.QueueChannel
import org.springframework.integration.dsl.groovy.ChannelInterceptor
import org.springframework.integration.dsl.groovy.Wiretap



/**
 * @author David Turanski
 *
 */
class ChannelDomBuilder extends IntegrationComponentDomBuilder {

	private declaredChannels = []

	ChannelDomBuilder(IntegrationDomSupport integrationDomSupport) {
		this.integrationDomSupport = integrationDomSupport
	}

	/*
	 * Builds and registers a channel definition. These can be overridden
	 */
	@Override
	public void build(builder, ApplicationContext applicationContext, Object channel, Closure closure) {
		if (channel instanceof Channel) {
			builder."$siPrefix:channel"([id:channel.name] << channel.componentProperties) {
				addChannelInterceptors(builder, applicationContext, channel, closure)
			}
		} else if (channel instanceof PubSubChannel) {
			builder."$siPrefix:publish-subscribe-channel"([id:channel.name] << channel.componentProperties){
				addChannelInterceptors(builder, applicationContext, channel, closure)
			}
		} else if (channel instanceof QueueChannel) {
			builder."$siPrefix:channel"(id:channel.name) {
				"$siPrefix:queue"(channel.componentProperties)
				addChannelInterceptors(builder, applicationContext, channel, closure)
			}
		}
		declaredChannels << channel.name
	}

	private addChannelInterceptors(builder, ApplicationContext applicationContext, Object channel, Closure closure){
		if (channel.channelInterceptors){
			def ciBuilder = integrationDomSupport.domBuilder(ChannelInterceptor.class.name)
			builder."$siPrefix:interceptors"{
				channel.channelInterceptors.each {
					if (it instanceof Wiretap){
						"$siPrefix:wire-tap"(channel:it.channel)
					} else {
						ciBuilder.build(builder,applicationContext, it, closure)
					}
				}
			}
		}
	}

	/**
	 * Declares a direct channel if not defined already
	 * @param builder
	 * @param channelName
	 * @return
	 */
	def createDirectChannelIfNotDefined (builder, channelName) {
		if (!declaredChannels.contains(channelName)){
			builder."$siPrefix:channel"(id:channelName)
			declaredChannels << channelName
		}
	}
}
