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

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition

/**
 * @author David Turanski
 *
 */
abstract class IntegrationComponentFactory extends AbstractFactory {
	protected Log logger = LogFactory.getLog(this.class)

	protected defaultAttributes(name, value, Map attributes) {
		assert !(attributes.containsKey('name') && value), "$name cannot accept both a default value and a 'name' attribute"
		assert !(attributes.containsKey('id') && value), "$name cannot accept both a default value and a 'id' attribute"

		attributes = attributes ?: [:]
		attributes.builderName = name

		if (!attributes.containsKey('name') && value){
			attributes.name = value
		}

		attributes
	}

	def newInstance(FactoryBuilderSupport builder, name, value, Map attributes) throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()){
			logger.debug("newInstance name: $name value:$value attr:$attributes")
		}

		attributes = defaultAttributes(name, value, attributes)
		def instance = doNewInstance(builder, name, value, attributes)
		if (instance.respondsTo('validateAttributes')) {
			def validationContext = instance.validateAttributes(attributes)
			assert !validationContext.hasErrors, validationContext.errorMessage
		}
		instance
	}

	@Override
	void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		assert parent instanceof BaseIntegrationComposition, "'${parent.builderName}' cannot be a child of '${child.builderName}'"
		parent.add child
	}

	protected abstract Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
}