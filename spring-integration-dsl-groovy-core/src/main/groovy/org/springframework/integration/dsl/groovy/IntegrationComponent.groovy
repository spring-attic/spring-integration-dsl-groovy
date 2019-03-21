/*
 * Copyright 2002-2014 the original author or authors.
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
package org.springframework.integration.dsl.groovy

import org.apache.commons.logging.LogFactory

/**
 * The base class for all components defined in the DSL model
 *
 * @author David Turanski
 *
 */
//todo change when @Trait comes to Groovy
@Mixin([AttributeHelper, ComponentNamer])
//@CompileStatic
abstract class IntegrationComponent   {
	protected logger = LogFactory.getLog(this.class)

	protected String name
	protected Map attributes = [:]
	protected String builderName

	def component

	//getter
	def propertyMissing(name) {
		attributes[propertyNameToAttributeName(name)]
	}

	//setter
	def propertyMissing(String name, Object val) {
		attributes[propertyNameToAttributeName(name)]=val
	}

	IntegrationComponent() {
		component = this
	}
}

/**
 * A class used to support core Spring components commonly used in integration
 * such as task executor and task scheduler, etc
 * @author David Turanski
 *
 */
class CoreSpringComponent extends IntegrationComponent {
	protected String namespacePrefix
}
