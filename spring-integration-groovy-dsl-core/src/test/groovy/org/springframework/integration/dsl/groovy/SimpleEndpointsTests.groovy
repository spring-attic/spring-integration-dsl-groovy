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
package org.springframework.integration.dsl.groovy;

import static org.junit.Assert.*
import org.junit.Test
import org.springframework.integration.dsl.groovy.builder.FilterFactory
/**
 * @author David Turanski
 *
 */
class SimpleEndpointsTests {
	@Test
	void testSimpleEndpointConstructors () {
		def transformer = new Transformer();
		assert transformer.toString().startsWith('$xfmr_')
		
		def bridge = new MessagingBridge(target:null)
		assert bridge.name.startsWith(bridge.defaultNamePrefix())
	}
	
	@Test
	void testEndpointFactory(){
		def factory = new FilterFactory()
		
		def filter = factory.newInstance(null,null, null,[:])
		assert filter.class == Filter
	}
	
	@Test
	void testComponentProperties() {
		def transformer = new Transformer();
		transformer.foo = 'foo'
		if (transformer.foo == 'bar') {
			
		}
		assert transformer.foo == 'foo'
	}
}
