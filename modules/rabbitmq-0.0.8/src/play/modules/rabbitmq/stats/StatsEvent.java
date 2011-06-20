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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class StatsEvent.
 */
public class StatsEvent {

	/** The queue. */
	public String queue;

	/** The type. */
	public StatsEvent.Type type;

	/** The status. */
	public StatsEvent.Status status;

	/**
	 * Instantiates a new rabbit mq stats item.
	 * 
	 * @param queue
	 *            the queue
	 * @param type
	 *            the type
	 * @param status
	 *            the status
	 */
	public StatsEvent(String queue, Type type, Status status) {
		super();
		this.queue = queue;
		this.type = type;
		this.status = status;
	}

	/**
	 * The Enum Type.
	 */
	public static enum Type {

		/** The CONSUMER. */
		CONSUMER,

		/** The PRODUCER. */
		PRODUCER;

	}

	/**
	 * The Enum Status.
	 */
	public static enum Status {

		/** The SUCCESS. */
		SUCCESS,
		
		/** The SUCCES s_ afte r_ retry. */
		SUCCESS_AFTER_RETRY,
		
		/** The SUCCES s_ firs t_ attempt. */
		SUCCESS_FIRST_ATTEMPT,

		/** The ERROR. */
		ERROR,
		
		/** The ERRO r_ afte r_ retry. */
		ERROR_AFTER_RETRY,
		
		/** The ERRO r_ firs t_ attempt. */
		ERROR_FIRST_ATTEMPT;

	}

	/**
	 * Hash Code
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.queue == null) ? 0 : this.queue.hashCode());
		result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		return result;
	}

	/**
	 * Equals
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		StatsEvent other = (StatsEvent) obj;
		if (this.queue == null) {
			if (other.queue != null) {
				return false;
			}
		} else if (!this.queue.equals(other.queue)) {
			return false;
		}
		if (this.status != other.status) {
			return false;
		}
		if (this.type != other.type) {
			return false;
		}
		return true;
	}

	/**
	 * To String
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		return df.format(new Date()) + " - queue: " + this.queue + ", type: " + this.type + ", status: " + this.status;
	}

}
