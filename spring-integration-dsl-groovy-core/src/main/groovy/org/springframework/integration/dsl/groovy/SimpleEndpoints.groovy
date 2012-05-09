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
	static mutuallyExclusiveAttributes=[['inputChannel','input-channel'],['ref','action','evaluate']]
	String inputChannel
	def poller
	
	SimpleEndpoint(){
		name = defaultName(defaultNamePrefix())
	}
	

	protected String defaultNamePrefix(){
	}
	

	@Override
	String toString() {
		name
	}
}

class MessageProducingEndpoint extends SimpleEndpoint {
	static mutuallyExclusiveAttributes= SimpleEndpoint.mutuallyExclusiveAttributes << [['outputChannel','output-channel']]
	
	static requiresReply = true
	boolean linkToNext = true
	String outputChannel
}

class ServiceActivator extends MessageProducingEndpoint {
	static attributesRequiresOneOf = ['ref','action','evaluate']
	Closure action
	static requiresReply = false
	protected String defaultNamePrefix(){
		'$sa'
	}
}

class MessagingBridge extends MessageProducingEndpoint {
	protected String defaultNamePrefix(){
		'$br'
	}
}


class Transformer extends MessageProducingEndpoint {
	static attributesRequiresOneOf = ['ref','action','evaluate']
	Closure action
	protected String defaultNamePrefix(){
		'$xfmr'
	}
}

class Filter extends MessageProducingEndpoint {
	static attributesRequiresOneOf = ['ref','action','evaluate']
	Closure action
	String discardChannel
	protected String defaultNamePrefix(){
		'$flt'
	}
}

class Splitter extends MessageProducingEndpoint {
	Closure action
	protected String defaultNamePrefix(){
		'$splt'
	}
}

class Aggregator extends MessageProducingEndpoint {
	Closure action
	Closure releaseStrategy
	Closure correlationStrategy
	protected String defaultNamePrefix(){
		'$aggr'
	}
}

class Bridge extends MessageProducingEndpoint {
	protected String defaultNamePrefix(){
		'$brdg'
	}
}


