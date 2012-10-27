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

import org.springframework.integration.dsl.groovy.AbstractChannel
import org.springframework.integration.dsl.groovy.Channel
import org.springframework.integration.dsl.groovy.ChannelInterceptor
import org.springframework.integration.dsl.groovy.IntegrationContext
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.PubSubChannel
import org.springframework.integration.dsl.groovy.QueueChannel
import org.springframework.integration.dsl.groovy.Wiretap


/**
 *
 * @author David Turanski
 */
class ChannelFactory  extends IntegrationComponentFactory {

	Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
		switch(name){
			case 'channel':
				return new Channel(attributes)
			case 'pubSubChannel':
				return new PubSubChannel(attributes)
			case 'queueChannel':
				return new QueueChannel(attributes)
		}
	}

	void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		if (parent instanceof MessageFlow) {
			parent.integrationContext.add(child)
		} else if (parent instanceof IntegrationContext) {
			parent.add(child)
		} else {
			throw new IllegalStateException("${child.builderName} cannot be a child of ${parent.builderName}")
		}
	}
}

class ChannelInterceptorFactory extends IntegrationComponentFactory {

	Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
		if (name == "interceptor") {
			return new ChannelInterceptor(attributes)
		}
		else if  (name == "wiretap") {
			return new Wiretap(attributes)
		}
	}

	void setParent(FactoryBuilderSupport builder, Object parent, Object channelInterceptor) {
		if (parent instanceof MessageFlow) {
			parent.integrationContext.add(channelInterceptor)
		} else if (parent instanceof IntegrationContext) {
			parent.add(channelInterceptor)
		}  else if (parent instanceof AbstractChannel) {
			channelInterceptor.global = false
			assert !channelInterceptor.attributes.containsKey('pattern'), "'pattern is only valid for global channel interceptors"
			parent.addChannelInterceptor(channelInterceptor)
		} else {
			throw new IllegalStateException("${channelInterceptor.builderName} cannot be a child of ${parent.builderName}")
		}
	}
}