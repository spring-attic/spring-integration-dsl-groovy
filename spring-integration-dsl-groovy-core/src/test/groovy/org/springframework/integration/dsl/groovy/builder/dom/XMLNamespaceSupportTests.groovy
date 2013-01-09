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

import static org.junit.Assert.*
import org.junit.*
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

/**
 * @author David Turanski
 *
 */
class XMLNamespaceSupportTests {
	def namespaceSupport = new XMLNamespaceSupport()
	
	@Test
	void testMultipleNamespaces () {
		new IntegrationBuilder().doWithSpringIntegration {
			namespaces("int-twitter,rabbit")
		}
	}

	@Test
	void testAddIntegrationNamespace() {
		namespaceSupport.addIntegrationNamespace('jms')

		assert namespaceSupport.schemaLocations.contains(
		'http://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd')

		assert namespaceSupport.schemaLocations != XMLNamespaceSupport.REQUIRED_NAMESPACE_DECLARATIONS
		assert namespaceSupport.namespaceDeclarations.jms == 'http://www.springframework.org/schema/integration/jms'
	}

	@Test
	void testAddStandardIntegrationNamespace() {
		namespaceSupport.addIntegrationNamespace('int-jms')
		assert namespaceSupport.namespaceDeclarations.'int-jms' == 'http://www.springframework.org/schema/integration/jms'
	}

	@Test
	void testAddStandardSpringNamespace() {
		namespaceSupport.addSpringNamespace('jms')
		assert namespaceSupport.namespaceDeclarations.'jms' == 'http://www.springframework.org/schema/jms'
	}

	@Test
	void testAddSDefaultNamespace() {
		namespaceSupport.addDefaultNamespace('int-http')
		assert namespaceSupport.namespaceDeclarations.'int-http' == 'http://www.springframework.org/schema/integration/http'
	}

	@Test
	void testSetCoreIntegrationNamespacePrefix() {
		namespaceSupport.setCoreIntegrationNamespacePrefix('int')
		assert namespaceSupport.namespaceDeclarations.int == 'http://www.springframework.org/schema/integration'
		assert !namespaceSupport.namespaceDeclarations.si

		namespaceSupport.setCoreIntegrationNamespacePrefix('si')
		assert namespaceSupport.namespaceDeclarations.si == 'http://www.springframework.org/schema/integration'
		assert !namespaceSupport.namespaceDeclarations.int

		try {
			namespaceSupport.setCoreIntegrationNamespacePrefix('xsi')
			fail('should throw exception on existing prefix')
		} catch (e) {
		}

		try {
			namespaceSupport.addIntegrationNamespace('jms')
			namespaceSupport.setCoreIntegrationNamespacePrefix('jms')
			fail('should throw exception on existing prefix')
		} catch (e) {
		}
	}
}
