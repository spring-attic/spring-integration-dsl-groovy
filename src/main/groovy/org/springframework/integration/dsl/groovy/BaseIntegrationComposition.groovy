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
import org.springframework.integration.util.ClassUtils;

/**
 * @author David Turanski
 *
 */

protected abstract class IntegrationComponent   {
	protected logger = LogFactory.getLog(this.class)
	protected String name
	protected componentProperties = [:]
	
	void propertyMissing(String name, Object val) {
		componentProperties[name]=val
	}
}


protected class BaseIntegrationComposition {
	protected logger = LogFactory.getLog(this.getClass());
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

class SendingIntegrationComposition extends BaseIntegrationComposition {

}

 
class SendingEndpointComposition extends SendingIntegrationComposition  {
	def methodMissing(String name, args) {
		logger.debug("invoked $name")
	}
}

abstract class RouterCondition extends BaseIntegrationComposition {
	def channel
	def value	
}

class WhenCondition extends RouterCondition {
}

class OtherwiseCondition extends RouterCondition {
}

class RouterComposition extends BaseIntegrationComposition {
   String inputChannel
   String name
   def channelMap
   Closure evaluate
}




