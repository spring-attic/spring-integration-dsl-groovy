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
package org.springframework.integration.dsl.groovy.builder;

import groovy.util.FactoryBuilderSupport;

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.Map;

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.integration.dsl.groovy.*
 

/**
 * @author David Turanski
 *
 */

abstract class EndpointFactory extends IntegrationComponentFactory {
	
	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		logger.debug("newInstance name: $name value:$value attr:$attributes")
			
	   attributes = defaultAttributes(name, value, attributes)
		if (attributes.evaluate){
			attributes.action = attributes.evaluate
			attributes.remove('evaluate')
		}
		
		logger.debug("newInstance name: $name value:$value attr:$attributes")

		return endpointInstance(attributes);
	}

	@Override
	public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		if (parent instanceof MessageFlow) {
	        
		}	
		else {
			child.inputChannel = child.inputChannel ?: "${child.name}.inputChannel" 
		}
		
		
		if (parent.respondsTo("add")){
			parent.add(child)
			if (logger.isDebugEnabled()){
				logger.debug("setParent parent ${parent} child $child")
			}
		} else {
			logger.warn("attempted to invoke 'add' method on parent")
		}
	}
	
	protected abstract SimpleEndpoint endpointInstance(Map attributes) 
}

public class TransformerFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		return new Transformer(attributes)
	}
}

public class FilterFactory extends EndpointFactory {
		@Override
		protected SimpleEndpoint endpointInstance(Map attributes) {	 
			return new Filter(attributes)
		}	
}

public class ServiceActivatorFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		return new ServiceActivator(attributes)
	}
}

public class BridgeFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		return new Bridge(attributes)
	}
}

public class SplitterFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		return new Splitter(attributes)
	}
}

public class AggregatorFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		return new Aggregator(attributes)
	}
}


