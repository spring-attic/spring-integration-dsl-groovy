doWithSpringIntegration {
		messageFlow('flow1',outputChannel:'flow2.inputChannel') {
				transform {it.toUpperCase()}
		}

		def flow2 = messageFlow('flow2',outputChannel:'flow2.ouputChannel') {
				filter {it.class == String}
				transform {it.toLowerCase()}
		}
		handle(inputChannel:flow2.outputChannel,{println it})
}	