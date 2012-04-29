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

class AggregatorTests {


	IntegrationBuilder builder = new IntegrationBuilder()

	@Test
	void testDefaultAggregator() {
		def flow = builder.messageFlow {
		   split()
		   aggregate()
		}

		 
		def result = flow.sendAndReceive([1, 2])
        assert result == [1,2] 
	}
	
	@Test
	void testCustomReleaseStrategy() {
		def flow = builder.messageFlow {
		
		   split()
		   aggregate(releaseStrategy:{ list-> (list.inject(0){sum,item-> sum+item} >= 6) })
		}

		 
		def result = flow.sendAndReceive([1, 2, 3, 4])
		assert result == [1,2,3]
	}
	
	@Test
	void testCustomCorrelationStrategy() {
		def flow = builder.messageFlow(outputChannel:'queueChannel') {
		   queueChannel('queueChannel')
		   split()
		   aggregate(
			     releaseStrategy:{ list-> (list.size() == 2 ) },
			     correlationStrategy:{it % 2 ? 'even' : 'odd' })
		}

		def ac = builder.integrationContext.createApplicationContext()
		def queueChannel = ac.getBean('queueChannel')
		flow.send([1, 2, 3, 4])

		def result = queueChannel.receive()
		assert result.payload == [1,3]
		result = queueChannel.receive()
		assert result.payload == [2,4]
	}
	
}
