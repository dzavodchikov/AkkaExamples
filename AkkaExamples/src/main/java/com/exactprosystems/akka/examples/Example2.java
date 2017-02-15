package com.exactprosystems.akka.examples;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Example2 {

	public static void main(String[] args) {
		
		final ActorSystem system = ActorSystem.create("MySystem");
		
		final ActorRef myActor = system.actorOf(Props.create(MyActor.class, "arg1", "arg2"), "MyActor");
		
		myActor.tell(new InitMessage(), ActorRef.noSender());
		
	}
	
	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public MyActor(String arg1, String arg2) {
			log.info("Constructor");
		}
		
		@Override
		public void preStart() throws Exception {
			log.info("PreStart");
		}
		
		public void onReceive(Object message) throws Exception {
			
			if (message instanceof InitMessage) {
				log.info("Received init message: {}", message);
			} else {
				unhandled(message);
			}
			
		}

	}
	
	private static class InitMessage {

		@Override
		public String toString() {
			return "InitMessage []";
		}
		
	}
	
}
