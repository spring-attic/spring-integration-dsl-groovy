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

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.AbstractFactory
import groovy.util.FactoryBuilderSupport
import java.io.InputStream

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition
import org.springframework.integration.dsl.groovy.IntegrationConfig
import org.springframework.integration.dsl.groovy.IntegrationContext
import org.springframework.integration.dsl.groovy.Filter
import org.springframework.integration.dsl.groovy.ServiceActivator
import org.springframework.integration.dsl.groovy.Transformer
import org.springframework.integration.dsl.groovy.MessageFlow

/**
 * @author David Turanski
 *
 */


class IntegrationBuilder extends FactoryBuilderSupport {
	private static Log logger = LogFactory.getLog(IntegrationBuilder.class)
	private static stubFactory = new StubFactory()
	private IntegrationContext integrationContext;

	IntegrationBuilder() {
		super(true)
		this.integrationContext = new IntegrationContext()
	}
	
	public IntegrationContext getIntegrationContext() {
		this.integrationContext
	}

	@Override
	def registerObjectFactories() {
		registerFactory "config", new ConfigFactory()
		registerFactory "messageFlow", new MessageFlowFactory()
		registerFactory "doWithSpringIntegration", new IntegrationContextFactory()
		/*
		 * Simple endpoints
		 */
		registerFactory "filter", new FilterFactory()
		registerFactory "transform", new TransformerFactory()
		registerFactory "handle", new ServiceActivatorFactory()
		registerFactory "bridge", new BridgeFactory()
		/*
		 * Router 
		 */
		registerFactory "route", new RouterCompositionFactory()
		registerFactory "when", new RouterConditionFactory()
		registerFactory "otherwise", new RouterConditionFactory()
		registerFactory "map", new ChannelMapFactory()
		
		/*
		 * XML Bean 
		 */
		registerFactory "springXml", new XMLBeanFactory()
		registerFactory "namespaces", new XMLNamespaceFactory()
	
		
		registerFactory "channel", new ChannelFactory()
		registerFactory "pubSubChannel", new ChannelFactory()
		registerFactory "queueChannel", new ChannelFactory()
		
		registerFactory "interceptor", new ChannelInterceptorFactory()
		registerFactory "onPreSend", new ChannelInterceptorFactory()
		registerFactory "jmsListen", stubFactory
		registerFactory "httpPost", stubFactory
		registerFactory "httpGet", stubFactory
		registerFactory "poll", new PollerFactory()
		registerFactory "exec", new FlowExecutionFactory()
		
	}

	ApplicationContext createApplicationContext(ApplicationContext parentContext=null) {
		this.integrationContext.createApplicationContext(parentContext);
	}
	
	MessageFlow[] getMessageFlows() {
		this.integrationContext.messageFlows
	}
	
	public Object build(InputStream is) {
		def script = new GroovyClassLoader().parseClass(is).newInstance()
		this.build(script)
	}	
	
}

abstract class IntegrationComponentFactory extends AbstractFactory {
	protected Log logger = LogFactory.getLog(this.class)
	
	protected defaultAttributes(name, value, attributes) {
		assert !(attributes.containsKey('name') && value), "$name cannot accept both a default value and a 'name' attribute"
		
		attributes = attributes ?: [:]
		attributes.builderName = name
		
		if (!attributes.containsKey('name') && value){
			attributes.name = value
		}
		
		attributes
	}
}

class StubFactory extends AbstractFactory {
	private static Log logger = LogFactory.getLog(StubFactory.class)
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {

		if (logger.isDebugEnabled()){
			logger.debug("newInstance name: $name value:$value attr:$attributes")
		}
		logger.warn("this factory is not implemented.")
		// TODO Auto-generated method stub
		return new Expando();
	}
}

class ConfigFactory extends IntegrationComponentFactory {
	 
	/* (non-Javadoc)
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object, java.util.Map)
	 */
	def newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes)
	throws InstantiationException, IllegalAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("creating new Config()")
		}
		new IntegrationConfig(attributes)
	}

	boolean isLeaf() {
		false
	}

	@Override
	void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object config ) {
		logger.debug("adding $config")
		builder.integrationContext.config = config
	}
}