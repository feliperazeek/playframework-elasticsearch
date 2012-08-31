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

import play.Logger;
import play.Play;
import play.modules.elasticsearch.rabbitmq.RabbitMQIndexEventHandler;

/**
 * The ElasticSearchDeliveryMode specifies some predefined IndexEventHandlers, but also allows to specify a custom IndexEventHandler in the classpath through setting the following variables in conf/application.conf elasticsearch.delivery=CUSTOM # the name of the custom IndexEventHandler class elasticsearch.customIndexEventHandler=service.MyCustomIndexEventHandler
 */
public class ElasticSearchDeliveryMode {

	/** The LOCAL. */
	public final static ElasticSearchDeliveryMode LOCAL = new ElasticSearchDeliveryMode(new LocalIndexEventHandler());

	/** The RABBITMQ. */
	public final static ElasticSearchDeliveryMode RABBITMQ = new ElasticSearchDeliveryMode(new RabbitMQIndexEventHandler());

	/** The Synchronous. */
	public final static ElasticSearchDeliveryMode SYNCHRONOUS = new ElasticSearchDeliveryMode(new SynchronousIndexEventHandler());

	/** The Discard all messages. */
	public final static ElasticSearchDeliveryMode DISCARD = new ElasticSearchDeliveryMode(new IndexEventHandler() {
		@Override
		public void handle(final ElasticSearchIndexEvent event) {
			Logger.info("Discarding index event %s", event);
		}
	});

	private final IndexEventHandler handler;

	ElasticSearchDeliveryMode(final IndexEventHandler handler) {
		this.handler = handler;
	}

	public IndexEventHandler getHandler() {
		return handler;
	}

	public static ElasticSearchDeliveryMode createCustomIndexEventHandler(final String clazzName) {
		try {
			final Class clazz = Play.classloader.loadClass(clazzName);
			final IndexEventHandler handler = (IndexEventHandler) clazz.newInstance();
			return new ElasticSearchDeliveryMode(handler);
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("Illegal className " + clazzName + " specified or class not in classpath");
		} catch (final InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate IndexEventHandler " + clazzName);
		} catch (final IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate IndexEventHandler " + clazzName);
		}
	}

	public static ElasticSearchDeliveryMode valueOf(final String s) {
		if ("LOCAL".equals(s))
			return LOCAL;
		if ("RABBITMQ".equals(s))
			return RABBITMQ;
		if ("SYNCHRONOUS".equals(s))
			return SYNCHRONOUS;
		if ("DISCARD".equals(s))
			return DISCARD;
		throw new IllegalArgumentException("Unspecified Mode given: " + s);
	}

}
