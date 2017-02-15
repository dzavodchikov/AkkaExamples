package com.exactprosystems.akka.examples;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Example6 {

	public static void main(String[] args) throws InterruptedException {

		Config config = ConfigFactory.parseResources("app.conf");
		
		final ActorSystem system = ActorSystem.create("MySystem", config);
		
		final ActorRef myActor = system.actorOf(Props.create(MyActor.class), "MyActor");
		
		myActor.tell("ping", ActorRef.noSender());
		
	}

	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof String) {
				log.info("Received String message: {}", message);
				getSender().tell(message, getSelf());
			} else {
				unhandled(message);
			}
			
		}

	}
	
}
