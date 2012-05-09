package org.springframework.integration.dsl.groovy

import org.springframework.integration.Message
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log


/**
 * 
 * @author David Turanski
 *
 */
class ClosureInvokingMessageProcessor  {
	protected  Log logger = LogFactory.getLog(this.class)
	private Class parameterType
	protected Closure closure

	ClosureInvokingMessageProcessor(Closure closure){
		assert closure.getParameterTypes().size() == 1, 'Closure must specify exactly one parameter'
		this.closure = closure.clone()
		this.parameterType = closure.getParameterTypes()[0]
	}


	public Object processMessage(Message message) {
		Object result = null
		if (parameterType == Message) {
			if (logger.isDebugEnabled()) {
				logger.debug("invoking closure on message")
			}
			result =  this.closure.doCall(message)
		}

		else if (parameterType == Map && !(message.payload instanceof Map)) {
			if (logger.isDebugEnabled()) {
				logger.debug("invoking closure on message headers")
			}
			result =  this.closure.doCall(message.headers)
		}

		else {
			if (logger.isDebugEnabled()) {
				logger.debug("invoking closure on message payload")
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
class ClosureInvokingListProcessor extends ClosureInvokingMessageProcessor {
	ClosureInvokingListProcessor(Closure closure) {
		super(closure)
	}
    
	def processList(List list){
		if (logger.isDebugEnabled()) {
			logger.debug("invoking closure on list $list")
		}
		def result = this.closure.doCall(list)
		result
	}	
}


/**
 * 
 * @author David Turanski
 *
 */

class ClosureInvokingReleaseStrategy extends ClosureInvokingMessageProcessor {
	ClosureInvokingReleaseStrategy(Closure closure) {
		super(closure)
	}

	public Boolean canRelease(List<?> items){
		this.closure.doCall(items)
	}
}

class ClosureInvokingChannelInterceptor implements org.springframework.integration.channel.ChannelInterceptor {
	protected  Log logger = LogFactory.getLog(this.class)
	private org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor

	ClosureInvokingChannelInterceptor(org.springframework.integration.dsl.groovy.ChannelInterceptor interceptor){
		this.interceptor = interceptor
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preSend(org.springframework.integration.Message, org.springframework.integration.MessageChannel)
	 */
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (!interceptor.preSend){
			return message
		}

		if (logger.isDebugEnabled()){
			logger.debug("invoking preSend closure")
		}
		def result = execClosure(interceptor.preSend, message,channel)
		if (result && !(result instanceof Message)){
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		return result
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#postSend(org.springframework.integration.Message, org.springframework.integration.MessageChannel, boolean)
	 */
	public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
		if (!interceptor.postSend){
			return 
		}
		if (logger.isDebugEnabled()){
			logger.debug("invoking postSend closure")
		}

		execClosure(interceptor.postSend, message,channel,sent)
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#preReceive(org.springframework.integration.MessageChannel)
	 */
	public boolean preReceive(MessageChannel channel) {
		return interceptClosure.preReceive ? interceptClosure.prereceive.doCall(channel) : true
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.channel.ChannelInterceptor#postReceive(org.springframework.integration.Message, org.springframework.integration.MessageChannel)
	 */
	public Message<?> postReceive(Message<?> message, MessageChannel channel) {
		if (!interceptor.postReceive){
			return message
		}

		def result = execClosure(interceptor.postReceive, message,channel)
		if (result && !(result instanceof Message)){
			result = MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
		}
		return result
	}

	private Object execClosure(Closure interceptClosure, Message<?>message, MessageChannel channel, sent = null){
		def paramTypes = interceptClosure.getParameterTypes()
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
					logger.debug("invoking closure on message")
				}
				result = interceptClosure.doCall(message)
			} else if (parameterType == Map && !(message.payload instanceof Map)) {
				if (logger.isDebugEnabled()) {
					logger.debug("invoking closure on message headers")
				}
				result =  interceptClosure.doCall(message.headers)
			} else if (parameterType == MessageChannel) {
				if (logger.isDebugEnabled()) {
					logger.debug("invoking closure on message headers")
				}
				result =  interceptClosure.doCall(channel)
			}
			else if (parameterType == boolean && !(message.payload instanceof boolean)) {
				if (logger.isDebugEnabled()) {
					logger.debug("invoking closure on message sent")
				}
				result =  interceptClosure.doCall(sent)
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("invoking closure on message payload")
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
