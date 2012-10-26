package org.springframework.integration.samples.basic

import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

class SimpleExamples {
	
	static main(args) {
		//helloWorld();
		//multipleFlows();
		//simpleChain();
		channelNames();
	}

	static helloWorld() {
		def builder = new IntegrationBuilder()
		def flow = builder.messageFlow {
			 transform {"$it, world!"}
		}
	
		println flow.sendAndReceive('hello')
	}
	
	static multipleFlows() {
		def builder = new IntegrationBuilder()
		builder.setAutoCreateApplicationContext(false)
		
		def flow1 = builder.messageFlow {
			 transform {it.toUpperCase()}
		}
		
		def flow2 = builder.messageFlow {
			transform {it.toLowerCase()}
	   }
	
		println "${flow1.sendAndReceive('hello')}, ${flow2.sendAndReceive('WORLD!!')}"
	}
	
	static simpleChain() {
		def builder = new IntegrationBuilder()
		def flow = builder.messageFlow(inputChannel:'inputChannel') {
			filter {it == 'World'}
			transform {'Hello ' + it}
			handle {println "****************** $it ***************" }
		}
		
		flow.send("World")
		flow.send("shouldNotPrint")
	}
	
	static channelNames() {
		def builder = new IntegrationBuilder()
		def ic = builder.doWithSpringIntegration {
			messageFlow('flow') {
				filter {it == 'World'}
				transform(inputChannel:'transformerChannel') {'Hello ' + it}
				handle {println "****************** $it ***************" }
			}
		}
		
		ic.send('flow.inputChannel','World')
		ic.send('transformerChannel','Earth')
		
	}
}
