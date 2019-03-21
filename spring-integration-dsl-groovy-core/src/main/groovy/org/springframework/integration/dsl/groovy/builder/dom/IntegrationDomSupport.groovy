/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.dsl.groovy.builder.dom

import groovy.xml.StreamingMarkupBuilder

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.integration.dsl.groovy.*
import org.springframework.integration.dsl.groovy.builder.AbstractIntegrationBuilderModuleSupport
import groovy.xml.XmlUtil

/**
 * @author David Turanski
 * Translates Spring Integration DSL model to Spring XML
 */
class IntegrationDomSupport {
	private Log logger = LogFactory.getLog(IntegrationDomSupport)
	private XMLNamespaceSupport namespaceSupport = new XMLNamespaceSupport()
	
	private final domBuilders = [:]

	/**
	 * Default constructor
	 */
	IntegrationDomSupport() {
		registerBuilders()
	}
	
	/**
	 * Constructor to register additional DOM builders for external modules
	 *  
	 * @param moduleSupportInstances
	 */
	IntegrationDomSupport(List<AbstractIntegrationBuilderModuleSupport> moduleSupportInstances) {
		this()
		moduleSupportInstances?.each {AbstractIntegrationBuilderModuleSupport moduleSupport ->
			moduleSupport.registerDomBuilders(this)
			moduleSupport.registerNamespaces(this.namespaceSupport)
		}
	}

    /*
     * Register core DSL DOM Builders
     */
	private void registerBuilders() {
		domBuilders["org.springframework.integration.dsl.groovy.AbstractChannel"]=new ChannelDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.SimpleEndpoint"]=new SimpleEndpointDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.Aggregator"]=new AggregatorDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.MessageFlow"]=new MessageFlowDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.RouterComposition"]=new RouterDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.Poller"]=new GenericDomBuilder(this,'poller')
		domBuilders["org.springframework.integration.dsl.groovy.ChannelInterceptor"]=new ChannelInterceptorDomBuilder(this)
		domBuilders["org.springframework.integration.dsl.groovy.Wiretap"]=new GenericDomBuilder(this,'wire-tap')
		domBuilders["org.springframework.integration.dsl.groovy.XMLBean"]=new SpringXMLBuilder()
	}

	/**
	 * Get the DOM Builder registered for an IntegrationComponent by its class
	 * @param clazz
	 * @return the builder or null if one does not exist
	 */
	IntegrationComponentDomBuilder domBuilder(Class clazz) {		
		def builder = null
		if (AbstractChannel.isAssignableFrom(clazz)) {
			builder = domBuilders[AbstractChannel.class.name]			
		} else if (SimpleEndpoint.isAssignableFrom(clazz)) {
			builder =  domBuilders[clazz.name] ?: domBuilders[SimpleEndpoint.class.name]
		} else {
			builder = domBuilders[clazz.name]
		}		
		if (logger.isDebugEnabled()) {
			logger.debug("returning DOMBuilder ${builder?.class?.name} for ${clazz.name}")
		  }
		  
		builder
	}
	
	/**
	 * Get the DOM Builder assigned to an Integration Component by 
	 * its class name
	 * @param className
	 * @return the builder or null if one does not exist
	 */
	IntegrationComponentDomBuilder domBuilder(String className) {
			domBuilders[className]
	}
	
	/**
	 * Return the DOM builder for an IntegrationComponent instance
	 * @param component the instance
	 * @return the builder or null if one does not exist
	 */
	IntegrationComponentDomBuilder domBuilder(Object component) {
		domBuilder(component.class)
	}
	
	protected void registerDomBuilder(Class clazz, IntegrationComponentDomBuilder domBuilder) {
		domBuilder.integrationDomSupport = this;
		domBuilders[clazz.name] = domBuilder
	}
	
	/**
	 * Translate DSL context to XML
	 * @param integrationContext the DSL IntegrationContext
	 * @return Spring XML bean definitions
	 */
	def translateToXML(integrationContext) {

		resolveXMLBeanNamespaces(integrationContext.components.findAll {it instanceof XMLNamespace} )

		integrationContext.messageFlows?.each {messageFlow ->
			domBuilder(messageFlow).resolveMessageFlowChannels(messageFlow)
		}

		def markupBuilder = new StreamingMarkupBuilder()
		def writer = markupBuilder.bind { builder ->
			namespaces <<  namespaceSupport.namespaceDeclarations
			beans(namespaceSupport.schemaLocationDeclaration() ) {
				integrationContext.components.each {component ->
					if (logger.isDebugEnabled()) {
						logger.debug("building ${component}")
					}
					domBuilder(component)?.build(builder, integrationContext.applicationContext, component)
				}
			}
		}

		def xml =  XmlUtil.serialize(writer)

		if (logger.isDebugEnabled()) {
			logger.debug(xml)
		}
		xml
	}

	private resolveXMLBeanNamespaces(xmlNamespaces) {
		xmlNamespaces?.each { namespace->
			namespace.name.split(',').each { ns->
				if (logger.isDebugEnabled()) {
					logger.debug("adding namespace for $ns")
				}
				namespaceSupport.addDefaultNamespace(ns)
			}
		}
	}
}
