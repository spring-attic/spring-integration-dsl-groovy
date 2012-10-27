package org.springframework.integration.samples.http;


import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder

public class HttpExamples {

	/**
	 * @param args
	 */
	static void main(String[] args) {
		def builder = new IntegrationBuilder('http')
		builder.setAutoCreateApplicationContext(false)
	 
		/*
		 * Static endpoint
		 */
		def flow1 = builder.messageFlow {
				httpGet(url:'http://www.google.com/finance/info?q=VMW',responseType:String)
		}		
				
		/*
		 * Dynamic endpoint using a closure
		 */
		
		def flow2 = builder.messageFlow {
			httpGet(url:{"http://www.google.com/finance/info?q=$it"},responseType:String)
			    
		}
		
		def result = flow1.sendAndReceive('')
		println result
		
		result = flow2.sendAndReceive('VMW')
		println result
	}

}
