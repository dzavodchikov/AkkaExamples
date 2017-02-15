package com.exactprosystems.akka.examples;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Example4 {

	public static void main(String[] args) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("MySystem");
		
		final ActorRef myActor = system.actorOf(Props.create(MyActor.class), "MyActor");
		
		myActor.tell("ping", ActorRef.noSender());
		
	}

	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof String) {
				log.info("Received String message: {}", message);
				ActorRef child = getContext().actorOf(Props.empty(), "Child");
				log.info("Parent path: {}", getSelf());
				log.info("Child path: {}", child);
			} else {
				unhandled(message);
			}
			
		}

	}
	
}
