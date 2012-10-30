package org.springframework.integration.samples.http;


import org.springframework.integration.dsl.groovy.MessageFlow
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

public class HttpExamples {

	/**
	 * @param args
	 */
	static void main(String[] args) {
		def builder = new IntegrationBuilder('http')
		builder.setAutoCreateApplicationContext(false)

		def ticker = 'VMW'
		
		def flow = builder.messageFlow {
			
				httpGet(url:"http://www.google.com/finance/info?q=$ticker",responseType:String)
		}		
						
		def result = flow.sendAndReceive('')
		println result
	 
	}

}
