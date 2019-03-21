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
package org.springframework.integration.dsl.groovy.builder.dom

/**
 * Provides Spring Integration XML support for use by the MarkupBuilder
 *
 * @author David Turanski
 *
 */
class XMLNamespaceSupport {
	static final String REQUIRED_SCHEMA_LOCATIONS =
	"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd "+
	"http://www.springframework.org/schema/integration http://www.springframework.org/schema/integration/spring-integration.xsd"

	static final String DEFAULT_INTEGRATION_NAMESPACE_PREFIX = 'si'

	static final Map REQUIRED_NAMESPACE_DECLARATIONS =
	[
		'': 'http://www.springframework.org/schema/beans',
		xsi: 'http://www.w3.org/2001/XMLSchema-instance',
		si: 'http://www.springframework.org/schema/integration'
	]


	String schemaLocations = new String(REQUIRED_SCHEMA_LOCATIONS)

	Map namespaceDeclarations = [:] << REQUIRED_NAMESPACE_DECLARATIONS

	String integrationNamespacePrefix = DEFAULT_INTEGRATION_NAMESPACE_PREFIX

	/**
	 * Add an XML namespace for a Spring Integration component using Spring Integration conventions
	 * Example: The prefix 'int-jms' generates xmlns:int-jms="http://www.springframework.org/schema/integration/jms" and
	 * binds the namespace URL to the schema location "http://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd"
	 * @param prefix the namespace prefix - If it starts with 'int-' the component name will be the remaining characters otherwise the
	 * namespace prefix will be the same as the component name.
	 */
	void addIntegrationNamespace(String prefix) {
		def component = prefix
		def m = prefix =~ /^int-(.*)/
		if (m) {
			component = m[0][1]
		}

		def namespace = "http://www.springframework.org/schema/integration/$component"
		addSchemaLocation(namespace,namespace+"/spring-integration-${component}.xsd")
		namespaceDeclarations[prefix]=namespace
	}
	/**
	 * Set the namespace prefix for Spring integration core namespace
	 * @param prefix the namespace prefix
	 */
	void setCoreIntegrationNamespacePrefix(String prefix){
		if (namespaceDeclarations[prefix] && prefix != integrationNamespacePrefix ){
			throw new IllegalArgumentException("namespace prefix $prefix already in use")
		}

		if (prefix != integrationNamespacePrefix) {
			namespaceDeclarations[prefix]= 'http://www.springframework.org/schema/integration'
			namespaceDeclarations.remove(integrationNamespacePrefix)
			integrationNamespacePrefix = prefix
		}
	}

	/**
	 * Add default Spring namespace
	 * @param prefix
	 */
	void addSpringNamespace(String prefix) {
		def namespace = "http://www.springframework.org/schema/$prefix"
		addSchemaLocation(namespace,namespace+"/spring-${prefix}.xsd")
		namespaceDeclarations[prefix]=namespace
	}

	/**
	 *
	 * @param prefix
	 */
	void addDefaultNamespace(String prefix) {
		if (prefix && !namespaceDeclarations[prefix]) {
			if (prefix.startsWith("int-")) {
				addIntegrationNamespace(prefix)
			}
			else {
				addSpringNamespace(prefix)
			}
		}
	}
	/**
	 * Add a namespace declaration
	 * @param prefix
	 * @param namespaceUri
	 * @param schemaLocation
	 */
	void addNamespace(String prefix, String namespaceUri, String schemaLocation) {
		addSchemaLocation(namespaceUri,schemaLocation)
		namespaceDeclarations[prefix] = namespaceUri
	}


	/**
	 * Get the schemaLocation Declaration
	 * @return a singleton map containing the xsi:schemaLocation attribute
	 */
	protected schemaLocationDeclaration() {
		['xsi:schemaLocation':schemaLocations]
	}

	private addSchemaLocation(String namespace, xsd) {
		if (!schemaLocations.contains(namespace)){
			schemaLocations += " $namespace $xsd"
		}
	}
}
