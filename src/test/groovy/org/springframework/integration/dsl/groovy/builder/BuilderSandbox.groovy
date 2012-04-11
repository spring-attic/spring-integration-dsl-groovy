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
package org.springframework.integration.dsl.groovy.builder

import org.springframework.integration.dsl.groovy.Filter

/**
 * @author David Turanski
 *
 */

/*
 * Scala DSL
 *   val messageFlow =
     filter.using { payload: String => payload == "hello" }.where(name="myFilter") -->
     transform.using { m: Message[String] => m.getPayload().toUpperCase() }.where(name="myTrans") -->
     handle.using { m: Message[_] => println(m) }.where(name="myHandle")

    messageFlow.send("hello")
 */

//transform.withName("foo").andFoo("").andBar("").using{. . .}

def builder = new IntegrationBuilder() 
builder.messageFlow {
	
//	filter('myFilter', action: {payload -> payload == "World"}) 
//	transform('myTrans').using { payload -> "Hello " + payload}
	route('myRouter')/*.onHeaderValue('foo')*/ {		
		when('bar') { 
			handle(action:{}) 
		}
		when('zaz'){
			handle(action:{})  
		}
	}
	//handle('myHandler').using{ payload -> println("payload: $payload")}
}

println "======================================================================================="

builder.messageFlow {
	
	//filter('myFilter', action: {payload -> payload == "World"})
	//transform('myTrans').using { payload -> "Hello " + payload}
	route('myRouter1',value:{Map headers-> headers.foo})
	routeOnHeaderValue('myRouter2',value:'foo') {
		when('bar') {
			handle(action:{})
		}
		when('zaz'){
			handle(action:{})
		}
	}
	//handle('myHandler').using{ payload -> println("payload: $payload")}
}


/*builder.create {
	  pubSubChannel("output")
	  filter(name:'myFilter',input:"input",output:"output",action:{payload -> payload == "World"})
}
*/
