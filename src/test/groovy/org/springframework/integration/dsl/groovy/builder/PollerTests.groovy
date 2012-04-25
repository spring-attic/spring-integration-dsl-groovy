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
package org.springframework.integration.dsl.groovy.builder;
import org.junit.Test
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.PublishSubscribeChannel
import org.springframework.integration.channel.QueueChannel
import static org.junit.Assert.*
/**
 * @author David Turanski
 *
 */
 
public class PollerTests {
	IntegrationBuilder builder = new IntegrationBuilder()
	 
	@Test
	void testRefToTopLevelPoller() {
		 def ic = builder.doWithSpringIntegration {
			 poll('cronPoller',cron:"*/10 * * * * MON-FRI")
			 queueChannel('transformer.inputChannel')
			 transform('transformer',evaluate:{it},poller:"cronPoller") 
		 }
		 
		 def ac = ic.createApplicationContext()	 
	}
	
	@Test
	void testInnerPoller() {
		 def ic = builder.doWithSpringIntegration {
			
			 queueChannel('transformer.inputChannel')
			 transform('transformer',evaluate:{it}){
				 poll(cron:"*/10 * * * * MON-FRI")
			 }
		 }
		 
		 def ac = ic.createApplicationContext()
	}
}
