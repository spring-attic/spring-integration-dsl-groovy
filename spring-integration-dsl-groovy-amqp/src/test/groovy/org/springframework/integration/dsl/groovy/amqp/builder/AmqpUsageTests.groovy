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
package org.springframework.integration.dsl.groovy.amqp.builder
import static org.junit.Assert.*
import org.junit.Test
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
/**
 * @author David Turanski
 *
 */
class AmqpUsageTests {
	def builder = new IntegrationBuilder('amqp')
	
	@Test
	void testConnectionFactory() {
		builder.doWithSpringIntegration {
				rabbitConnectionFactory()
		}
		
		builder.applicationContext.getBean('connectionFactory')
	}
	
	@Test
	void testDirectExchange() {
		builder.doWithSpringIntegration {
			rabbitQueue('q1')
			rabbitQueue('q2')
			rabbitQueue('q3')
			rabbitDirectExchange('myExchange',bindings:[[key:'t1',queue:'q1'],[key:'t2',queue:'q2'],[queue:'q3']])
		}
		
		builder.applicationContext.getBean('myExchange')
	}
	
	@Test
	void testAdmin() {
		builder.doWithSpringIntegration {
			rabbitConnectionFactory('myConnectionFactory')
			rabbitAdmin(connectionFactory:'myConnectionFactory')
		}
	}
}
