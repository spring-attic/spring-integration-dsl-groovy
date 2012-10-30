Groovy DSL For Spring Integration
=================================

## Overview
This project implements a Groovy DSL for Spring Integration. Coming on the heels of the [Scala DSL for Spring Integration](https://github.com/SpringSource/spring-integration-dsl-scala), I have incorporated some of the same basic ideas and vocabulary however this has been developed independently with a primary focus on providing Groovy and Java developers a simple, flexible, and powerful alternative to XML configuration for Spring Integration applications. Please refer to the [DSL User's Guide](https://github.com/SpringSource/spring-integration-dsl-groovy/wiki/DSL-User's-Guide) and other pages on the project [wiki](https://github.com/SpringSource/spring-integration-dsl-groovy/wiki) for more details.

## Features
* Simplified Spring Integration configuration and execution based on the familiar Groovy Builder pattern
* Implement endpoint logic with closures
* Automatically chains endpoints, eliminating the need to eplicitly declare channels
* Named channels are supported if needed
* Full support for native Spring bean and Spring Integration via Groovy markup. 
* Provides access to the underlying Spring Application context
* May be executed direclty in Groovy or from a Java class

## Implementation
The DSL uses Groovy Builder pattern so the syntax will be familiar to Groovyists. The central class is the *IntegrationBuilder* which extends  [FactoryBuilderSupport](http://groovy.codehaus.org/FactoryBuilderSupport) framework to create a Spring Integration domain model which is translated directly to Spring XML to create a Spring Application Context. 

The main benefit of this approach is that the DSL can leverage existing Spring Integration namespace parsers to perform all the necessary validation and bean definition processing. Since this code is tightly coupled to XML parsing, the bean definitions and would otherwise need to be entirely replicated for the DSL. Another advantage is that if the log level is set to DEBUG, you can inspect the generated XML on the console which will give you more insight into how the DSL interprets things. Finally it makes it very easy for the IntegrationBuilder to inject XML builder markup, providing seamless integration with Spring XML.

## Example

The following is a simple example in Groovy.

    def builder = new IntegrationBuilder()

    def flow = builder.messageFlow {
	 transform {payload->payload.toUpperCase()}
	 filter {payload-> payload =="HELLO"}
	 handle {payload->payload}
    }

    assert flow.sendAndReceive("hello") == "HELLO"
    assert flow.sendAndReceive("world") == null


This flow can also be executed from a Java class. The easiest way is to create an external file or classpath resource. The equivalent to the above example looks like :

     messageFlow {
	     transform {payload->payload.toUpperCase()}
	     filter {payload-> payload =="HELLO"})
	     handle {payload->payload})
      }

If we save this to a file named 'messageFlow1.groovy', it may be accessed from a Java class as follows:

    IntegrationBuilder builder = new IntegrationBuilder();
    
    MessageFlow flow = (MessageFlow)builder.build(
    	new FileInputStream("messageFlow1.groovy"));
    	
    flow.sendAndReceive("hello");
    
In addition to *InputStream* the API accepts other sources such as a compiled *groovy.lang.Script* and *groovy.lang.GroovyCodeSource*

 The [DSL User's Guide](https://github.com/SpringSource/spring-integration-dsl-groovy/wiki/DSL-User's-Guide) contains a lot more examples to get you started. Also take a look at the [spring-integration-dsl-groovy-samples](https://github.com/SpringSource/spring-integration-dsl-groovy/tree/master/spring-integration-dsl-groovy-samples) subproject as well as the unit tests included in the various sub projects. 


## Project Structure

The DSL structure mirrors Spring Integration itself. The core module provides the core components and language framework. Protocol adapters, e.g., http, jms, are maintained as separate subprojects. In addition there is a samples module to help get you started. Each adapter module plugs in to the DSL framework to provide extensions needed to support the associated adapters. At this time, there are only a few adapters implemented for the DSL, but this is expected to grow according to demand, community contributions, etc.  

Even if an adapter is not implemented directly in the DSL, you can use Groovy markup to drop in native Spring XML. Please refer to the [DSL User's Guide](https://github.com/SpringSource/spring-integration-dsl-groovy/wiki/DSL-User's-Guide) for more information.

## Contributing 

With the inital core DSL features in place, there remains much to be done to support the constantly growing collection of protocol adapters covered by Spring Integration. Community contributions are certainly welcome! If this interests you, please refer to the [DSL Developer's Guide](https://github.com/SpringSource/spring-integration-dsl-groovy/wiki/DSL-Developer's-Guide) for more information.

Here are some ways for you to get involved in the community:

* Create [JIRA](https://jira.springsource.org/browse/INTDSLGROOVY) tickets for bugs and new features and comment and vote on the ones that you are interested in.

* Github is for social coding: if you want to write code, we encourage contributions through pull requests from forks of this repository. Please refer to the [Spring Integration Contributer Guidelines](https://github.com/SpringSource/spring-integration/wiki/Contributor-Guidelines) (apply the same process for this repository). Also, if you want to contribute code this way, please reference a [JIRA](https://jira.springsource.org/browse/INTDSLGROOVY) ticket as well covering the specific issue you are addressing.

* Watch for upcoming articles on Spring by [subscribing](www.springsource.org/node/feed) to springframework.org

* Before we accept a non-trivial patch or pull request we will need you to sign the contributor's [agreement](https://support.springsource.com/spring_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do. Active contributors might be asked to join the core team, and given the ability to merge pull requests.