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

import org.springframework.integration.dsl.groovy.*

/**
 * @author David Turanski
 *
 */

abstract class EndpointFactory extends IntegrationComponentFactory {

	@Override
	Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes){
		endpointInstance(attributes)
	}

	@Override
	void setParent(FactoryBuilderSupport builder, Object parent, Object child) {

		if (!(parent instanceof MessageFlow)) {
			child.inputChannel = child.inputChannel ?: "${child.name}.inputChannel"
		}

		if (parent.respondsTo('add')){
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

class TransformerFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new Transformer(attributes)
	}
}

class FilterFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new Filter(attributes)
	}
}

class ServiceActivatorFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new ServiceActivator(attributes)
	}
}

class BridgeFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new Bridge(attributes)
	}
}

class SplitterFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new Splitter(attributes)
	}
}

class AggregatorFactory extends EndpointFactory {
	@Override
	protected SimpleEndpoint endpointInstance(Map attributes) {
		new Aggregator(attributes)
	}
}

class ActionAwareEndpointFactory extends AbstractFactory {
	Closure action
	IntegrationComponentFactory factory
	ActionAwareEndpointFactory(IntegrationComponentFactory factory, Closure action) {
		this.action = action
		this.factory = factory
	}

	Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		def node = factory.newInstance(builder, name, value, attributes)
		node.action = action
		node
	}

	@Override
	void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		factory.setParent(builder,parent,child)
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object child ) {
		factory.onNodeCompleted(builder,parent,child)
	}
}