Groovy DSL For Spring Integration

Multiple message flows may be created. 
//The default argument (value) of a message flow is its name and its input channel "${name}#inputChannel"
// if no inputChannel is specified, the initial endpoint must specify an input channel (or a name).


Examples

//Can work like a chain
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

