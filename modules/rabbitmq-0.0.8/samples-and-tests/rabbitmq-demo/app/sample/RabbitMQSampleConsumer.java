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
package sample;

import play.jobs.OnApplicationStart;
import play.modules.rabbitmq.consumer.RabbitMQConsumer;

// TODO: Auto-generated Javadoc
/**
 * The Class RabbitMQSampleConsumer.
 */
@OnApplicationStart(async=true)
public class RabbitMQSampleConsumer extends RabbitMQConsumer<SampleMessage> {

	/**
	 * Consume Message
	 * 
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#consume(T)
	 */
	@Override
	protected void consume(SampleMessage message) {
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
	 * Return message type.
	 *
	 * @return the message type
	 * @see play.modules.rabbitmq.consumer.RabbitMQConsumer#getMessageType()
	 */
	protected Class getMessageType() {
		return SampleMessage.class;
	}
}

