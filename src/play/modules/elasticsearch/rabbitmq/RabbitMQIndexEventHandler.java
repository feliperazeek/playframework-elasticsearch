package play.modules.elasticsearch.rabbitmq;

import play.Logger;
import play.Play;
import play.modules.elasticsearch.ElasticSearchIndexEvent;
import play.modules.elasticsearch.IndexEventHandler;

/**
 * Handler which pushes events into a rabbitmq queue
 */
public class RabbitMQIndexEventHandler implements IndexEventHandler {
	
	/** Flag that indicates if the consumer has been started */
	private static boolean consumerStarted = false;

	@Override
	public void handle(ElasticSearchIndexEvent event) {
		if (consumerStarted == false) {
			startConsumer();
			consumerStarted = true;
		}
		
		// Exchange
		akka.amqp.ExchangeType directExchange = akka.amqp.Direct.getInstance();
		akka.amqp.AMQP.ExchangeParameters params = new akka.amqp.AMQP.ExchangeParameters(getQueue(), directExchange);
		
		// Producer
		akka.amqp.AMQP.ProducerParameters producerParams = new akka.amqp.AMQP.ProducerParameters(params);
		
		// Connection
		com.rabbitmq.client.Address address = new com.rabbitmq.client.Address(getHost(), getPort());
		com.rabbitmq.client.Address[] addresses = {address};
		akka.amqp.AMQP.ConnectionParameters connectionParameters = new akka.amqp.AMQP.ConnectionParameters(addresses, getUsername(), getPassword(), getVirtualHost());
		akka.actor.ActorRef connection = akka.amqp.AMQP.newConnection(connectionParameters);
		
		// Send Event
		akka.actor.ActorRef producer = akka.amqp.AMQP.newProducer(connection, producerParams);
		producer.sendOneWay(event);
	}
	
	/**
	 * Gets the queue.
	 * 
	 * @return the queue
	 */
	private static String getQueue() {
		String s = Play.configuration.getProperty("elasticsearch.rabbitmq.queue");
		if (s == null) {
			return "elasticSearchQueue";
		}
		return s;
	}
	
	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	private static String getHost() {
		return Play.configuration.getProperty("elasticsearch.rabbitmq.host");
	}
	
	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	private static Integer getPort() {
		return Integer.valueOf(Play.configuration.getProperty("elasticsearch.rabbitmq.port"));
	}
	
	/**
	 * Gets the username.
	 * 
	 * @return the username
	 */
	private static String getUsername() {
		return Play.configuration.getProperty("elasticsearch.rabbitmq.username");
	}
	
	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	private static String getPassword() {
		return Play.configuration.getProperty("elasticsearch.rabbitmq.password");
	}
	
	/**
	 * Gets the virtualhost.
	 * 
	 * @return the virtualhost
	 */
	private static String getVirtualHost() {
		return Play.configuration.getProperty("elasticsearch.rabbitmq.virtualHost");
	}
	
	private static void startConsumer() {
		// TODO Finish RabbitMQ Integration
		Logger.info("Triggering RabbitMQConsumer for Elastic Search...");
		
		// Exchange
		akka.amqp.ExchangeType directExchange = akka.amqp.Direct.getInstance();
		akka.amqp.AMQP.ExchangeParameters params = new akka.amqp.AMQP.ExchangeParameters(getQueue(), directExchange);
		
		// Consumer
		com.rabbitmq.client.Address address = new com.rabbitmq.client.Address(getHost(), getPort());
		com.rabbitmq.client.Address[] addresses = {address};
		akka.amqp.AMQP.ConnectionParameters connectionParameters = new akka.amqp.AMQP.ConnectionParameters(addresses, getUsername(), getPassword(), getVirtualHost());
		@SuppressWarnings("unused")
		akka.actor.ActorRef connection = akka.amqp.AMQP.newConnection(connectionParameters);
		
		akka.actor.ActorRef ref = akka.actor.Actors.actorOf(RabbitMQConsumerActor.class);
		akka.amqp.AMQP.ConsumerParameters consumerParams = new akka.amqp.AMQP.ConsumerParameters(getQueue(), ref, params);
		akka.amqp.AMQP.newConsumer(ref, consumerParams);
	}

}
