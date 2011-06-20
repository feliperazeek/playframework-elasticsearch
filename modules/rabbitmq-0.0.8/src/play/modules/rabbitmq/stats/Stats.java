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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import play.Logger;
import play.libs.F.EventStream;
import play.libs.F.Promise;

import play.modules.rabbitmq.util.ExceptionUtil;

/**
 * The Class Stats.
 * 
 * @param <KEY>
 *            the generic type
 */
public class Stats<KEY> {

	/** The map. */
	protected ConcurrentHashMap<KEY, ExecutionTimes> map;

	/** The execution stream. */
	public EventStream<ExecutionEvent> executionStream;

	/** The executor. */
	protected ExecutorService executor;

	/** The debug. */
	protected boolean debug = true;

	/**
	 * Instantiates a new stats.
	 */
	public Stats() {
		this.map = new ConcurrentHashMap<KEY, ExecutionTimes>();
		this.executionStream = new EventStream<ExecutionEvent>();
		this.executor = Executors.newFixedThreadPool(5);
		this.now();
	}

	/**
	 * Log.
	 * 
	 * @param msg
	 *            the msg
	 */
	protected void log(String msg) {
		if (this.debug) {
			Logger.info(msg);
		}
	}

	/**
	 * Now.
	 */
	public void now() {
		try {
			this.executor.submit(new Callable<Stats>() {
				@Override
				public Stats call() throws Exception {
					Stats.this.log("Firing job to listen on execution stream");
					while (true) {
						Stats.this.log("Waiting on Execution Event...");
						Stats.this.await(Stats.this.executionStream.nextEvent());
					}
				}

			});
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * The Class ExecutionEvent.
	 */
	public class ExecutionEvent {

		/** The key. */
		public KEY key;

		/** The execution time. */
		public long executionTime;

		/**
		 * Instantiates a new execution event.
		 * 
		 * @param key
		 *            the key
		 * @param executionTime
		 *            the execution time
		 */
		public ExecutionEvent(KEY key, long executionTime) {
			super();
			this.key = key;
			this.executionTime = executionTime;
		}
		
		/**
		 * To String
		 */
		public String toString() {
			return "RabbitMQ Execution Event - Key: " + key + ", Execution Time: " + executionTime + " milisecond(s).";
		}
	}

	/**
	 * The Class ExecutionTimes.
	 */
	protected static class ExecutionTimes {

		/** The executions. */
		protected AtomicLong executions;

		/** The execution times. */
		protected AtomicLong executionTimes;

		/**
		 * Instantiates a new execution times.
		 */
		protected ExecutionTimes() {
			this.executions = new AtomicLong();
			this.executionTimes = new AtomicLong();
		}

	}

	/**
	 * Executions.
	 * 
	 * @param key
	 *            the key
	 * @return the long
	 */
	public long executions(KEY key) {
		ExecutionTimes et = this.map.get(key);
		if (et == null) {
			return 0l;
		}
		for (;;) {
			long current = et.executions.get();
			return current;
		}
	}

	/**
	 * Execution times.
	 * 
	 * @param key
	 *            the key
	 * @return the long
	 */
	public long executionTimes(KEY key) {
		ExecutionTimes et = this.map.get(key);
		if (et == null) {
			return 0l;
		}
		for (;;) {
			long current = et.executionTimes.get();
			return current;
		}
	}

	/**[
	 * Record.
	 * 
	 * @param key
	 *            the key
	 * @param executionTime
	 *            the execution time
	 */
	public void record(KEY key, long executionTime) {
		this.executionStream.publish(new Stats.ExecutionEvent(key, executionTime));
	}

	/**
	 * Await.
	 * 
	 * @param promise
	 *            the promise
	 */
	protected void await(Promise<ExecutionEvent> promise) {
		try {
			ExecutionEvent e = promise.get();
			this.log("Received Execution Event: " + e);

			this.map.putIfAbsent(e.key, new ExecutionTimes());
			ExecutionTimes et = this.map.get(e.key);
			if (et == null) {
				throw new RuntimeException("Invalid Key: " + et);
			}

			for (;;) {
				long execs = et.executions.incrementAndGet();
				this.log("Executions: " + execs);
				break;
			}

			for (;;) {
				long times = et.executionTimes.getAndAdd(e.executionTime);
				this.log("Execution Times: " + times);
				break;
			}

		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * Average time.
	 * 
	 * @param key
	 *            the key
	 * @return the long
	 */
	public long averageTime(KEY key) {
		long executions = this.executions(key);
		long times = this.executionTimes(key);
		if ( executions <= 0 ) {
			return 0;
		}
		return times / executions;
	}

}
