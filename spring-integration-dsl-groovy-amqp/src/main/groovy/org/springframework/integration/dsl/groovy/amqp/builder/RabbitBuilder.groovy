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
package org.springframework.integration.dsl.groovy.amqp.builder

import groovy.lang.Closure
import groovy.util.FactoryBuilderSupport
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

/**
 * A proxy builder to support doWithRabbit closure
 * @author David Turanski
 *
 */
class RabbitBuilder extends FactoryBuilderSupport {
	IntegrationBuilder builder
	RabbitBuilder(IntegrationBuilder builder){
		super(true)
		this.builder = builder
		registerExplicitMethod 'springXml', builder.&springXml
	}
	@Override
	def registerObjectFactories() {
		registerFactory 'directExchange', new RabbitExchangeFactory()
		registerFactory 'topicExchange', new RabbitExchangeFactory()
		registerFactory 'fanoutExchange', new RabbitExchangeFactory()
		registerFactory 'headersExchange', new RabbitExchangeFactory()
		registerFactory 'federatedExchange', new RabbitExchangeFactory()
		registerFactory 'connectionFactory', new RabbitConnectionFactoryFactory()
		registerFactory 'queue', new RabbitQueueFactory()
		registerFactory 'admin', new RabbitAdminFactory()
		registerFactory 'template', new RabbitTemplateFactory()
	}
}
