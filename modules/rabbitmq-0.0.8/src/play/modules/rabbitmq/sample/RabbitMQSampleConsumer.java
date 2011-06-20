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
package play.modules.rabbitmq.sample;

import play.modules.rabbitmq.RabbitMQPlugin;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQSampleConsumer.
 */
// You need to uncomment the line below to allow the job to get triggered
// @OnApplicationStart(async = true)
public class RabbitMQSampleConsumer extends RabbitMQConsumer<SampleMessage> {

	/**
	 * Consume Message
	 * 
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#consume(java.lang.Object)
	 */
	@Override
	protected void consume(SampleMessage message) {
		java.util.Random r = new java.util.Random();
		boolean b = r.nextBoolean();
		if ( b == false ) {
			//throw new RuntimeException("Unexpected error processing message: " + message);
		}
		System.out.println("******************************");
		System.out.println("* Message Consumed: " + message);
		System.out.println("******************************");
	}

	/**
	 * Name of the Queue that this consumer will be listening to.
	 * 
	 * @return the string
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#queue()
	 */
	@Override
	protected String queue() {
		return "myQueue";
	}

	/**
	 * Number of times we'll try to re-deliver the message if any exception
	 * happens
	 * 
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#retries()
	 */
	@Override
	protected int retries() {
		// This is the default value defined by "rabbitmq.retries" on
		// application.conf (please override if you need a new value)
		return RabbitMQPlugin.retries();
	}

	/**
	 * Return message type
	 * 
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#getMessageType()
	 */
	@Override
	protected Class getMessageType() {
		return SampleMessage.class;
	}
}