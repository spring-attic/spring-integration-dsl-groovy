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
  
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.OtherwiseCondition
import org.springframework.integration.dsl.groovy.Poller
import org.springframework.integration.dsl.groovy.Router
import org.springframework.integration.dsl.groovy.RouterComposition
import org.springframework.integration.dsl.groovy.RouterCondition
import org.springframework.integration.dsl.groovy.SimpleEndpoint
import org.springframework.integration.dsl.groovy.WhenCondition
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
/**
 * @author David Turanski
 *
 */
class PollerFactory extends IntegrationComponentFactory {
 
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
 
       attributes = defaultAttributes(name, value, attributes)
		
		def poller = new Poller(attributes)
			
		if (logger.isDebugEnabled()) {
			logger.debug("created poller attributes: $attributes")
		}
		poller
	}

	@Override
	void setParent(FactoryBuilderSupport builder, Object parent, Object poller) {		
		if (parent instanceof BaseIntegrationComposition) { 
			parent.add(poller)
		} 
		else if ( parent instanceof SimpleEndpoint ) {
			parent.poller = poller
		}
	}
}
