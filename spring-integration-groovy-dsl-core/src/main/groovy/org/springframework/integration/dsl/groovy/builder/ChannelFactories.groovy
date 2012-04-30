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

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.dsl.groovy.Channel
import org.springframework.integration.dsl.groovy.IntegrationConfig
import org.springframework.integration.dsl.groovy.IntegrationContext
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.PubSubChannel
import org.springframework.integration.dsl.groovy.QueueChannel

 
/**
 *
 * @author David Turanski
 */
class ChannelFactory  extends IntegrationComponentFactory {
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()){
			logger.debug("newInstance name: $name value:$value attr:$attributes")
		}
		
	    attributes = defaultAttributes(name,value,attributes) 
		
		switch(name){
		  case 'channel': 
			return new Channel(attributes)
		  case 'pubSubChannel':
		  	return new PubSubChannel(attributes)
		  case 'queueChannel':
		   return new QueueChannel(attributes)
		}
	}
	
	public void setParent(FactoryBuilderSupport builder, Object parent, Object child) {
		if (parent instanceof MessageFlow) {
			parent.integrationContext.add(child)
		} else if (parent instanceof IntegrationContext) {
			parent.add(child)
		} else {
			throw new IllegalStateException("${child.builderName} cannot be a child of ${parent.builderName}")
		}
	}
}

class ChannelInterceptorFactory extends AbstractFactory {
	private static Log logger = LogFactory.getLog(ChannelInterceptorFactory.class)
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {

		if (logger.isDebugEnabled()){
			logger.debug("newInstance name: $name value:$value attr:$attributes")
		}
		// TODO Auto-generated method stub
		return name;
	}
}

