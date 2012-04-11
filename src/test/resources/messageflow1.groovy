messageFlow {
	route('myRouter'){
		when('bar') {
			handle(action:{})
		}
		when('zaz'){
			handle(action:{})
		}
	}
}