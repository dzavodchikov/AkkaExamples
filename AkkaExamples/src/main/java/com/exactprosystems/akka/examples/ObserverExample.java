package com.exactprosystems.akka.examples;

import java.util.HashSet;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.Future;

public class ObserverExample {

	public static void main(String[] args) throws InterruptedException {
		
		final ActorSystem system = ActorSystem.create("MySystem");
		
		final ActorRef observable = system.actorOf(Props.create(Observable.class), "Observable");
		
		final ActorRef observer1 = system.actorOf(Props.create(Observer.class, observable), "Observer1");
		final ActorRef observer2 = system.actorOf(Props.create(Observer.class, observable), "Observer2");
		final ActorRef observer3 = system.actorOf(Props.create(Observer.class, observable), "Observer3");
		
		// What happens if comment this line?
		Thread.sleep(1000);
		
		observable.tell(new Event("Hello"), ActorRef.noSender());
		
		// What happens if comment this line?
		Thread.sleep(1000);
		
		observer1.tell(PoisonPill.getInstance(), ActorRef.noSender());
		observer2.tell(PoisonPill.getInstance(), ActorRef.noSender());
		observer3.tell(PoisonPill.getInstance(), ActorRef.noSender());
		
		// What happens if comment this line?
		Thread.sleep(1000);
		
		Future<Terminated> future = system.terminate();
		future.value();
		
	}

	private static class Observable extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		private final Set<ActorRef> observers = new HashSet<>();
		
		public void onReceive(Object message) throws Exception {
			
			if (message instanceof Event) {
				log.info("Received event message: {}", message);
				for (ActorRef actorRef : observers) {
					actorRef.forward(message, getContext());
				}
			} else if (message instanceof Subscribe) {
				log.info("Received subscribe message: {}", message);
				if (!observers.add(((Subscribe) message).getObserver())) {
					throw new RuntimeException();
				}
			} else if (message instanceof Unsubscribe) {
				log.info("Received unsubscribe message: {}", message);
				if (!observers.remove(((Unsubscribe) message).getObserver())) {
					throw new RuntimeException();
				}
			} else {
				unhandled(message);
			}
			
		}

	}
	
	private static class Observer extends UntypedActor {

		private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		private final ActorRef observable;
		
		public Observer(ActorRef observable) {
			this.observable = observable;
		}
		
		@Override
		public void preStart() throws Exception {
			this.observable.tell(new Subscribe(getSelf()), getSelf());
		}
		
		public void onReceive(Object message) throws Exception {
			
			if (message instanceof Event) {
				log.info("Received event message: {}", message);
			} else {
				unhandled(message);
			}
			
		}
		
		@Override
		public void postStop() throws Exception {
			this.observable.tell(new Unsubscribe(getSelf()), getSelf());
		}

	}
	
	private static class Subscribe {
		
		private final ActorRef observer;

		public Subscribe(ActorRef observer) {
			super();
			this.observer = observer;
		}

		public ActorRef getObserver() {
			return observer;
		}

		@Override
		public String toString() {
			return "Subscribe [observer=" + observer + "]";
		}
		
	}
	
	private static class Unsubscribe {
		
		private final ActorRef observer;

		public Unsubscribe(ActorRef observer) {
			super();
			this.observer = observer;
		}

		public ActorRef getObserver() {
			return observer;
		}

		@Override
		public String toString() {
			return "Unsubscribe [observer=" + observer + "]";
		}
		
	}
	
	private static class Event {
		
		private final Object subj;

		public Event(Object subj) {
			super();
			this.subj = subj;
		}

		public Object getSubj() {
			return subj;
		}

		@Override
		public String toString() {
			return "Event [subj=" + subj + "]";
		}
		
	}
	
}
