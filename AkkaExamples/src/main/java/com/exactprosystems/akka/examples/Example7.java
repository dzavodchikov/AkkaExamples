package com.exactprosystems.akka.examples;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Example7 {

	public static void main(String[] args) throws InterruptedException {

		Config config = ConfigFactory.parseResources("app.conf");
		
		final ActorSystem system = ActorSystem.create("MySystem", config);
		
		final ActorRef myActor1 = system.actorOf(Props.create(MyActor.class).withMailbox("limited-mailbox"), "DeadLockActor1");

		final ActorRef myActor2 = system.actorOf(Props.create(MyActor.class).withMailbox("limited-mailbox"), "DeadLockActor2");
		
		myActor1.tell("ping", myActor2);
		
		myActor2.tell("ping", myActor1);

		
	}

	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		
		private int resendTimes = 2;

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof String) {
				log.info("Received String message: {}", message);
				for (int i = 0; i < resendTimes; i++) {
					getSender().tell(message, getSelf());
				}
			} else {
				unhandled(message);
			}
			
		}

	}
	
}
