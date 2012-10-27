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
package org.springframework.integration.dsl.groovy.amqp.builder.dom

import groovy.lang.Closure;

import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.integration.dsl.groovy.IntegrationComponent
import org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder

/**
 * @author David Turanski
 *
 */
class AmqpInboundDomBuilder extends IntegrationComponentDomBuilder {

	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.builder.dom.IntegrationComponentDomBuilder#doBuild(java.lang.Object, org.springframework.context.ApplicationContext, java.lang.Object, java.util.Map, groovy.lang.Closure)
	 */
	@Override
	protected void doBuild(Object builder, ApplicationContext applicationContext, IntegrationComponent component,
			Closure closure) {
			
		def attributes = component.attributes
			
		attributes.'connection-factory' = attributes.'connection-factory'?:'connectionFactory'
		
		if (component.requestChannel){
			attributes.'request-channel' = component.requestChannel
		}

		builder.'int-amqp:inbound-gateway'(attributes)
		
	}

}
