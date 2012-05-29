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

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.context.ApplicationContext
import org.springframework.integration.dsl.groovy.BaseIntegrationComposition
import org.springframework.integration.dsl.groovy.IntegrationContext
import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.SpringModuleContext
import org.springframework.integration.dsl.groovy.XMLBean

/**
 * Workaround for DefaultGroovyMethods.split()
 * @author David Turanski
 *
 */
class IntegrationBuilderCategory {
	/**
	 * Overrrides the DefaultGroovyMethods.split() with no parameters
	 * @param self
	 * @param closure
	 * @return the result of builder invoking split
	 */
	static Object split(Object self, Closure closure){
		self.delegate.splitter([closure]as Object[] )
	}
}

/**
 * Builds a Spring Integration application context from the DSL
 * @author David Turanski
 *
 */
class IntegrationBuilder extends FactoryBuilderSupport {
	private static final Log logger = LogFactory.getLog(IntegrationBuilder.class)
	private final IntegrationContext integrationContext
	private final ApplicationContext parentContext
	private boolean autoCreateApplicationContext = true

	IntegrationBuilder(ApplicationContext parentContext = null) {
		super(true)
		this.integrationContext = new IntegrationContext()
		this.parentContext = parentContext
	}

	IntegrationBuilder(List<String> modules, ApplicationContext parentContext = null) {
		this(modules as String[])
		this.parentContext = parentContext
	}

	IntegrationBuilder(String... modules) {
		this()
		def moduleSupportInstances =
				getIntegrationBuilderModuleSupportInstances(modules)

		this.integrationContext.moduleSupportInstances = moduleSupportInstances

		moduleSupportInstances.each { AbstractIntegrationBuilderModuleSupport moduleSupport ->
			moduleSupport.registerBuilderFactories(this)
		}
	}

	void setAutoCreateApplicationContext(boolean autoCreateApplicationContext){
		this.autoCreateApplicationContext = autoCreateApplicationContext
	}

	ApplicationContext getApplicationContext() {
		this.integrationContext.applicationContext
	}

	IntegrationContext getIntegrationContext() {
		this.integrationContext
	}

	@Override
	def registerObjectFactories() {

		registerFactory 'messageFlow', new MessageFlowFactory()
		registerFactory 'doWithSpringIntegration', new IntegrationContextFactory()
		/*
		 * Simple endpoints
		 */
		registerFactory 'bridge', new BridgeFactory()
		registerExplicitMethod 'filter', this.&filter
		registerExplicitMethod 'transform', this.&transformer
		registerExplicitMethod 'handle', this.&serviceActivator
		registerExplicitMethod 'aggregate', this.&aggregator
		registerExplicitMethod 'split', this.&splitter

		/*
		 * Router
		 */
		registerExplicitMethod 'route', this.&router
		registerFactory 'when', new RouterConditionFactory()
		registerFactory 'otherwise', new RouterConditionFactory()
		registerFactory 'map', new ChannelMapFactory()

		/*
		 * XML Bean
		 */
		registerExplicitMethod 'springXml', this.&springXml
		registerFactory 'namespaces', new XMLNamespaceFactory()


		registerFactory 'channel', new ChannelFactory()
		registerFactory 'pubSubChannel', new ChannelFactory()
		registerFactory 'queueChannel', new ChannelFactory()
		registerFactory 'interceptor', new ChannelInterceptorFactory()
		registerFactory 'wiretap', new ChannelInterceptorFactory()

		registerFactory 'poll', new PollerFactory()
		registerFactory 'exec', new FlowExecutionFactory()
	}

	@Override
	protected dispathNodeCall(name, args){
		use (IntegrationBuilderCategory) {
			super.dispathNodeCall(name,args)
		}
	}

	MessageFlow[] getMessageFlows() {
		this.integrationContext.messageFlows
	}

	Object build(InputStream is) {
		def script = new GroovyClassLoader().parseClass(is).newInstance()
		this.build(script)
	}

	def filter(Object[] args){
		createActionAwareEndpoint(new FilterFactory(), 'filter', args)
	}

	def transformer(Object[] args) {
		createActionAwareEndpoint(new TransformerFactory(), 'transform', args)
	}

	def serviceActivator(Object[] args) {
		createActionAwareEndpoint(new ServiceActivatorFactory(), 'handle', args)
	}

	def splitter(Object[] args) {
		createActionAwareEndpoint(new SplitterFactory(), 'split', args)
	}

	def router(Object[] args) {
		createActionAwareEndpoint(new RouterCompositionFactory(), 'route', args)
	}

	def aggregator(Object[] args) {
		createActionAwareEndpoint(new AggregatorFactory(), 'aggregate', args)
	}

	def createActionAwareEndpoint(IntegrationComponentFactory factory, String name, Object[] args){
		/*
		 * Find the action closure if any, remove it from the args and use an ActionAwareEndpointFactory to
		 * build the node correctly
		 */

		Closure actionClosure

		list = []
		list.addAll(0,args)

		Map attributes = [:]

		if ( (list.size() > 0) && (list.last() instanceof Map)) {
			attributes = list.head()
		}
		/*
		 * Identify any unnamed closures. Should be at most 2. If there are 2, the first one must be the action closure
		 */
		def closures = list.findAll {it instanceof Closure}

		if (closures?.size == 2) {
			logger.debug(closures)
			actionClosure = closures[0]
		}

		/*
		 * If only one closure, assume it's an action closure unless the 'ref' attribute is set. If the 'ref'
		 * attribute is set, then it is invalid to also provide an action closure. In this case assume it's the
		 * builder closure
		 */
		else if (closures?.size == 1) {
			if (!attributes.containsKey('ref')){
				logger.debug(closures)
				actionClosure = closures[0]
			}
		}
		/*
		 * If we have an action closure, remove it from the arg list and register a new
		 * ActionAwareEndpointFactory instance to set the action after the node is created
		 */
		if (actionClosure){
			assert list.contains(actionClosure)
			list = list - actionClosure
			registerFactory(name,new ActionAwareEndpointFactory(factory,actionClosure.dehydrate()))
		} else {
			registerFactory(name,factory)
		}

		if (logger.isDebugEnabled()) {
			logger.debug("invoking dispathNodeCall for name: $name with args: $list")
		}

		def node = dispathNodeCall(name,list as Object[])
	}

	def springXml(Closure closure) {
		def parent = getCurrent()
		parent = parent?:this.integrationContext
		assert parent instanceof BaseIntegrationComposition, "'springXml' is not valid in this context"
		parent.add(new XMLBean(builderName:"springXml",beanDefinitions:closure.dehydrate()))
		closure = null
		this
	}

	private getIntegrationBuilderModuleSupportInstances(String[] modules) {
		def instances = []
		modules?.each { module ->
			def className = "org.springframework.integration.dsl.groovy.${module}.builder.IntegrationBuilderModuleSupport"
			if (logger.isDebugEnabled()) {
				logger.debug("checking classpath for $className")
			}
			instances << this.class.classLoader.loadClass(className).newInstance()
		}
		instances
	}
}