package com.exactprosystems.akka.examples;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Example8 {

	public static void main(String[] args) throws InterruptedException {

		Config config = ConfigFactory.parseResources("app.conf");
		
		final ActorSystem system = ActorSystem.create("MySystem", config);
		
		final ActorRef blockingActor = system.actorOf(Props.create(BlockingActor.class).withDispatcher("single-thread-dispatcher"), "BlockingActor");
		blockingActor.tell("ping", ActorRef.noSender());
		
		final ActorRef normalActor = system.actorOf(Props.create(NormalActor.class).withDispatcher("single-thread-dispatcher"), "NormalActor");
		normalActor.tell("ping", ActorRef.noSender());
		
	}

	private static class BlockingActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof String) {
				log.info("Received String message: {}", message);
				Thread.sleep(5000);
			} else {
				unhandled(message);
			}
			
		}

	}
	
	private static class NormalActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof String) {
				log.info("Received String message: {}", message);
			} else {
				unhandled(message);
			}
			
		}

	}
	
}
