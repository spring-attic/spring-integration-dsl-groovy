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
  
import java.util.Map

import org.apache.commons.logging.Log
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.OtherwiseCondition
import org.springframework.integration.dsl.groovy.RouterComposition
import org.springframework.integration.dsl.groovy.RouterCondition
import org.springframework.integration.dsl.groovy.WhenCondition
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
/**
 * @author David Turanski
 *
 */
class RouterCompositionFactory extends IntegrationComponentFactory {
	 
	public Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes){		
		new RouterComposition(attributes)
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object routerComposition ) {
		if (routerComposition.channelMap){
			assert routerComposition.components.empty, 'router cannot include both a channel map and conditions'
		}
		
		
		def otherwises = routerComposition.components.findAll {it instanceof OtherwiseCondition}
		
		if (!otherwises.empty) {
			assert otherwises.size == 1  && otherwises[0] == routerComposition.components.last(),
			 "only one 'otherwise' allowed and it must occur last"
		}
	}
	
	@Override
	void setParent(FactoryBuilderSupport builder, Object parent, Object child) {		
		if (parent instanceof MessageFlow) {
		}
		else {
			child.inputChannel = child.inputChannel ?: "${child.name}.inputChannel"
		}
		assert parent instanceof BaseIntegrationComposition
		parent.add(child)
	}
}


class RouterConditionFactory extends IntegrationComponentFactory  {
	
   public Object doNewInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes){
	   validate(name,value,attributes)
	   def routerCondition
	   if (name == "when"){
		   routerCondition =  new WhenCondition(attributes);
		   routerCondition.value=value
	   } else if (name=="otherwise") {

		   if (attributes.containsKey('channel')){
			   routerCondition =  new OtherwiseCondition(attributes)
		   } else {
			   routerCondition =  new OtherwiseCondition(channel:value)
		   }
	   }
	   routerCondition
   }
	public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		if (logger.isDebugEnabled()) {
			logger.debug("set parent parent:${parent.class} child:${child.class}")
		}
		assert parent instanceof RouterComposition, "${parent.class} must be a child of " + RouterComposition	
		parent.add(child)		
	}

	private validate(name,value,attributes) {
		if (name == "when") {
			assert value, "'when' requires a value'"
		} else if (name == "otherwise" ) {
		}
	}
}