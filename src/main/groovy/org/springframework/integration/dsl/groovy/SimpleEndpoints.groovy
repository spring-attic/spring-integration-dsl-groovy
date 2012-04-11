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

/**
 * @author David Turanski
 *
 */
class SimpleEndpoint extends IntegrationComponent {
	Object target
	Closure action
	String inputChannel
	String outputChannel

	SimpleEndpoint(String name, Object target){
		this()
		if (name) {
			this.name = name
		}
	
		this.target=target
	}
	
	SimpleEndpoint(){
		name = defaultNamePrefix() + "_" + UUID.randomUUID().toString().substring(0, 8)
	}
	

	protected String defaultNamePrefix(){
	}
	

	@Override
	String toString() {
		name
	}
}

class ServiceActivator extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$sa'
	}
}

class MessagingBridge extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$br'
	}
}

class Enricher extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$enr'
	}
}

class Transformer extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$xfmr'
	}
}

class Filter extends SimpleEndpoint {
	String discardChannel
	protected String defaultNamePrefix(){
		'$flt'
	}
}

class Router extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$rtr'
	}
}
 
class Splitter extends SimpleEndpoint {
	protected String defaultNamePrefix(){
		'$splt'
	}
}




