package com.exactprosystems.akka.examples;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.AskTimeoutException;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Example3 {

	public static void main(String[] args) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("MySystem");
		
		// 1 Method
		final ActorRef myActor1 = system.actorOf(Props.create(MyActor.class), "MyActor1");
		myActor1.tell(new StopMessage(), ActorRef.noSender());
		
		// 2 Method
		final ActorRef myActor2 = system.actorOf(Props.create(MyActor.class), "MyActor2");
		myActor2.tell(akka.actor.PoisonPill.getInstance(), ActorRef.noSender());
		
		// 3 Method
		try {
			final ActorRef myActor3 = system.actorOf(Props.create(MyActor.class), "MyActor3");
			Future<Boolean> stopped = Patterns.gracefulStop(myActor3, Duration.create(5, TimeUnit.SECONDS));
			Await.result(stopped, Duration.create(6, TimeUnit.SECONDS));
		} catch (Exception e) { }
		
	}

	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		public void onReceive(Object message) throws Exception {
			
			if (message instanceof StopMessage) {
				log.info("Received stop message: {}", message);
				// 1 Method
				context().stop(getSelf());
			} else {
				unhandled(message);
			}
			
		}
		
		@Override
		public void postStop() throws Exception {
			log.info("Stopped {}", getSelf());
		}

	}
	
	private static class StopMessage {

		@Override
		public String toString() {
			return "StopMessage []";
		}
		
	}
	
}
