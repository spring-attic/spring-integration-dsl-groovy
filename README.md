Groovy DSL For Spring Integration

Multiple message flows may be created. 
//The default argument (value) of a message flow is its name and its input channel "${name}#inputChannel"
// if no inputChannel is specified, the initial endpoint must specify an input channel (or a name). (Maybe not)
// Also the final endpoint may specify an output channel


MessageFlow is backed by a chain: Provides convenience methods, send(obj), sendAndReceive(obj)




Message Flow examples

messageFlow(inputChannel:'inputC',outputChannel:'outputC') {
	 transform(evaluate:{payload->payload.toUpperCase()})
	 filter(evaluate:{payload-> payload=="HELLO"})
	 handle(evaluate:{payload->payload})
}

// More complex example

doWithSpringIntegration {
		messageFlow(outputChannel:'outputChannel1') {
			transform(evaluate:{it.toUpperCase()})
		}
		
		def flow2 = messageFlow(inputChannel:'outputChannel1') {
			transform(evaluate:{it.toLowerCase()})
		}
		
		handle(inputChannel:flow2.outputChannel,evaluate:{println it})
}

def integrationContext = doWithSpringIntegration {builder->
   		
   		def flow1 = builder.messageFlow(outputChannel:'outputChannel1') {
			transform(evaluate:{it.toUpperCase()})
		}
		
		def flow2 = builder.messageFlow(inputChannel:'outputChannel1') {
			transform(evaluate:{it.toLowerCase()})
		}
		
		handle(inputChannel:flow2.outputChannel,evaluate:{println it})
}

messageFlow(inputChannel:'inputC',outputChannel:'outputC') {
	 transform(evaluate:{payload->payload.toUpperCase()})
	 filter(evaluate:{payload-> payload=="HELLO"},outputChannel:'toHandler',discardChannel:'discardChannel')
	 handle(inputChannel:'toHandler', evaluate:{payload})
	 handle(inputChannel:'discardChannel',evaluate:{println it})
}

//Alternate Ideas  
 -- Explicit chain
def ic = builder.doWithSpringIntegration  {
	chain(inputChannel:'inputC',outputChannel:'outputC') {
	 transform(evaluate:{payload->payload.toUpperCase()})
	 filter(evaluate:{payload-> payload=="HELLO"})
	 handle(evaluate:{payload})
	}
}

def reply=ic.sendAndReceive('inputC')



messageFlow(inputChannel:'inputC',outputChannel:'outputC') {
	 transform(evaluate:{payload->payload.toUpperCase()})
	 filter(evaluate:{payload-> payload=="HELLO"})
	 handle(evaluate:{payload})
}

//Inline Routing
messageFlow {
	route(evaluate:{condition}){
		when(value1) {
			//chain
		}
		when (value2) {
			//chain
		}
	}
}

//Routing 1
messageFlow {
	route(evaluate:{condition}){
		when(value1,channel:'output1') 
		when(value2,channel:'output2')
		otherwise('output3') 
	}
	
	transform(inputChannel:'output1',evaluate:{transformation1})
	transform(inputChannel:'output2',evaluate:{transformation2})
	transform(inputChannel:'output3',evaluate:{transformation3})
}

//Routing 2
messageFlow {
	route(evaluate:{condition}){
		when(value1,channel:'output1') 
		when(value2,channel:'output2')
		otherwise('output3') 
	}
	
	messageFlow('output1'){
	
	}
	
	messageFlow('output2') {
	
	}
	
	messageFlow('output3') {
	
	}
}


//Routing 3
route(evaluate {Message m ->
   if (m.headers['foo'] == 'bar') {
   	return 'bar#inputChannel'
   } 
   return 'foo#inputChannel'
})

handle('foo',evaluate:{})
handle('bar',evaluate:{})


//Nested flows
messageFlow('subflow'){
}

messageFlow('main'){
   transform(evaluate:{payload->payload.toUpperCase()})
   filter(evaluate:{payload-> payload=="HELLO"},outputChannel:'subflow')
}

messageFlow('main'){
   transform(evaluate:{payload->payload.toUpperCase()})
   filter(evaluate:{payload-> payload=="HELLO"})
   call('subflow')
}

//Channel usage:

	//@Test
	void testChannels() {
		builder.messageFlow {
			pubSubChannel('pubSub') {
				subscribe(){
					transform()
					route(outputChannel:'outputChannel')
					
				}
				subscribe(){
					
				}
			}
			channel('outputChannel') {
				log(evaluate:{payload})
			}
		}
	}

