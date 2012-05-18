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
package org.springframework.integration.dsl.groovy.jms.builder
import org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationDomSupport
import org.springframework.integration.dsl.groovy.jms.builder.dom.JmsListenerDomBuilder

/**
 * @author David Turanski
 *
 */
class IntegrationBuilderModuleSupport extends AbstractIntegrationBuilderModuleSupport {
	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport#registerBuilderFactories(org.springframework.integration.dsl.groovy.builder.IntegrationBuilder)
	 */
	@Override
	public void registerBuilderFactories(IntegrationBuilder builder) {
		builder.registerFactory "jmsListen", new JmsListenerFactory()
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport#registerXmlBuilders(org.springframework.integration.dsl.groovy.builder.dom.IntegrationMarkupSupport)
	 */
	@Override
	public void registerDomBuilders(IntegrationDomSupport integrationDomSupport) {
		integrationDomSupport.namespaceSupport.addIntegrationNamespace('int-jms')
		integrationDomSupport.domBuilders["org.springframework.integration.dsl.groovy.jms.JmsListener"] \
			= new JmsListenerDomBuilder(integrationDomSupport:integrationDomSupport)
	}
}
