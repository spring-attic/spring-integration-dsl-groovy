/*
 * Copyright 2002-2015 the original author or authors.
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

import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.support.GenericMessage

/**
 * @author David Turanski
 *
 */
class ClosureInvokingChannelInterceptorTests {

	@Test
	void testSimplePreSendClosure() {
		def message = new GenericMessage('hello')
		def channel = new DirectChannel()
		boolean afterSendCompletion
		def channelInterceptor = new ChannelInterceptor(preSend: { payload -> "$payload, world" },
				afterSendCompletion: { m -> afterSendCompletion = true })
		def interceptor = new ClosureInvokingChannelInterceptor(channelInterceptor)
		channel.interceptors = [interceptor]

		def count = 0
		channel.subscribe(new MessageHandler() {

			void handleMessage(Message<?> msg) throws MessagingException {
				count++
				assert msg.payload == 'hello, world'
			}
		}
		)

		channel.send(message)
		assert count
		assert afterSendCompletion
	}

	@Test
	void testSimplePostSendClosure() {
		def message = new GenericMessage('hello')
		def channel = new DirectChannel()
		def count = 0
		def channelInterceptor = new ChannelInterceptor(postSend: { msg, chnl, sent ->
			assert msg.payload == 'hello'
			assert chnl.componentName == 'channel'
			assert sent
			count++
		})
		def interceptor = new ClosureInvokingChannelInterceptor(channelInterceptor)
		channel.setInterceptors([interceptor])
		channel.setComponentName('channel')

		channel.subscribe(new MessageHandler() {

			void handleMessage(Message<?> msg) throws MessagingException {
			}
		}
		)

		channel.send(message)
		assert count
	}

	@Test
	void testReceiveClosures() {
		def message = new GenericMessage('hello')
		def channel = new QueueChannel()
		boolean afterReceiveCompletion
		def channelInterceptor = new ChannelInterceptor(afterReceiveCompletion: { m, c, e -> afterReceiveCompletion = true })
		def interceptor = new ClosureInvokingChannelInterceptor(channelInterceptor)
		channel.interceptors = [interceptor]

		channel.send(message)
		def receive = channel.receive(10000)
		assert receive
		assert afterReceiveCompletion
	}

}