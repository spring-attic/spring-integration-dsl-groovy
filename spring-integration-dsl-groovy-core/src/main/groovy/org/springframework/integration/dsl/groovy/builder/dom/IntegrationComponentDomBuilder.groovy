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
package org.springframework.integration.dsl.groovy.builder.dom


import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.context.ApplicationContext
import groovy.lang.Closure
import java.util.Map

/**
 * @author David Turanski
 *
 */
abstract class IntegrationComponentDomBuilder {
	protected Log logger = LogFactory.getLog(this.class)
	protected IntegrationDomSupport integrationDomSupport

	void build(builder, ApplicationContext applicationContext, component, Closure closure){
		def attributes = component.componentProperties
		if (!component.id){
			component.id = component.name
		}

		doBuild(builder,applicationContext,component,attributes,closure)
	}

	void build(builder, ApplicationContext applicationContext, component) {
		build(builder, applicationContext, component, null)
	}

	protected String getSiPrefix() {
		integrationDomSupport.namespaceSupport.integrationNamespacePrefix
	}

	/** 
	 * @param builder StreamingMarkupBuilder
	 * @param applicationContext the Spring ApplicationContext
	 * @param component the IntegrationComponent
	 * @param attributes a Map of undeclared component properties passed as named parameters
	 * @param an optional closure containing additional XML markup used to generate child elements if necessary
	 */
	protected abstract void doBuild(Object builder, ApplicationContext applicationContext, Object component, Map attributes, Closure closure);
}
