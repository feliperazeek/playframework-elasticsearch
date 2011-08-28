/** 
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Felipe Oliveira (http://mashup.fm)
 * 
 */
package play.modules.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import play.modules.elasticsearch.adapter.ElasticSearchAdapter;
import play.modules.elasticsearch.util.ExceptionUtil;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import play.modules.elasticsearch.rabbitmq.*;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.Model;
import play.mvc.Router;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchPlugin.
 */
public class ElasticSearchPlugin extends PlayPlugin {

	/** The started. */
	private static boolean started = false;

	/** Flag that indicates if the consumer has been started */
	private static boolean consumerStarted = false;

	/** The model index. */
	private static Map<Class<?>, Boolean> modelIndex = null;

	/** The client. */
	private static Client client = null;

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public static Client client() {
		return client;
	}

	/**
	 * Checks if is local mode.
	 * 
	 * @return true, if is local mode
	 */
	private boolean isLocalMode() {
		try {
			String client = Play.configuration.getProperty("elasticsearch.client");
			Boolean local = Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.local", "true"));

			if (client == null) {
				return true;
			}
			if (client.equalsIgnoreCase("false") || client.equalsIgnoreCase("true")) {
				return true;
			}

			return local;
		} catch (Exception e) {
			Logger.error("Error! Starting in Local Model: %s", ExceptionUtil.getStackTrace(e));
			return true;
		}
	}

	/**
	 * Gets the hosts.
	 * 
	 * @return the hosts
	 */
	public static String getHosts() {
		String s = Play.configuration.getProperty("elasticsearch.client");
		if (s == null) {
			return "";
		}
		return s;
	}

	/**
	 * Gets the delivery model.
	 * 
	 * @return the delivery model
	 */
	public static ElasticSearchDeliveryMode getDeliveryModel() {
		String s = Play.configuration.getProperty("elasticsearch.delivery");
		if (s == null) {
			return ElasticSearchDeliveryMode.LOCAL;
		}
		return ElasticSearchDeliveryMode.valueOf(s.toUpperCase());
	}

	/**
	 * Gets the queue.
	 * 
	 * @return the queue
	 */
	public static String getRabbitMQQueue() {
		String s = Play.configuration.getProperty("elasticsearch.rabbitmq.queue");
		if (s == null) {
			return "elasticSearchQueue";
		}
		return s;
	}

