/*
 * Copyright 2002-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.samples.cafe;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder;

/**
 * @author David Turanski
 * 
 */
public class Main {

	/**
	 * Main class for the Cafe Demo using the Groovy DSL from Java
	 * @param args
	 */
	public static void main(String[] args) {
		
		IntegrationBuilder integrationBuilder = new IntegrationBuilder();

		/*
		 * Build the SI Application from Groovy DSL
		 *
		 * In this case the script is a compiled groovy.lang.Script. 
		 * 
		 * It is also possible to load a .groovy file as a File, Spring Resource,
		 * GroovyCodeSource, or InputStream
		 */
		
		integrationBuilder.build(new CafeConfig());
		
		
		/*
		 * Get a reference to the Cafe gateway and place some orders
		 */
		ApplicationContext applicationContext = integrationBuilder
				.getApplicationContext();

		Cafe cafe = applicationContext.getBean("cafe",Cafe.class);
		for (int i = 1; i <= 100; i++) {
			Order order = new Order(i);
			order.addItem(DrinkType.LATTE, 2, false);
			order.addItem(DrinkType.MOCHA, 3, true);
			cafe.placeOrder(order);
		}
		
		((GenericApplicationContext) applicationContext).close();
	}

}