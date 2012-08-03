package org.springframework.integration.dsl.groovy






import java.lang.reflect.ParameterizedType

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.integration.Message
import org.springframework.integration.MessageChannel
import org.springframework.integration.support.MessageBuilder

/**
 *
 * @author David Turanski
 *
 */
class ClosureInvokingMessageProcessor  {
	protected  Log logger = LogFactory.getLog(this.class)
	protected final Class parameterType
	protected final Closure closure

	ClosureInvokingMessageProcessor(Closure closure){
		assert closure.parameterTypes.size() == 1, 'Closure must specify exactly one parameter'
		this.closure = closure.clone()
		this.parameterType = closure.parameterTypes[0]
	}

	Object processMessage(Message message) {
		Object result = null
		if (parameterType == Message) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message')
			}
			result =  this.closure.doCall(message)
		}

		else if (parameterType == Map && !(message.payload instanceof Map)) {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message headers')
			}
			result =  this.closure.doCall(message.headers)
		}

		else {
			if (logger.isDebugEnabled()) {
				logger.debug('invoking closure on message payload')
			}

			result =  this.closure.doCall(message.payload)
		}

		result
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
		def method = closure.class.methods.find { it.name == 'call' }
		if (method.genericParameterTypes) {
			def ptype =  method.genericParameterTypes[0]
			if (ptype instanceof ParameterizedType) {
				closureExpectsMessages = Message.isAssignableFrom(ptype.actualTypeArguments[0])
			} else {
				closureParameterIsArray = ptype.isArray()
				closureExpectsMessages =  closureParameterIsArray && Message == ptype.componentType
			}
		}
	}

	def mapClosureArg(List list){
		boolean listContainsMessages = list?.get(0) instanceof Message

		def transformedArg = closureExpectsMessages  ? list: listContainsMessages  ? list*.payload : list
		if (closureParameterIsArray) {
			transformedArg = closureExpectsMessages? transformedArg as Message[] : transformedArg as Object[]
		}
		transformedArg
	}
}

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
class ClosureInvokingListProcessor extends MultiMessageClosureInvoker {

	ClosureInvokingListProcessor(Closure closure) {
		super(closure)
	}

	def processList(List list){
		def arg = mapClosureArg(list)
		this.closure.doCall(arg)
	}
}


/**
 *
 * @author David Turanski
 *
 */

class ClosureInvokingReleaseStrategy extends MultiMessageClosureInvoker {

	ClosureInvokingReleaseStrategy(Closure closure) {
		super(closure)
	}

	Boolean canRelease(List items){
		def arg = mapClosureArg(items)
		this.closure.doCall(arg)
	}
}

class ClosureInvokingChannelInterceptor implements org.springframework.integration.channel.ChannelInterceptor {
	protected  Log logger = LogFactory.getLog(this.class)
	private final org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor

	ClosureInvokingChannelInterceptor(org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor){
		this.interceptor = interceptor
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preSend(org.springframework.integration.Message, org.springframework.integration.MessageChannel)
	 */
	Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (!interceptor.preSend){
			return message
		}

		if (logger.isDebugEnabled()){
			logger.debug('invoking preSend closure')
		}
		def result = execClosure(interceptor.preSend, message,channel)
		if (result && !(result instanceof Message)){
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		result
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#postSend(org.springframework.integration.Message, org.springframework.integration.MessageChannel, boolean)
	 */
	void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		if (!interceptor.postSend){
			return
		}
		if (logger.isDebugEnabled()){
			logger.debug('invoking postSend closure')
		}

		execClosure(interceptor.postSend, message,channel,sent)
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preReceive(org.springframework.integration.MessageChannel)
	 */
	boolean preReceive(MessageChannel channel) {
		interceptClosure.preReceive ? interceptClosure.prereceive.doCall(channel) : true
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#postReceive(org.springframework.integration.Message, org.springframework.integration.MessageChannel)
	 */
	Message<?> postReceive(Message<?> message, MessageChannel channel) {
		if (!interceptor.postReceive){
			return message
		}

		def result = execClosure(interceptor.postReceive, message,channel)
		if (result && !(result instanceof Message)){
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		result
	}

	private Object execClosure(Closure interceptClosure, Message<?>message, MessageChannel channel, sent = null){
		def paramTypes = interceptClosure.parameterTypes
		def numParams = paramTypes.size()

		def maxParams = sent ? 3 : 2
		assert numParams <= maxParams, 'channel intercept closure has too many parameters: $numParams'

		def result

		if (numParams == 0){
			result = interceptClosure.doCall()
		}
		else if (numParams == 1){
			def parameterType = paramTypes[0]
			if (parameterType == Message){
				if (logger.isDebugEnabled()) {
					logger.debug('invoking closure on message')
				}
				result = interceptClosure.doCall(message)
			} else if (parameterType == Map && !(message.payload instanceof Map)) {
				if (logger.isDebugEnabled()) {
					logger.debug('invoking closure on message headers')
				}
				result =  interceptClosure.doCall(message.headers)
			} else if (parameterType == MessageChannel) {
				if (logger.isDebugEnabled()) {
					logger.debug('invoking closure on message headers')
				}
				result =  interceptClosure.doCall(channel)
			}
			else if (parameterType == boolean && !(message.payload instanceof boolean)) {
				if (logger.isDebugEnabled()) {
					logger.debug('invoking closure on message sent')
				}
				result =  interceptClosure.doCall(sent)
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug('invoking closure on message payload')
				}
				result =  interceptClosure.doCall(message.payload)
			}
		} else if (numParams == 2){
			result =  interceptClosure.doCall(message,channel)
		} else if (numParams == 3){
			result =  interceptClosure.doCall(message,channel,sent)
		}

		result
	}
}