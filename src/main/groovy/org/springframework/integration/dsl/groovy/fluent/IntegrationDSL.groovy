

/* (non-Javadoc)
 * @see groovy.lang.Script#run()
 */
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
package org.springframework.integration.dsl.groovy.fluent;

import org.codehaus.groovy.control.CompilerConfiguration

/**
 * @author David Turanski
 *
 */
abstract class IntegrationDSL extends Script {
	 
	private static CompilerConfiguration compiler
	static  {
		compiler = new CompilerConfiguration()
		compiler.setScriptBaseClass("org.springframework.integration.dsl.groovy.fluent.IntegrationDSL")
		println("Integration DSL initialized")
	}

 
	static def evaluate(script){
	 
		GroovyShell  shell = new GroovyShell(this.class.classLoader, new Binding(), compiler)
		if (script instanceof Closure) {
			//((Closure)script).owner = this
			((Closure)script).delegate = this
			//script.call()
			shell.setVariable("closure", script)
		    shell.evaluate("closure.call()")
			
		} else {
			shell.evaluate(script)
		}
	}
	
	def foo(Closure c) {
		c.call();
		this
	}
	
	def rightShift(target) {
		println "--> $target"
		this
	}
	
	def to(target) {
		println "--> $target"
		this
	}
	
	static FluentTransformer transform() {
		new FluentTransformer()
	}
	
	static Stage1Router route() {
		new FluentRouter()
	}
	 
}
