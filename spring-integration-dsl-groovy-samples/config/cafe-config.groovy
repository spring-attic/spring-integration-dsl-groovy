import org.springframework.integration.samples.cafe.*
doWithSpringIntegration {
  springXml {
	  bean(id:'barista','class':'org.springframework.integration.samples.cafe.Barista')
	  'si:gateway'(id:'cafe',
		  'service-interface':'org.springframework.integration.samples.cafe.Cafe',
		  'default-request-channel':'orders')
  }
  
  split(inputChannel:'orders', evaluate:{payload->payload.items}, outputChannel:'drinks')
  route(inputChannel:'drinks', evaluate:{payload-> payload.iced ? 'coldDrinks' : 'hotDrinks'})
  queueChannel('coldDrinks',capacity:10)
  handle(inputChannel:'coldDrinks',ref:'barista',method:'prepareColdDrink',outputChannel:'preparedDrinks')

  queueChannel('hotDrinks',capacity:10)
  handle(inputChannel:'hotDrinks',ref:'barista',method:'prepareHotDrink',outputChannel:'preparedDrinks')
  
  aggregate(inputChannel:'preparedDrinks', action:{drinks-> new Delivery(drinks)},
      correlationStrategy: {drink->  drink.getOrderNumber()}
      ,outputChannel:'deliveries')
  
  handle(inputChannel:'deliveries',action:{println it})	 
  
  poll('poller','default':true,'fixed-delay':1000)
}