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
import groovy.xml.XmlUtil

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

import org.springframework.integration.dsl.groovy.*

/**
 * @author David Turanski
 * Translates Spring Integration DSL model to Spring XML
 */
class IntegrationMarkupSupport {
	private Log logger = LogFactory.getLog(IntegrationMarkupSupport)
	private IntegrationNamespaceSupport namespaceSupport = new IntegrationNamespaceSupport()

	/**
	 * Translate DSL context to XML	
	 * @param integrationContext
	 * @return Spring XML bean definitions
	 */

	private final xmlBuilders = [:]

	IntegrationMarkupSupport() {
		registerBuilders();
	}

    private void registerBuilders() {
		xmlBuilders["org.springframework.integration.dsl.groovy.AbstractChannel"]=new ChannelXmlBuilder(this)
		xmlBuilders["org.springframework.integration.dsl.groovy.SimpleEndpoint"]=new SimpleEndpointXmlBuilder(this)
		xmlBuilders["org.springframework.integration.dsl.groovy.MessageFlow"]=new MessageFlowXmlBuilder(this)
		xmlBuilders["org.springframework.integration.dsl.groovy.RouterComposition"]=new RouterXmlBuilder(this)
		xmlBuilders["org.springframework.integration.dsl.groovy.Poller"]=new GenericXmlBuilder(this,'poller')
	}

	def xmlBuilder(Object component) {
		def builder = null 
		if (component instanceof String) {
			builder = xmlBuilders[component]
		}
		else if (component instanceof AbstractChannel) {
			builder = xmlBuilders[AbstractChannel.class.name]
		} else if (component instanceof SimpleEndpoint) {
			builder =  xmlBuilders[SimpleEndpoint.class.name]
		} else {
			builder = xmlBuilders[component.class.name]
		}
		builder
	}


	def translateToXML(integrationContext) {

		resolveXMLBeanNamespaces(integrationContext.components.findAll {it instanceof XMLNamespace} )

		integrationContext.messageFlows?.each {messageFlow -> 
			xmlBuilder(messageFlow).resolveMessageFlowChannels(messageFlow) 
		}

		def markupBuilder = new StreamingMarkupBuilder()
		def writer = markupBuilder.bind { builder ->
			namespaces <<  namespaceSupport.namespaceDeclarations
			println namespaceSupport.namespaceDeclarations
			beans(namespaceSupport.schemaLocationDeclaration() ) {
				integrationContext.components.each {component ->
					if (component instanceof XMLBean){
						Closure c = component.defn.dehydrate()
						c.delegate = builder
						c.call()
					} 
					else  {
						xmlBuilder(component)?.build(builder, integrationContext.applicationContext, component)
					}
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
