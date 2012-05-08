doWithSpringIntegration {
		messageFlow('flow1',outputChannel:'flow2.inputChannel') {
				transform(evaluate:{it.toUpperCase()})
		}
		
		def flow2 = messageFlow('flow2',outputChannel:'flow2.ouputChannel') {
				filter(evaluate:{it.class == String})
				transform(evaluate:{it.toLowerCase()})
		}
		
		handle(inputChannel:flow2.outputChannel,evaluate:{println it}) 		
}	