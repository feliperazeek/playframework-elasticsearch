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
package play.modules.rabbitmq.stats;

import play.libs.F.EventStream;

/**
 * The Class StatsService.
 */
public class StatsService {
	
	/** The stats. */
	public play.modules.rabbitmq.stats.Stats<play.modules.rabbitmq.stats.StatsEvent> stats = new play.modules.rabbitmq.stats.Stats<play.modules.rabbitmq.stats.StatsEvent>();
	
	/** The execution stream. */
	public play.libs.F.EventStream<String> liveStream = new play.libs.F.EventStream<String>();
	
	/**
	 * Record.
	 *
	 * @param queue the queue
	 * @param type the type
	 * @param status the status
	 * @param executionTime the execution time
	 */
	public void record(String queue, StatsEvent.Type type, StatsEvent.Status status, long executionTime) {
		StatsEvent event = new StatsEvent(queue, type, status);
		this.stats.record(event, executionTime);
		String color = "green";
		if ( status.equals( StatsEvent.Status.ERROR ) ) {
			color = "red";
		}
		String coloredStatus = String.format("<font color='%s'>%s</font>", color, status);
		String msg = String.format("RabbitMQ <strong>%s</strong> Event - Queue: <strong>%s</strong>, Status: %s, Execution Time: <strong>%s milisecond(s)</strong>.", type, queue, coloredStatus, executionTime);
		liveStream.publish(msg);
	}

	/**
	 * Gets the.
	 * 
	 * @param queue
	 *            the queue
	 * @param type
	 *            the type
	 * @param status
	 *            the status
	 * @return the long
	 */
	public long executions(String queue, StatsEvent.Type type, StatsEvent.Status status) {
		StatsEvent event = new StatsEvent(queue, type, status);
		return this.stats.executions(event);
	}
	
	/**
	 * Average time.
	 *
	 * @param queue the queue
	 * @param type the type
	 * @param status the status
	 * @return the long
	 */
	public long averageTime(String queue, StatsEvent.Type type, StatsEvent.Status status) {
		StatsEvent event = new StatsEvent(queue, type, status);
		return this.stats.averageTime(event);
	}
}
