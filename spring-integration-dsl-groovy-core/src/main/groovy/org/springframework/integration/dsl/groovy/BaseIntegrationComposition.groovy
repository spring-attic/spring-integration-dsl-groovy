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
package org.springframework.integration.dsl.groovy

import org.apache.commons.logging.LogFactory

/**
 * @author David Turanski
 *
 */
@Mixin(AttributeValidator)
@Mixin(ComponentNamer)
abstract class IntegrationComponent   {
	protected logger = LogFactory.getLog(this.class)
	protected String name
	protected componentProperties = [:]
	protected String builderName
	
	def component
	
	def propertyMissing(name) {
		componentProperties[name]
	}
	
	def propertyMissing(String name, Object val) {	
		componentProperties[name]=val
	}	
	
	IntegrationComponent() {
		component = this
	}
}

/**
 * A class used to support core Spring components commonly used in integration such as task executor and task scheduler, etc
 * @author David Turanski
 *
 */
class CoreSpringComponent extends IntegrationComponent {
	protected String namespacePrefix
}

class BaseIntegrationComposition extends IntegrationComponent {
	protected BaseIntegrationComposition parentComposition;
	protected components = [];
	
	def add(child){
		 components << child
		 if (child instanceof BaseIntegrationComposition) {
			 child.parentComposition = this
		 }
	}
	
	String toString(){
		 "{${this.class} components $components}" 
	}
}


abstract class RouterCondition extends BaseIntegrationComposition {
	def channel
	def value	
}

class WhenCondition extends RouterCondition {
}

class OtherwiseCondition extends RouterCondition {
	def name
	OtherwiseCondition(){
		name = defaultName("$otherwise")
	}
}

class RouterComposition extends BaseIntegrationComposition {
   String inputChannel
   String name
   def channelMap
   Closure action
   
   RouterComposition() {
	   name = defaultName("$rtr")
   }
   
   void setEvaluate(closure) {
	   action = closure
   }
   
   def getEvaluate() {
	   action
   }
}
