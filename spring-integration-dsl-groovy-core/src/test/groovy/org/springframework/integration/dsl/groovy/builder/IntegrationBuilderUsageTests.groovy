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

import static org.junit.Assert.*

import org.junit.Test

/**
 * @author David Turanski
 *
 */
class IntegrationBuilderUsageTests {
	IntegrationBuilder builder = new IntegrationBuilder()

	Script script

	@Test
	void test1() {

		def codeSource = new GroovyCodeSource(new File("src/test/resources/messageflow1.groovy"));
		script = new GroovyClassLoader().parseClass(codeSource).newInstance()

		def eip = builder.build(script)
	}

	@Test
	void testBridge() {
		def ic = builder.doWithSpringIntegration {
			transform('t1',outputChannel:'t1.out',{it.toUpperCase()})
			bridge(inputChannel:'t1.out', outputChannel:'bridge.out')
			transform('t2',inputChannel:'bridge.out', {it*2})
		}

		assert ic.sendAndReceive('t1.inputChannel','Hello') == "HELLOHELLO"
	}

	@Test
	void testMergedApplicationContext() {
		IntegrationBuilder builder1 = new IntegrationBuilder()
		builder1.setAutoCreateApplicationContext(false)

		IntegrationBuilder builder2 = new IntegrationBuilder()
		builder2.setAutoCreateApplicationContext(false)

		def ic1 = builder1.doWithSpringIntegration {
            queueChannel('global.out')
			transform('t1',inputChannel:'t1.in',outputChannel:'global.out',{it.toUpperCase()})
		}

		def ic2 = builder2.doWithSpringIntegration {
			transform('t2',inputChannel:'t2.in',outputChannel:'global.out',{it.toUpperCase()})
		}

		def ac = ic2.createApplicationContext([ic1])

		ac.getBean('t1')
		ac.getBean('t1.in')
		ac.getBean('t2')
		ac.getBean('t2.in')
        def queue = ac.getBean('global.out')

        ic1.send('t1.in','hello')
        ic1.send('t2.in','hello')
        2.times {
            assert queue.receive().payload == 'HELLO'
        }
	}


}
