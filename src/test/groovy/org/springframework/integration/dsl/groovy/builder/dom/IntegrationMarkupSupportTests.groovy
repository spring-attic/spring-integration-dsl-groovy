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
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

import org.junit.*
import static org.junit.Assert.*

public class IntegrationMarkupSupportTests {
    
   def integrationMarkupSupport = new IntegrationMarkupSupport()
   def builder = new IntegrationBuilder()
   
   @Test
   void testResolveMessageFlowChannels() {
	   def flow =   
	   builder.messageFlow {
	    transform('t',evaluate:{payload->payload.toUpperCase()})
	    filter('f',evaluate:{payload-> payload=="HELLO"},outputChannel:'toHandler',discardChannel:'discardChannel')
	    handle('sa1',inputChannel:'discardChannel',evaluate:{println it})
		handle('sa2',inputChannel:'toHandler', evaluate:{payload})
	   }
	   
	   integrationMarkupSupport.resolveMessageFlowChannels(flow)
	   
	   def component
	   
	   component = flow.components.first()
	   assert component.inputChannel == flow.inputChannel
	   assert component.outputChannel == 'from.t.to.f'
	   
	   component = flow.components.last()
	   assert component.outputChannel == flow.outputChannel
	   
	   component = flow.components.find({it.name == 'sa1'})
	   assert component.inputChannel == 'discardChannel'
	   assert component.outputChannel == 'toHandler'
	   
   } 
  
}
