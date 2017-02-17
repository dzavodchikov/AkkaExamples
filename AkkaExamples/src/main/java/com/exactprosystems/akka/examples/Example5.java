package com.exactprosystems.akka.examples;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.Option;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.escalate;

public class Example5 {

	public static void main(String[] args) throws InterruptedException {

		final ActorSystem system = ActorSystem.create("MySystem");
		
		final ActorRef myActor = system.actorOf(Props.create(MyActor.class), "MyActor");
		
		myActor.tell(Props.create(ChildActor.class, ArithmeticException.class), ActorRef.noSender());
		Thread.sleep(1000);
		
		myActor.tell(Props.create(ChildActor.class, NullPointerException.class), ActorRef.noSender());
		Thread.sleep(1000);
		
		myActor.tell(Props.create(ChildActor.class, IllegalArgumentException.class), ActorRef.noSender());
		Thread.sleep(1000);
		
		myActor.tell(Props.create(ChildActor.class, Exception.class), ActorRef.noSender());
		Thread.sleep(1000);
		
	}

	private static class MyActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		private static SupervisorStrategy strategy = new OneForOneStrategy(10, Duration.create(1, TimeUnit.SECONDS),
				new Function<Throwable, Directive>() {
					@Override
					public Directive apply(Throwable t) {
						if (t instanceof ArithmeticException) {
							return resume();
						} else if (t instanceof NullPointerException) {
							return restart();
						} else if (t instanceof IllegalArgumentException) {
							return stop();
						} else {
							return escalate();
						}
					}
				});

		@Override
		public SupervisorStrategy supervisorStrategy() {
			return strategy;
		}
		
		public void onReceive(Object message) throws Exception {
			
			if (message instanceof Props) {
				ActorRef child = getContext().actorOf((Props) message);
				child.tell("Throw exception", getSelf());
			} else {
				unhandled(message);
			}
			
		}

	}
	
	private static class ChildActor extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		
		private Class<? extends Exception> exception;
		
		public ChildActor(Class<? extends Exception> exception) {
			this.exception = exception;
		}
		
		@Override
		public void preStart() throws Exception {
			log.info("Starting {}", this);
		}
		
		@Override
		public void preRestart(Throwable reason, Option<Object> message) throws Exception {
			log.info("Restarting {}", this);
		}
		
		@Override
		public void onReceive(Object message) throws Throwable {
			if (message instanceof String) {
				log.info("Received message: {}", message);
				Constructor<? extends Exception> constructor = this.exception.getConstructor(String.class);
				throw constructor.newInstance("Error during receive");
			} else {
				unhandled(message);
			}
		}
		
		@Override
		public void postStop() throws Exception {
			log.info("Stopping {}", this);
		}
		
	}
	
}
