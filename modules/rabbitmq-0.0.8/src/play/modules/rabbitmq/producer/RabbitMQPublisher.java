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
package play.modules.rabbitmq.producer;

import play.Logger;
import play.Play;
import play.jobs.Job;
import play.modules.rabbitmq.RabbitMQPlugin;
import play.modules.rabbitmq.util.ExceptionUtil;

import com.rabbitmq.client.Channel;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQPublisher.
 */
public abstract class RabbitMQPublisher {

	/**
	 * Publish.
	 * 
	 * @param queueName
	 *            the queue name
	 * @param message
	 *            the message
	 */
	public static void publish(String queueName, Object message) {
		try {
			new RabbitMQPublisherJob(queueName, message).doJobWithResult();
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
		}
	}
	
	/**
	 * Publish.
	 *
	 * @param queueName the queue name
	 * @param routingKey the routing key
	 * @param message the message
	 */
	public static void publish(String queueName, String routingKey, Object message) {
		try {
			new RabbitMQPublisherJob(queueName, routingKey, message).doJobWithResult();
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * The Class RabbitMQPublisherJob.
	 */
	protected static class RabbitMQPublisherJob extends Job {

		/** The message. */
		private Object message;
		
		/** The routing key. */
		private String routingKey;

		/** The queue name. */
		private String queueName;
		
		/**
		 * Instantiates a new rabbit mq publisher job.
		 *
		 * @param queueName the queue name
		 * @param message the message
		 */
		public RabbitMQPublisherJob(String queueName, Object message) {
			this(queueName, queueName, message);
		}

		/**
		 * Instantiates a new rabbit mq publisher.
		 * 
		 * @param queueName
		 *            the queue name
		 * @param message
		 *            the message
		 */
		public RabbitMQPublisherJob(String queueName, String routingKey, Object message) {
			this.queueName = queueName;
			this.routingKey = routingKey;
			this.message = message;
		}

		/**
		 * Deliver Message.
		 * 
		 * @see play.jobs.Job#doJob()
		 */
		@Override
		public void doJob() {
			// Do Work
			Channel channel = null;
			long executionTime = 0l;
			try {
				// Start Timer
				long start = new java.util.Date().getTime();

				// Get Producer Information
				RabbitMQProducer producer = this.getClass().getAnnotation(RabbitMQProducer.class);
				if ((producer == null) && (this.queueName == null)) {
					throw new RuntimeException("Please define annotation @RabbitMQProducer.");
				}

				// Create Channel
				RabbitMQPlugin plugin = Play.plugin(RabbitMQPlugin.class);
				channel = plugin.createChannel(this.queueName, this.routingKey);
				if (channel == null) {
					throw new RuntimeException("Error creating a communication channel with RabbitMQ. Please verify the health of your RabbitMQ node and check your configuration.");
				}

				// Publish Message
				channel.basicPublish(this.queueName, this.routingKey, plugin.getBasicProperties(), this.getBytes());

				// Execution Time
				executionTime = new java.util.Date().getTime() - start;
				Logger.info("Message %s has been published to queue %s (execution time: %s ms)", this.message, this.queueName, executionTime);

				// Update Stats
				play.modules.rabbitmq.RabbitMQPlugin.statsService().record(this.queueName, play.modules.rabbitmq.stats.StatsEvent.Type.PRODUCER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS, executionTime);

			} catch (Throwable t) {
				// Handle Exception
				Logger.error(ExceptionUtil.getStackTrace(t));

				// Update Stats
				play.modules.rabbitmq.RabbitMQPlugin.statsService().record(this.queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR, executionTime);
			
			} finally {
				// Close Channel
				if ( channel != null ) {
					try {
						if ( channel.getConnection() != null && channel.getConnection().isOpen() ) {
							channel.getConnection().close();
						}
						if ( channel != null && channel.isOpen() ) {
							channel.close();
						}
					} catch (Throwable t) {
						Logger.error(ExceptionUtil.getStackTrace(t));
					}
					channel = null;
				}
			}
		}

		/**
		 * Gets the bytes.
		 * 
		 * @return the bytes
		 * @throws Exception
		 *             the exception
		 */
		private byte[] getBytes() throws Exception {
			return RabbitMQPlugin.mapper().getBytes(this.message);
		}

	}
}
