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

import play.Play;
import play.modules.elasticsearch.rabbitmq.RabbitMQIndexEventHandler;

/**
 * The ElasticSearchDeliveryMode specifies some predefined IndexEventHandlers,
 * but also allows to specify a custom IndexEventHandler in the classpath through
 * setting the following variables in conf/application.conf
 * elasticsearch.delivery=CUSTOM
 * # the name of the custom IndexEventHandler class
 * elasticsearch.customIndexEventHandler=service.MyCustomIndexEventHandler
 */
public class ElasticSearchDeliveryMode {

	/** The LOCAL. */
	public final static ElasticSearchDeliveryMode LOCAL = new ElasticSearchDeliveryMode(new LocalIndexEventHandler());

	/** The RABBITMQ. */
    public final static ElasticSearchDeliveryMode RABBITMQ = new ElasticSearchDeliveryMode(new RabbitMQIndexEventHandler());

	private final IndexEventHandler handler;

	ElasticSearchDeliveryMode(IndexEventHandler handler) {
		this.handler = handler;
	}

	public IndexEventHandler getHandler() {
		return handler;
	}


    public static ElasticSearchDeliveryMode createCustomIndexEventHandler(String clazzName)
    {
        try {
            Class clazz = Play.classloader.loadClass(clazzName);
            IndexEventHandler handler = (IndexEventHandler) clazz.newInstance();
            return new ElasticSearchDeliveryMode(handler);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Illegal className " + clazzName + " specified or class not in classpath");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Couldn't instantiate IndexEventHandler " + clazzName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Couldn't instantiate IndexEventHandler " + clazzName);
        }
    }

    public static ElasticSearchDeliveryMode valueOf(String s)
    {
        if("LOCAL".equals(s))
            return LOCAL;
        if("RABBITMQ".equals(s))
            return RABBITMQ;
        throw new IllegalArgumentException("Unspecified Mode given: " + s);
    }

}
