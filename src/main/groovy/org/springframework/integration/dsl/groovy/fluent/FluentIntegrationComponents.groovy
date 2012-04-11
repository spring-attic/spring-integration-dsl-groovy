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
package org.springframework.integration.dsl.groovy.fluent

/**
 * @author David Turanski
 *
 */
class FluentTransformer implements ClosureUser {
	private def closure 
	def using(Closure c) {
		 this.closure = c
		 this
	}
}

class FluentRouter extends AbstractClosureAwareEndpoint implements Stage1Router {
	
	Stage2Router onPayloadType(Class clazz) {
		new Stage2Router(endpoint:this)
	}
	
	Stage2Router onHeaderValue(obj) {
		new Stage2Router(endpoint:this)
	}
	
	Stage2Router onValueOf(Closure c) {
		this.closure = c
		new Stage2Router(endpoint:this)
	}
}

class Stage2Router extends AbstractClosureAwareEndpoint implements Stage2Component {
	private AbstractClosureAwareEndpoint endpoint
	 

	/* (non-Javadoc)
	 * @see org.springframework.integration.dsl.groovy.fluent.Stage2Component#where(java.lang.Object)
	 */
	public Object where(Map<String,Object> attributes) {
		// TODO Auto-generated method stub
		return this;
	}
	
}

class FluentServiceActivator {

}

abstract class AbstractClosureAwareEndpoint {
	private def name
	private def closure
}

abstract class AbstractClosureUsingEndpoint extends AbstractClosureAwareEndpoint implements ClosureUser {
	@Override 
	def using(Closure c) {
		this.closure = c
		this
   }
}

 
 

interface ClosureUser {
	def using(Closure c)
}

interface Stage1Router {
	Stage2Router onPayloadType(Class clazz)
	
	Stage2Router onHeaderValue(obj) 
	
	Stage2Router onValueOf(Closure c)  
}

interface Stage2Component {
	def where(Map<String,Object> attributes)
}