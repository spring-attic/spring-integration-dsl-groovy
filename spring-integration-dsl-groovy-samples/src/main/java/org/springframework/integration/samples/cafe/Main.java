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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder;

/**
 * @author David Turanski
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IntegrationBuilder integrationBuilder = new IntegrationBuilder();
		try {
			integrationBuilder.build(new FileInputStream("config/cafe-config.groovy"));	
			ApplicationContext applicationContext = integrationBuilder.getApplicationContext();
			
			Cafe cafe = (Cafe) applicationContext.getBean("cafe");
			for (int i = 1; i <= 100; i++) {
				Order order = new Order(i);
				order.addItem(DrinkType.LATTE, 2, false);
				order.addItem(DrinkType.MOCHA, 3, true);
				cafe.placeOrder(order);
			}
			((GenericApplicationContext)applicationContext).close();		
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
