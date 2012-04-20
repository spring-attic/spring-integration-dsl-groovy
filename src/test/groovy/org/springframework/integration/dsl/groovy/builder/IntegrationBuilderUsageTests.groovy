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
import static org.junit.Assert.*
/**
 * @author David Turanski
 *
 */
/*
 * class MyBinding extends Binding {
    def builder
    Object getVariable(String name) {
        return { Object... args ->  builder.invokeMethod(name,args) }
    }   
}

// parse the script and run it against the builder
new File("foo.groovy").withInputStream { input ->
    Script s = new GroovyClassLoader().parseClass(input).newInstance()
    s.binding = new MyBinding(builder:builder)
    s.run()
}
 */
public class IntegrationBuilderUsageTests {
	IntegrationBuilder builder = new IntegrationBuilder()
	
	Script script
	
	@Test
	void test1() {
		new File("src/test/resources/messageflow1.groovy").withInputStream {input->
			script = new GroovyClassLoader().parseClass(input).newInstance()
		}
		def eip = builder.build(script)
		println(eip);
	}
	
	@Test 
	void testSimpleServiceActivator() {
		builder.messageFlow('flow1') {
			handle(action:{})
		}
	}
	
	@Test 
	void testChannels() {
		builder.doWithSpringIntegration {
			 channel('input'){
			 	interceptor(){
					 onPreSend(){}
				 }
			 }
			 channel('output'){
				 
			 }
		}
	}
	
	@Test 
	void testNestedMessageFlows() {
		builder.messageFlow('main') {
			messageFlow('subFlow'){
				transform(evaluate:{payload->payload.toLowerCase()})
			}
		}
	}
	
	@Test
	void testBridge() {
		def ic = builder.doWithSpringIntegration {
			 transform('t1',outputChannel:'t1.out',evaluate:{it.toUpperCase()})
			 bridge(inputChannel:'t1.out', outputChannel:'bridge.out')
			 transform('t2',inputChannel:'bridge.out',evaluate:{it*2})
		}
		
		assert ic.sendAndReceive('t1.inputChannel','Hello') == "HELLOHELLO"
	}
	
}
