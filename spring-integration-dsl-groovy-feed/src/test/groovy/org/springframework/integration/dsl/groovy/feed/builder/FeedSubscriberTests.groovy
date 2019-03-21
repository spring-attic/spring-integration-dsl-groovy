/*
 * Copyright 2002-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.dsl.groovy.feed.builder

import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.beans.factory.BeanCreationException
import org.springframework.integration.dsl.groovy.builder.IntegrationBuilder
/**
 * @author Russell Hart
 *
 */
class FeedSubscriberTests {
	IntegrationBuilder builder
	
	@Before
	void setup() {
		builder = new IntegrationBuilder('feed')
	}
	
	@Test
	void testFeedSubscriberWithTopLevelPoller() {
		def flow = builder.messageFlow {
			poll('cronPoller',cron:"*/10 * * * * *", "max-messages-per-poll": "2")
			readFeed(url:'http://test/rss.xml', poller:"cronPoller")
			transform {it.title.toUpperCase()}
			handle() {println "$it"}
		}
	}
	
	@Test
	void testFeedSubscriberWithInnerLevelPoller() {
		def flow = builder.messageFlow {
            readFeed(url:'http://test/rss.xml'){ poll(cron:"*/10 * * * * *", "max-messages-per-poll": "2") }
			transform {it.title.toUpperCase()}
			handle() {println "$it"}
		}
	}
	
	@Test
	void testFeedSubscriberWithDefaultPoller() {
		builder.doWithSpringIntegration {
            def flow = builder.messageFlow {
                readFeed(url:'http://test/news/rss.xml')
                transform {it.title.toUpperCase()}
                handle() {println "$it"}
            }
		
			poll('poller','default':true, fixedDelay:1000)
		}
	}
	
	@Test(expected=BeanCreationException.class)
	void testFeedSubscriberWithNoPoller() {
		builder.doWithSpringIntegration {
            readFeed(url:'http://test/rss.xml')
		}
	}
	
	@Test(expected=AssertionError.class)
	void testFeedSubscriberNoUrl() {
		def flow = builder.messageFlow {
            readFeed(){ poll(cron:"*/10 * * * * *", "max-messages-per-poll": "2") }
		}
	}
	
	@Test
	void testFeedSubscriberWithCustomFeedFetcher() {
		def integrationContext = builder.doWithSpringIntegration {
		
			def flow = messageFlow {
				springXml {
					bean(id:"fileUrlFeedFetcher",
						class:"org.springframework.integration.dsl.groovy.feed.builder.FileUrlFeedFetcher")
				}

                readFeed(id:'fooFeed', url:'http://test/news/rss.xml', feedFetcher:'fileUrlFeedFetcher'){
					poll(cron:"*/10 * * * * *", "max-messages-per-poll": "2") 
				}
				transform {it.title.toUpperCase()}
				handle() {println "$it"}
			}
			
		}
		
		def fooFeedBean = integrationContext.applicationContext.getBean('fooFeed')
		assert(fooFeedBean.source.feedFetcher instanceof FileUrlFeedFetcher)
	}
	
	@Test
	void testFeedSubscriberWithCustomMetaDataStore() {
		def integrationContext = builder.doWithSpringIntegration {
		
			def flow = messageFlow {
				springXml {
					bean(id:"sampleMetadataStore",
						class:"org.springframework.integration.dsl.groovy.feed.builder.SampleMetadataStore")
				}

                readFeed(id:'fooFeed', url:'http://test/news/rss.xml', metadataStore:'sampleMetadataStore'){
					poll(cron:"*/10 * * * * *", "max-messages-per-poll": "2")
				}
				transform {it.title.toUpperCase()}
				handle() {println "$it"}
			}
			
		}
		
		def fooFeedBean = integrationContext.applicationContext.getBean('fooFeed')
		assert(fooFeedBean.source.metadataStore instanceof SampleMetadataStore)
	}
	
	@Test
	void validateSuccessfulNewsRetrievalWithFileUrl() {
		CountDownLatch latch = spy(new CountDownLatch(2));
			
		def flow = builder.messageFlow {
			springXml {
				bean(id:"myFileUrlFeedFetcher",
					class:"org.springframework.integration.dsl.groovy.feed.builder.FileUrlFeedFetcher")
			}

            readFeed(url:'file:src/test/groovy/sample.rss', feedFetcher:'myFileUrlFeedFetcher'){
                poll(cron:"*/10 * * * * *", "max-messages-per-poll": "2")
			}
			transform {it.title.toUpperCase()}
			handle() {latch.countDown()}
		}
								
		latch.await(11, TimeUnit.SECONDS);
		verify(latch, times(2)).countDown();
	}
}