	/**
	 * This method is called when the application starts - It will start ES
	 * instance
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// Start Model Map
		modelIndex = new HashMap<Class<?>, Boolean>();

		// Make sure it doesn't get started more than once
		if ((client != null) || started) {
			Logger.debug("Elastic Search Started Already!");
			return;
		}

		// Start Node Builder
		Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("client.transport.sniff", true);
		settings.build();

		// Check Model
		if (this.isLocalMode()) {
			Logger.info("Starting Elastic Search for Play! in Local Mode");
			NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
			Node node = nb.node();
			client = node.client();

		} else {
			Logger.info("Connecting Play! to Elastic Search in Client Mode");
			TransportClient c = new TransportClient(settings);
			if (Play.configuration.getProperty("elasticsearch.client") == null) {
				throw new RuntimeException("Configuration required - elasticsearch.client when local model is disabled!");
			}
			String[] hosts = getHosts().trim().split(",");
			boolean done = false;
			for (String host : hosts) {
				String[] parts = host.split(":");
				if (parts.length != 2) {
					throw new RuntimeException("Invalid Host: " + host);
				}
				Logger.info("Transport Client - Host: %s Port: %s", parts[0], parts[1]);
				c.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.valueOf(parts[1])));
				done = true;
			}
			if (done == false) {
				throw new RuntimeException("No Hosts Provided for Elastic Search!");
			}
			client = c;
		}

		// Bind Admin
		Router.addRoute("GET", "/es-admin/", "ElasticSearchAdmin.index");

		// Check Client
		if (client == null) {
			throw new RuntimeException("Elastic Search Client cannot be null - please check the configuration provided and the health of your Elastic Search instances.");
		}
	}

	/**
	 * Checks if is elastic searchable.
	 * 
	 * @param o
	 *            the o
	 * @return true, if is elastic searchable
	 */
	private boolean isElasticSearchable(Object o) {
		Class<?> clazz = o.getClass();
		while (clazz != null) {
			// Logger.info("Class: %s", clazz);
			for (Annotation a : clazz.getAnnotations()) {
				// Logger.info("Class: %s - Annotation: %s", clazz,
				// a.toString());
				if (a.toString().indexOf("ElasticSearchable") > -1) {
					return true;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return false;
	}

	/**
	 * This is the method that will be sending data to ES instance
	 * 
	 * @see play.PlayPlugin#onEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onEvent(String message, Object context) {
		// Log Debug
		Logger.info("Event: %s - Object: %s", message, context);

		// Check if object has annotation
		boolean isSearchable = this.isElasticSearchable(context);
		// Logger.info("Searchable: %s", isSearchable);
		if (isSearchable == false) {
			// Logger.debug("Not marked to be elastic searchable!");
			return;
		}

		// If delivery mode is RabbitMQ fire off the consumer
		if ((consumerStarted == false) && getDeliveryModel().equals(ElasticSearchDeliveryMode.RABBITMQ)) {
			// TODO Finish RabbitMQ Integration
			Logger.info("Triggering RabbitMQConsumer for Elastic Search...");
			
			// Exchange
			akka.amqp.ExchangeType directExchange = akka.amqp.Direct.getInstance();
			akka.amqp.AMQP.ExchangeParameters params = new akka.amqp.AMQP.ExchangeParameters(getRabbitMQQueue(), directExchange);
			
			// Consumer
			akka.actor.ActorRef ref = akka.actor.Actors.actorOf(RabbitMQConsumerActor.class);
			akka.amqp.AMQP.ConsumerParameters consumerParams = new akka.amqp.AMQP.ConsumerParameters(getRabbitMQQueue(), ref, params);
			akka.amqp.AMQP.newConsumer(ref, consumerParams);
			consumerStarted = true;
		}

		// Get Plugin
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);

		// Check if the index has been started
		Class<?> clazz = context.getClass();
		if (modelIndex.containsKey(clazz) == false) {
			Logger.info("Start Index for Class: %s", clazz);
			ElasticSearchAdapter.startIndex(plugin.client(), clazz);
			modelIndex.put(clazz, Boolean.TRUE);
		}

		// Define Event
		ElasticSearchIndexEvent event = null;
		if (message.endsWith(".objectPersisted") || message.endsWith(".objectUpdated")) {
			// Index Model
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.INDEX);

		} else if (message.endsWith(".objectDeleted")) {
			// Delete Model from Index
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.DELETE);
		}

		// Sync with Elastic Search
		Logger.info("Elastic Search Index Event: %s", event);
		if (event != null) {
			if (getDeliveryModel().equals(ElasticSearchDeliveryMode.RABBITMQ)) {	
				// 
				
				// Exchange
				akka.amqp.ExchangeType directExchange = akka.amqp.Direct.getInstance();
				akka.amqp.AMQP.ExchangeParameters params = new akka.amqp.AMQP.ExchangeParameters(getRabbitMQQueue(), directExchange);
				
				// Producer
				akka.amqp.AMQP.ProducerParameters producerParams = new akka.amqp.AMQP.ProducerParameters(params);
				
				// Connection
				com.rabbitmq.client.Address address = new com.rabbitmq.client.Address(Play.configuration.getProperty("elasticsearch.rabbitmq.host"), Integer.valueOf(Play.configuration.getProperty("elasticsearch.rabbitmq.port")));
				com.rabbitmq.client.Address[] addresses = {address};
				akka.amqp.AMQP.ConnectionParameters connectionParameters = new akka.amqp.AMQP.ConnectionParameters(addresses, Play.configuration.getProperty("elasticsearch.rabbitmq.username"), Play.configuration.getProperty("elasticsearch.rabbitmq.password"), Play.configuration.getProperty("elasticsearch.rabbitmq.virtualHost"));
				akka.actor.ActorRef connection = akka.amqp.AMQP.newConnection(connectionParameters);
				
				// Send Event
				akka.actor.ActorRef producer = akka.amqp.AMQP.newProducer(connection, producerParams);
				producer.sendOneWay(event);
				
			} else {
				ElasticSearchIndexer.stream.publish(event);
			}
		}
	}

}
