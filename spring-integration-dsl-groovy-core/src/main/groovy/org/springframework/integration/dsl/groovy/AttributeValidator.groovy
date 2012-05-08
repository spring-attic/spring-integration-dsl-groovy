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
package org.springframework.integration.dsl.groovy;

import java.util.Map;

/**
 * 
 * @author David Turanski
 *
 */
class AttributeValidator {
	/**
	 * Validates component attributes and generates a descriptive error message
	 * @param attributes
	 * @return an error message. If null than attributes are valid
	 */

	def validateAttributes(Map attributes) {
	
		def validationContext = [hasErrors:false,errorMessage:null]
		String errorMsg
      
		if (component.hasProperty('attributesRequiresAnyOf')){

			def attributeCount = attributesRequiresAnyOf.empty ? 1 : 0

			if (attributeCount == 0) {
				attributes?.each { k,v -> if (attributesRequiresAnyOf.contains(k)) {attributeCount++} }
			}


			if (attributeCount == 0) {
				errorMsg = "'$builderName' must include at least one of $attributesRequiresAnyOf"
			}
		}
		
		if (component.hasProperty('mutuallyExclusiveAttributes')) {
			def first = true
			component.mutuallyExclusiveAttributes.each { keyList ->
				def mutuallyExclusive = []
				keyList.each { if (attributes?.containsKey(it)) {mutuallyExclusive << it} }
				if (mutuallyExclusive.size() > 1) {
					errorMsg = "'$builderName' contains mutually exclusive attributes $mutuallyExclusive"
				}
			}
		}
		
		errorMsg = validateAgainstSimpleList(
			errorMsg,
			attributes,
			'requiredAttributes',
			"'$builderName' is missing the following required attributes:",
			{ !attributes || !attributes.containsKey(it) }
		)
		
		if (errorMsg){
			validationContext.hasErrors = true
			validationContext.errorMessage = errorMsg
		}
		validationContext
	}
	
	def validateAgainstSimpleList(String errorMsg, Map attributes, String keyListProperty, topic, Closure closure) {
		if (component.hasProperty(keyListProperty)){
			def keyList = component."$keyListProperty"
			def first = true
			keyList.each {
				if (closure.call(it)) {
					if (first){
						errorMsg = (errorMsg ? errorMsg + "\n" :"") + topic
						first = false
					}
					errorMsg += ( errorMsg.endsWith(":") ? "$it" : ", $it" )
				}
			}
		}
		errorMsg
	}
}
