/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.integration.dsl.groovy

import groovy.transform.CompileStatic

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel

/**
 *
 * @author David Turanski
 * @author Gary Russell
 *
 */
@CompileStatic
class ClosureInvokingMessageProcessor {

	protected Log logger = LogFactory.getLog(this.class)

	protected final Class parameterType

	protected final Closure closure

	ClosureInvokingMessageProcessor(Closure closure) {
		assert closure.parameterTypes.size() == 1, 'Closure must specify exactly one parameter'
		this.closure = closure.clone() as Closure
		this.parameterType = closure.parameterTypes[0]
	}

	Object processMessage(Message message) {
		Object result = null
		if (parameterType == Message) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message')
			}
			result = this.closure.call(message)
		}
		else if (parameterType == Map && !(message.payload instanceof Map)) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message headers')
			}
			result = this.closure.call(message.headers)
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message payload')
			}

			result = this.closure.call(message.payload)
		}

		result instanceof GString? result.toString(): result
	}
}

/**
 *
 * @author David Turanski
 *
 */
class MultiMessageParameterTransformer {

	private final boolean closureExpectsMessages

	private final boolean closureParameterIsArray

	MultiMessageParameterTransformer(Closure closure) {
		def method = closure.class.methods.find { Method method -> method.name == 'call' }
		if (method.genericParameterTypes) {
			def ptype = method.genericParameterTypes[0]
			if (ptype instanceof ParameterizedType) {
				closureExpectsMessages = Message.isAssignableFrom(ptype.actualTypeArguments[0] as Class)
			}
			else if (ptype instanceof GenericArrayType) {
				closureParameterIsArray = ptype instanceof Object[]
				closureExpectsMessages = false

			}
			else {
				closureParameterIsArray = (ptype as Class).array
				closureExpectsMessages = Message == (ptype as Class).componentType
			}
		}
	}

	@CompileStatic
	def mapClosureArg(List<Message<?>> list) {
		boolean listContainsMessages = list?.get(0) instanceof Message

		def transformedArg = closureExpectsMessages ? list : listContainsMessages ? list.collect { Message<?> m -> m.payload } : list
		if (closureParameterIsArray) {
			transformedArg = closureExpectsMessages ? transformedArg as Message[] : transformedArg as Object[]
		}
		transformedArg
	}
}

@CompileStatic
class MultiMessageClosureInvoker extends ClosureInvokingMessageProcessor {

	private final MultiMessageParameterTransformer multiMessageParameterTransformer

	MultiMessageClosureInvoker(Closure closure) {
		super(closure)
		multiMessageParameterTransformer = new MultiMessageParameterTransformer(closure)
	}

	def mapClosureArg(List<Message> list) {
		multiMessageParameterTransformer.mapClosureArg(list)
	}
}
/**
 *
 * @author David Turanski
 *
 */
@CompileStatic
class ClosureInvokingListProcessor extends MultiMessageClosureInvoker {

	ClosureInvokingListProcessor(Closure closure) {
		super(closure)
	}

	def processList(List list) {
		def arg = mapClosureArg(list)
		this.closure.call(arg)
	}
}

/**
 *
 * @author David Turanski
 *
 */
@CompileStatic
class ClosureInvokingReleaseStrategy extends MultiMessageClosureInvoker {

	ClosureInvokingReleaseStrategy(Closure closure) {
		super(closure)
	}

	Boolean canRelease(List items) {
		def arg = mapClosureArg(items)
		this.closure.call(arg)
	}
}

@CompileStatic
class ClosureInvokingChannelInterceptor implements org.springframework.messaging.support.ChannelInterceptor {

	protected Log logger = LogFactory.getLog(this.class)

	private final org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor

	ClosureInvokingChannelInterceptor(org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor) {
		this.interceptor = interceptor
	}

	Message<?> preSend(Message message, MessageChannel channel) {
		if (!interceptor.preSend) {
			return message
		}

		if (logger.isDebugEnabled()) {
			logger.debug('invoking preSend closure')
		}
		def result = execClosure(interceptor.preSend, message, channel)
		if (result && !(result instanceof Message)) {
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		result as Message
	}

	void postSend(Message message, MessageChannel channel, boolean sent) {
		if (!interceptor.postSend) {
			return
		}
		if (logger.isDebugEnabled()) {
			logger.debug('invoking postSend closure')
		}

		execClosure(interceptor.postSend, message, channel, sent)
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preReceive(org.springframework.integration.MessageChannel)
	 */

	boolean preReceive(MessageChannel channel) {
		interceptor.preReceive ? interceptor.preReceive.call(channel) as boolean : true
	}

	/* (non-Javadoc)
	 * @see ChannelInterceptor#postReceive(Message, MessageChannel)
	 */

	Message<?> postReceive(Message message, MessageChannel channel) {
		if (!interceptor.postReceive) {
			return message
		}

		def result = execClosure(interceptor.postReceive, message, channel)
		if (result && !(result instanceof Message)) {
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		result as Message
	}

	void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
		if (interceptor.afterSendCompletion) {
			execCompletionClosure(interceptor.afterSendCompletion, message, channel, ex, sent)
		}
	}

	void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
		if (interceptor.afterReceiveCompletion) {
			execCompletionClosure(interceptor.afterReceiveCompletion, message, channel, ex)
		}
	}


	private execClosure(Closure interceptClosure, Message message, MessageChannel channel, sent = null) {
		def paramTypes = interceptClosure.parameterTypes
		def numParams = paramTypes.size()

		def maxParams = sent ? 3 : 2
		assert numParams <= maxParams, 'channel intercept closure has too many parameters: $numParams'

		def result

		if (numParams == 0) {
			result = interceptClosure.call()
		}
		else if (numParams == 1) {
			result = execSingleParamClosure( interceptClosure, paramTypes[0], message, channel, sent)

		}
		else if (numParams == 2) {
			result = interceptClosure.call(message, channel)
		}
		else if (numParams == 3) {
			result = interceptClosure.call(message, channel, sent)
		}

		result
	}

	private execSingleParamClosure(Closure interceptClosure, Class<?> parameterType, Message message,
								   MessageChannel channel, sent = null, Exception ex = null) {
		def result
		if (parameterType == Message) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message')
			}
			result = interceptClosure.call(message)
		}
		else if (parameterType == Map && !(message.payload instanceof Map)) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message headers')
			}
			result = interceptClosure.call(message.headers)
		}
		else if (parameterType == MessageChannel) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message headers')
			}
			result = interceptClosure.call(channel)
		}
		else if (parameterType == Exception && !(message.payload instanceof Exception)) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on Exception')
			}
			result = interceptClosure.call(ex)
		}
		else if (parameterType == boolean && !(message.payload instanceof boolean)) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message sent')
			}
			result = interceptClosure.call(sent)
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message payload')
			}
			result = interceptClosure.call(message.payload)
		}

		result
	}

	private execCompletionClosure(Closure interceptClosure, Message message, MessageChannel channel, Exception ex,
								  sent = null) {
		def paramTypes = interceptClosure.parameterTypes
		def numParams = paramTypes.size()

		def maxParams = sent ? 4 : 3
		assert numParams <= maxParams, 'channel intercept closure has too many parameters: $numParams'

		if (numParams == 0) {
			interceptClosure.call()
		}
		else if (numParams == 1) {
			execSingleParamClosure interceptClosure, paramTypes[0], message, channel, sent

		}
		else if (numParams == 2) {
			interceptClosure.call(message, channel)
		}
		else if (numParams == 3) {
			interceptClosure.call(message, channel, ex)
		}
		else {
			interceptClosure.call(message, channel, sent, ex)
		}

	}

}
