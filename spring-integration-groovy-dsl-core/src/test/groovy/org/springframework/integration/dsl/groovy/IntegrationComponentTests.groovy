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
package org.springframework.integration.dsl.groovy
import org.junit.Test
/**
 * @author David Turanski
 *
 */
class IntegrationComponentTests {
	@Test
	void testValidateAttributes() {
		def test = new TestComponent()
		def validationContext = test.validateAttributes(['bar':'valbar','baz':'valbaz','bag':'valbag','foo':'valfoo'])
		assert !validationContext.hasErrors ,'should be valid'
		
		validationContext = test.validateAttributes(['bar':'valbar'])
		assert validationContext.hasErrors, 'should not be valid'
	    assert validationContext.errorMessage == "'test' is missing the following required attributes:baz, bag"
		
		 validationContext = test.validateAttributes(['bar':'valbar','barx':'valbarx'])
		 assert validationContext.errorMessage == \
"'test' is missing the following required attributes:baz, bag\n'test' contains the following invalid attributes:barx"
		 
		validationContext = test.validateAttributes(['bar':'valbar','baz':'valbaz','bag':'valbag','barx':'valbarx'])
		assert validationContext.errorMessage == "'test' contains the following invalid attributes:barx" 
	}
	
	@Test 
	void testAttributesRequiresOneOf() {
		def test = new TestComponent2()
		def validationContext = test.validateAttributes(['bar':'valbar'])
		assert !validationContext.hasErrors ,'should be valid'
		
		validationContext = test.validateAttributes(['bar':'valbar','baz':'valbaz','bazx':'valbazx'])
		assert validationContext.errorMessage == "'test2' contains the following invalid attributes:bazx"
	}
	
	@Test
	void testMutuallyExclusiveAttributes(){
		def test = new TestComponent2()
		def validationContext = test.validateAttributes(['bar':'barval','foo1':'valfoo1','foo4':'valfoo4'])
		assert !validationContext.hasErrors ,'should be valid'
		
		validationContext = test.validateAttributes(['bar':'barval','foo1':'valfoo1','foo2':'valfoo2'])
		assert validationContext.errorMessage == "'test2' contains mutually exclusive attributes [foo1, foo2]"
	}
	
	@Test
	void testNullOrEmptyAttributes() {
		def test = new TestComponent2()
		def validationContext = test.validateAttributes(null)
		assert validationContext.errorMessage == "'test2' must include at least one of [bar, baz, bag]"
		
		validationContext = test.validateAttributes([:])
		assert validationContext.errorMessage == "'test2' must include at least one of [bar, baz, bag]"
	}
		
}

class TestComponent extends IntegrationComponent {
	static requiredAttributes = ['bar','baz','bag']
	static invalidAttributes = ['barx','bazx','bagx']
	TestComponent() {	 
		builderName = 'test'
	}
}

class TestComponent2 extends IntegrationComponent {
	static attributesRequiresAnyOf = ['bar','baz','bag']
	static invalidAttributes = ['barx','bazx','bagx']
	static mutuallyExclusiveAttributes = [['foo1','foo2','foo3'],['foo4','foo5']]
	TestComponent2() {
		builderName = 'test2'
	}
}
