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
package org.springframework.integration.dsl.groovy.builder.dom;

/**
 * @author David Turanski
 *
 */
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

import org.springframework.context.support.GenericXmlApplicationContext
import org.springframework.core.io.ByteArrayResource;
import org.springframework.integration.Message
import org.springframework.integration.MessageChannel
import org.springframework.integration.MessagingException
import org.springframework.integration.core.MessageHandler
import org.springframework.integration.core.SubscribableChannel
import org.springframework.integration.dsl.groovy.ClosureInvokingTransformer
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.dsl.groovy.Transformer

import org.junit.*
import static org.junit.Assert.*

public class XmlDomTests {
    
   def xmlBuilder = new StreamingMarkupBuilder()
   
   
   @Test
   void testSimpleTransformer() {
	   def ac = new GenericXmlApplicationContext()
	   def transformer = new Transformer(inputChannel:'inputChannel',outputChannel:'outputChannel',action:{it.toUpperCase()})
	   
	   def beanDefinitionHolders = []
	   
	    def integrationSupport = new IntegrationMarkupSupport()
		def writer = xmlBuilder.bind { builder->
		    namespaces << IntegrationMarkupSupport.coreNamespaces() 
			beans( integrationSupport.schemaLocations() ) {
				integrationSupport.createEndpoint(ac, builder, transformer)
				'si:channel'(id:'outputChannel')
			}
		    
	   }
		assert writer instanceof Writable
		//println XmlUtil.serialize(writer)
		
		def stringWriter = new StringWriter()
		writer.writeTo(stringWriter)
		
		def xml =  stringWriter.toString()	
		ac.load(new ByteArrayResource(xml.getBytes() ))	
		ac.refresh()
		testStringXmlApplicationContext(ac)
   }
   
   
   
   
   //@Test
   void testAddNamespaces() {
	   def integrationSupport = new IntegrationMarkupSupport()
	   def writer = xmlBuilder.bind { builder->
		   namespaces << IntegrationMarkupSupport.coreNamespaces()
		   namespaces << integrationSupport.addIntegrationNamespace('jms')
		   namespaces << integrationSupport.addIntegrationNamespace('http')
		   beans( integrationSupport.schemaLocations() ) {
			   'si:transformer'('input-channel':'inputChannel','output-channel':'outputChannel',method:'transform'){
				   bean('class':'org.springframework.integration.dsl.groovy.builder.dom.XmlDomTests.TestClosureInvoker')
			   }
			   'si:channel'(id:'outputChannel')
		   } 
	  }
	   println XmlUtil.serialize(writer)
   }
   
   void testStringXmlApplicationContext(ac) {
	    
	 
	   
	   MessageChannel inputChannel = ac.getBean("inputChannel")
	   SubscribableChannel outputChannel = ac.getBean("outputChannel")
	   outputChannel.subscribe (new MessageHandler() {
		   void handleMessage(Message msg) throws MessagingException {
			   println msg
		   };
	   })
	   inputChannel.send(new GenericMessage("hello"))
	   
   }
   
   
   public static class TestClosureInvoker {
	   public Message transform (Message msg) {
		   new ClosureInvokingTransformer({it.toUpperCase()}).transform(msg)
	   }
   }
   
}
