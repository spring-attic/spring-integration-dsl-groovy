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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.MessageTransformingHandler;

/**
 * @author David Turanski
 * 
 */
public class ClosureInvokingTransformerTests {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSimpleMessageInvocation() {
		org.springframework.integration.transformer.Transformer messageClosureInvoker = ClosureInvokerTests
				.getSimpleMessageInvoker();
		Message message = new GenericMessage("hello");
		assertEquals("hello", messageClosureInvoker.transform(message).getPayload());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSimpleMessagePayloadInvocation() {
		org.springframework.integration.transformer.Transformer messageClosureInvoker = ClosureInvokerTests
				.getSimpleMessagePayloadInvoker();
		Message message = new GenericMessage("hello");
		assertEquals("HELLO", messageClosureInvoker.transform(message).getPayload());
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testSimpleMessageHeadersInvocation() {
		org.springframework.integration.transformer.Transformer messageClosureInvoker = ClosureInvokerTests
				.getSimpleMessageHeaderInvoker();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("foo", "foo");
		headers.put("bar", "bar");
		Message message = MessageBuilder.withPayload("hello").copyHeaders(headers).build();
		assertEquals("foobar", messageClosureInvoker.transform(message).getPayload());
	}

	@Test
	public void testConsumerEndpointFactoryBean() throws Exception {
		DirectChannel channel = new DirectChannel();
		channel.setComponentName("inputChannel");
		DirectChannel outputChannel = new DirectChannel();
		outputChannel.setComponentName("outputChannel");
		outputChannel.subscribe(new MessageHandler() {

			public void handleMessage(Message<?> message) throws MessagingException {
				assertEquals("HELLO", message.getPayload());
			}
		});

		ConsumerEndpointFactoryBean cefb = new ConsumerEndpointFactoryBean();
		cefb.setInputChannel(channel);
		cefb.setBeanName("endpoint");
		MessageTransformingHandler handler = new MessageTransformingHandler(
				ClosureInvokerTests.getSimpleMessagePayloadInvoker());
		handler.setOutputChannel(outputChannel);
		handler.setBeanName("handler");
		handler.afterPropertiesSet();
		cefb.setHandler(handler);
		cefb.setBeanFactory(new GenericApplicationContext().getDefaultListableBeanFactory());
		cefb.afterPropertiesSet();
		cefb.start();
		
		Message<String> message = new GenericMessage<String>("hello");
		channel.send(message);
	}
}
