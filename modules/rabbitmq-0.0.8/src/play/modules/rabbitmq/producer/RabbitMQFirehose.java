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

import java.util.List;

import play.Logger;
import play.jobs.Job;
import play.modules.rabbitmq.util.ExceptionUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQFirehose.
 */
public abstract class RabbitMQFirehose<T> extends Job {

	/**
	 * Gets data to be loaded, loop on each one and publish them to RabbitMQ
	 * 
	 * @see play.jobs.Job#doJob()
	 */
	@Override
	public void doJob() {
		// Start Daemon
		while (true) {
			// Do Work
			try {
				// Init counter that will keep track of each message published
				int itemsCount = 0;

				// Get Data
				List<T> items = this.getData(this.batchSize());

				// Check List
				if ((items != null) && (items.size() > 0)) {
					// Set count on item
					itemsCount = items.size();

					// Publish each message
					for (T item : items) {
						try {
							RabbitMQPublisher.publish(this.queueName(), this.routingKey(item), item);

						} catch (Throwable t) {
							Logger.error(ExceptionUtil.getStackTrace(t));
						}
					}
				}

				// If null stop process
				if (items == null) {
					Logger.warn("No data available from firehose %s - quitting process...", this);
					return;
				}

				// If this batch didn't return the max number of entries put the
				// process to sleep for a litle while
				if (itemsCount < this.batchSize()) {
					Thread.sleep(this.sleepInBetweenBatches());
				}

			} catch (Throwable t) {
				// Handle Exception
				Logger.error(ExceptionUtil.getStackTrace(t));
			}
		}
	}

	/**
	 * Gets the data.
	 * 
	 * @param n
	 *            the n
	 * @return the data
	 * @throws Exception
	 *             the exception
	 */
	protected abstract List<T> getData(int n) throws Exception;
	
	/**
	 * Routing key.
	 *
	 * @param t the t
	 * @return the string
	 */
	protected String routingKey(T t) {
		return this.queueName();
	}

	/**
	 * Batch size.
	 * 
	 * @return the int
	 */
	protected abstract int batchSize();

	/**
	 * Queue name.
	 * 
	 * @return the string
	 */
	protected abstract String queueName();

	/**
	 * Sleep in between batches.
	 * 
	 * @return the long
	 */
	protected long sleepInBetweenBatches() {
		long l = 1000l; // 1 sec
		l = l * 60; // 1 minute
		l = l * 5; // 5 minutes
		return l;
	}

}
