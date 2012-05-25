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

	/**
	 * Translate DSL context to XML
	 * @param integrationContext
	 * @return Spring XML bean definitions
	 */

	private final domBuilders = [:]

	IntegrationDomSupport() {
		registerBuilders()
	}
	IntegrationDomSupport(List<AbstractIntegrationBuilderModuleSupport> moduleSupportInstances) {
		this()
		moduleSupportInstances?.each {AbstractIntegrationBuilderModuleSupport moduleSupport ->
			moduleSupport.registerDomBuilders(this)
		}
	}


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

	def domBuilder(Object component) {
		def builder = null
		if (component instanceof String) {
			builder = domBuilders[component]
		}
		else if (component instanceof AbstractChannel) {
			builder = domBuilders[AbstractChannel.class.name]
		} else if (component instanceof SimpleEndpoint) {
			builder =  domBuilders[component.class.name] ?: domBuilders[SimpleEndpoint.class.name]
		} else {
			builder = domBuilders[component.class.name]
		}
		builder
	}

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
