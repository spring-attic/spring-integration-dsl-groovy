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
package org.springframework.integration.dsl.groovy.amqp.builder;

import org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationDomSupport

/**
 * @author David Turanski
 *
 */
class IntegrationBuilderModuleSupport extends AbstractIntegrationBuilderModuleSupport {

	@Override
	void registerBuilderFactories(IntegrationBuilder builder) {
		builder.registerFactory 'rabbitDirectExchange', new RabbitExchangeFactory()
		builder.registerFactory 'rabbitTopicExchange', new RabbitExchangeFactory()
		builder.registerFactory 'rabbitFanoutExchange', new RabbitExchangeFactory()
		builder.registerFactory 'rabbitHeadersExchange', new RabbitExchangeFactory()
		builder.registerFactory 'rabbitFederatedExchange', new RabbitExchangeFactory()
		builder.registerFactory 'rabbitConnectionFactory', new RabbitConnectionFactoryFactory()
		builder.registerFactory 'rabbitQueue', new RabbitQueueFactory()
		builder.registerFactory 'rabbitAdmin', new RabbitAdminFactory()
		builder.registerFactory 'doWithRabbit', new RabbitContextFactory()
	}

	@Override
	void registerDomBuilders(IntegrationDomSupport integrationDomSupport) {
		integrationDomSupport.namespaceSupport.addIntegrationNamespace('int-amqp')
		integrationDomSupport.namespaceSupport.addSpringNamespace('rabbit')
	}
}
