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
import static org.junit.Assert.*
import org.junit.Test
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.message.GenericMessage
import org.springframework.integration.router.ClosureInvokingRouter;
import org.springframework.integration.support.channel.ChannelResolver


/**
 * @author David Turanski
 * 
 */
public class ClosureInvokingRouterTests {

	@Test
	void testSimpleRouter() {
		 def channel = new DirectChannel()
		 
		 def processor = new ClosureInvokingMessageProcessor({ payload ->
			 payload.class.name})
		def router = new ClosureInvokingRouter(processor)
		router.setChannelMappings(["java.lang.String": "stringChannel"])
		router.setChannelResolver( new ChannelResolver(){
			   MessageChannel resolveChannelName(String channelName) {
				   println channelName
				   return channel
			   }
			})
		
		router.handleMessage(new GenericMessage("hello"))
		
	}
}
