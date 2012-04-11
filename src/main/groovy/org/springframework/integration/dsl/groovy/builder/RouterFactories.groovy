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
  
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition
import org.springframework.integration.dsl.groovy.OtherwiseCondition
import org.springframework.integration.dsl.groovy.Router
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
	//private routerFactory = new EndpointFactory(Router)
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("creating router for name: $name value: $value attributes: $attributes")
		}
		
		attributes = attributes ?: [:]
		
		if (!attributes.containsKey('name') && value){
			attributes.name = value
		}
		if (!attributes.containsKey('inputChannel') && value){
			attributes.inputChannel = "${value}#inputChannel"
		}
		
	 
		def routerComposition = new RouterComposition(attributes)
		
		

	 	    
		if (logger.isDebugEnabled()) {
			logger.debug("created router composition for name: $name value: $value attributes: $attributes")
		}
		
		routerComposition
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object routerComposition ) {
		if (!routerComposition.channelMap){
			assert routerComposition.components.size >= 1, 'router contains no conditions'
		} else {
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
		if (logger.isDebugEnabled()){
			logger.debug("setParent parent ${parent} child $child")
		}
		assert parent instanceof BaseIntegrationComposition
		parent.add(child)
		
	}
}


class RouterConditionFactory extends IntegrationComponentFactory  {
	 
	public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		if (logger.isDebugEnabled()) {
			logger.debug("set parent parent:${parent.class} child:${child.class}")
		}
		assert parent.class == RouterComposition.class, "${parent.class} must be a child of " + RouterComposition.class
		
		parent.add(child)
		 
		
	}

	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("creating router condition for $name $value $attributes")
		}
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

	private validate(name,value,attributes) {
		if (name == "when") {
			assert value, "'when' requires a value'"
		} else if (name == "otherwise" ) {
		}
	}
}