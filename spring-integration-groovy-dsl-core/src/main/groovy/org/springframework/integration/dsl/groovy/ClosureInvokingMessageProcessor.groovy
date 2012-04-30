package org.springframework.integration.dsl.groovy

import org.springframework.integration.Message
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

class ClosureInvokingReleaseStrategy extends ClosureInvokingMessageProcessor {
	ClosureInvokingReleaseStrategy(Closure closure) {
		super(closure)
	}
	
	public Boolean canRelease(List<?> items){
		this.closure.doCall(items)
	}
}


