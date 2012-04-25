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
import org.springframework.integration.dsl.groovy.OtherwiseCondition
import org.springframework.integration.dsl.groovy.PubSubChannel
import org.springframework.integration.dsl.groovy.QueueChannel
import org.springframework.integration.dsl.groovy.RouterComposition
import org.springframework.integration.dsl.groovy.RouterCondition
import org.springframework.integration.dsl.groovy.SimpleEndpoint
import org.springframework.integration.dsl.groovy.WhenCondition


/**
 * @author David Turanski
 *
 */
class ChannelXmlBuilder extends IntegrationComponentXmlBuilder {
	
	private declaredChannels = []
	
	ChannelXmlBuilder(IntegrationMarkupSupport integrationMarkupSupport) {
		this.integrationMarkupSupport = integrationMarkupSupport
	}
	
   /*
    * Builds and registers a channel definition. These can be overridden	
    */
   @Override 
   public void build(builder, ApplicationContext applicationContext, Object channel, Closure closure) {
		if (channel instanceof Channel) {
			builder."$siPrefix:channel"([id:channel.name] << channel.componentProperties)
		} else if (channel instanceof PubSubChannel) {
			builder."$siPrefix:publish-subscribe-channel"([id:channel.name] << channel.componentProperties)
		} else if (channel instanceof QueueChannel) {
			builder."$siPrefix:channel"(id:channel.name) {	 
				"$siPrefix:queue"(channel.componentProperties)
			}
		}
		declaredChannels << channel.name
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
