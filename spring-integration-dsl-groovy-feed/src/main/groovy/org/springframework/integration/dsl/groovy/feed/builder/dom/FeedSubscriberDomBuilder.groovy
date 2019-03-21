/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.dsl.groovy.feed.builder.dom

import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.Channel
import org.springframework.integration.dsl.groovy.IntegrationComponent
import org.springframework.integration.dsl.groovy.Poller
import org.springframework.integration.dsl.groovy.builder.dom.ChannelDomBuilder
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder

/**
 * @author Russell Hart
 *
 */
class FeedSubscriberDomBuilder extends IntegrationComponentDomBuilder {

	@Override
	void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent component, Closure closure) {

		def attributes = component.attributes
		attributes.url = component.url
		
		if (component.feedFetcher) {
			attributes.'feed-fetcher' = component.feedFetcher
		}
		
		if (component.metadataStore) {
			attributes.'metadata-store' = component.metadataStore
		}
		
		if (component.outputChannel) {
			attributes.'channel' = component.outputChannel
		}
		
		builder.'int-feed:inbound-channel-adapter'(attributes) {
			if (component.poller) {
				if (component.poller instanceof Poller) {
					"$siPrefix:poller"(component.poller.attributes)
				} else if (component.poller instanceof String) {
					"$siPrefix:poller"(ref:component.poller)
				}
			}
		} 
	}
}
