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
package controllers;

import play.modules.rabbitmq.sample.RabbitMQSampleConsumer;
import play.modules.rabbitmq.sample.RabbitMQSampleFirehose;
import play.mvc.Controller;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQStats.
 */
public class RabbitMQStats extends Controller {

	/** The service. */
	private static play.modules.rabbitmq.stats.StatsService service = play.modules.rabbitmq.RabbitMQPlugin.statsService();

	/**
	 * Index.
	 */
	public static void index() {
		render();
	}

	/**
	 * Stream.
	 */
	public static void stream() {
		render();
	}

	/**
	 * Queue stats.
	 * 
	 * @param queueName
	 *            the queue name
	 */
	public static void queueStats(String queueName) {
		render(queueName);
	}

	/**
	 * Queue stats details.
	 * 
	 * @param queueName
	 *            the queue name
	 */
	public static void queueStatsDetails(String queueName) {
		long producerSuccess = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.PRODUCER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS);
		long producerFailed = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.PRODUCER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR);
		long consumerSuccess = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS);
		long consumerFailed = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR);
		long producerSuccessAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.PRODUCER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS);
		long producerFailedAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.PRODUCER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR);
		long consumerSuccessAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS);
		long consumerFailedAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR);

		long consumerSuccessFirstAttempt = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS_FIRST_ATTEMPT);
		long consumerFailedFirstAttempt = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR_FIRST_ATTEMPT);
		long consumerSuccessAfterRetry = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS_AFTER_RETRY);
		long consumerFailedAfterRetry = service.executions(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR_AFTER_RETRY);

		long consumerSuccessFirstAttemptAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS_FIRST_ATTEMPT);
		long consumerFailedFirstAttemptAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR_FIRST_ATTEMPT);
		long consumerSuccessAfterRetryAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.SUCCESS_AFTER_RETRY);
		long consumerFailedAfterRetryAverageTime = service.averageTime(queueName, play.modules.rabbitmq.stats.StatsEvent.Type.CONSUMER, play.modules.rabbitmq.stats.StatsEvent.Status.ERROR_AFTER_RETRY);

		render(queueName, producerSuccess, producerFailed, consumerSuccess, consumerFailed, producerSuccessAverageTime, producerFailedAverageTime, consumerSuccessAverageTime, consumerFailedAverageTime, consumerSuccessFirstAttempt, consumerFailedFirstAttempt, consumerSuccessAfterRetry, consumerFailedAfterRetry, consumerSuccessFirstAttemptAverageTime, consumerFailedFirstAttemptAverageTime, consumerSuccessAfterRetryAverageTime, consumerFailedAfterRetryAverageTime);
	}

	/**
	 * Fire sample firehose.
	 */
	public static void fireSampleQueue() {
		new RabbitMQSampleConsumer().now();
		new RabbitMQSampleFirehose().now();
		render();
	}

}
