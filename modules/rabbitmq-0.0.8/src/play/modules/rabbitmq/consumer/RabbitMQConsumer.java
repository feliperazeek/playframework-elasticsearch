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
package play.modules.rabbitmq.consumer;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.modules.rabbitmq.RabbitMQPlugin;
import play.modules.rabbitmq.util.ExceptionUtil;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQConsumerJob.
 * 
 * @param <T>
 *            the generic type
 */
public abstract class RabbitMQConsumer<T> extends Job<T> {

	/**
	 * This is the default value defined by "rabbitmq.retries" on
	 * application.conf (please override if you need a new value)
	 */
	public static int retries = RabbitMQPlugin.retries();

	/**
	 * Consume.
	 * 
	 * @param message
	 *            the message
	 */
	protected abstract void consume(T message);

	/**
	 * Creates the channel.
	 * 
	 * @param plugin
	 *            the plugin
	 * @return the channel
	 * @throws Exception
	 *             the exception
	 */
	protected Channel createChannel(RabbitMQPlugin plugin) throws Exception {
		// Get Plugin
		Channel channel = plugin.createChannel(this.queue(), this.routingKey());
		return channel;
	}

	/**
	 * Creates the channel.
	 * 
	 * @param channel
	 *            the channel
	 * @param plugin
	 *            the plugin
	 * @return the channel
	 * @throws Exception
	 *             the exception
	 */
	protected QueueingConsumer createConsumer(Channel channel, RabbitMQPlugin plugin) throws Exception {
		// Get Plugin
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(this.queue(), plugin.isAutoAck(), consumer);

		// Log Debug
		Logger.info("RabbitMQ Consumer - Channel: %s, Consumer: %s " + channel, consumer);

		// Return Channel
		return consumer;
	}

	/**
	 * Let our baby go!.
	 * 
	 * @see play.jobs.Job#doJob()
	 */
	@Override
	public void doJob() {
		this.goGetHerSon();
	}

	/**
	 * Gets the message type.
	 * 
	 * @return the message type
	 */
	protected abstract Class getMessageType();

	/**
	 * Go get her son.
	 */
	private void goGetHerSon() {
		// Get Plugin
		RabbitMQPlugin plugin = Play.plugin(RabbitMQPlugin.class);

		// Define Channel
		Channel channel = null;
		QueueingConsumer consumer = null;
		Long deliveryTag = null;

		// Get Channel
		while (true) {
			// Log Debug
			Logger.info("Entering main loop on consumer: " + this);
			
			try {
				// Create Channel
				if (channel == null || (channel != null && channel.isOpen() == false)) {
					consumer = null;
					channel = this.createChannel(plugin);
				}

				// Create Consumer
				if (consumer == null) {
					consumer = this.createConsumer(channel, plugin);
				}

				// Get Task
				QueueingConsumer.Delivery task = consumer.nextDelivery();

				// Date Night
				if ((task != null) && (task.getBody() != null)) {
					try {
						// Fire job that will pass the message to the consumer,
						// ack the queue and do the retry logic
						deliveryTag = task.getEnvelope().getDeliveryTag();
						T message = this.toObject(task.getBody());
						new RabbitMQMessageConsumerJob(channel, deliveryTag, this.queue(), this, message, this.retries()).doJobWithResult();

					} catch (Throwable t) {
						// Handle Exception
						Logger.error(ExceptionUtil.getStackTrace(t));
					}

				}
				
			} catch (Throwable t) {
				Logger.error("Error creating consumer channel to RabbitMQ, retrying in a few seconds. Exception: %s", ExceptionUtil.getStackTrace(t));
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.error(ExceptionUtil.getStackTrace(t));
				}
				
			} finally {
				if ( channel != null ) {
					// Now tell Daddy everything is cool
					try {
						if ( deliveryTag != null && channel.isOpen() ) {
							channel.basicAck(deliveryTag, false);
						}
					} catch (Throwable e) {
						Logger.error(ExceptionUtil.getStackTrace("Error doing a basicAck for tag: " + deliveryTag, e));
					}
					try {
						if (  channel.getConnection() != null && channel.getConnection().isOpen() ) {
							channel.getConnection().close();
						}
						if ( channel.isOpen() == true ) {
							channel.close();
						}
					} catch (Throwable t) {
						Logger.error(ExceptionUtil.getStackTrace(t));
					} finally {
						channel = null;
					}
				}
			}
		}
	}

	/**
	 * Gets the queue name.
	 * 
	 * @return the queue name
	 */
	protected abstract String queue();
	
	/**
	 * Routing key.
	 *
	 * @param t the t
	 * @return the string
	 */
	protected String routingKey() {
		return this.queue();
	}

	/**
	 * Retries.
	 * 
	 * @return the int
	 */
	protected int retries() {
		return retries;
	}

	/**
	 * To object.
	 * 
	 * @param bytes
	 *            the bytes
	 * @return the object
	 * @throws Exception
	 *             the exception
	 */
	protected T toObject(byte[] bytes) throws Exception {
		return (T) RabbitMQPlugin.mapper().getObject(this.getMessageType(), bytes);
	}

}