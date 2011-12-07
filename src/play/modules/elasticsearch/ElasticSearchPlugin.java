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

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.Model;
import play.modules.elasticsearch.ElasticSearchIndexEvent.Type;
import play.modules.elasticsearch.adapter.ElasticSearchAdapter;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.modules.elasticsearch.util.ExceptionUtil;
import play.modules.elasticsearch.util.ReflectionUtil;
import play.mvc.Router;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchPlugin.
 */
public class ElasticSearchPlugin extends PlayPlugin {

	/** The started. */
	private static boolean started = false;

	/** The mappers index. */
	private static Map<Class<?>, ModelMapper<?>> mappers = null;
	
	/** The started indices. */
	private static Set<Class<?>> indicesStarted = null;

	/** The client. */
	private static Client client = null;

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public static Client client() {
		return client;
	}

	/**
	 * Checks if is local mode.
	 * 
	 * @return true, if is local mode
	 */
	private boolean isLocalMode() {
		try {
			String client = Play.configuration.getProperty("elasticsearch.client");
			Boolean local = Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.local", "true"));

			if (client == null) {
				return true;
			}
			if (client.equalsIgnoreCase("false") || client.equalsIgnoreCase("true")) {
				return true;
			}

			return local;
		} catch (Exception e) {
			Logger.error("Error! Starting in Local Model: %s", ExceptionUtil.getStackTrace(e));
			return true;
		}
	}

	/**
	 * Gets the hosts.
	 * 
	 * @return the hosts
	 */
	public static String getHosts() {
		String s = Play.configuration.getProperty("elasticsearch.client");
		if (s == null) {
			return "";
		}
		return s;
	}

	/**
	 * Gets the delivery mode.
	 * 
	 * @return the delivery mode
	 */
	public static ElasticSearchDeliveryMode getDeliveryMode() {
		String s = Play.configuration.getProperty("elasticsearch.delivery");
		if (s == null) {
			return ElasticSearchDeliveryMode.LOCAL;
		}
		return ElasticSearchDeliveryMode.valueOf(s.toUpperCase());
	}

	/**
	 * This method is called when the application starts - It will start ES
	 * instance
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// (re-)set caches
		mappers = new HashMap<Class<?>, ModelMapper<?>>();
		indicesStarted = new HashSet<Class<?>>();
		ReflectionUtil.clearCache();

		// Make sure it doesn't get started more than once
		if ((client != null) || started) {
			Logger.debug("Elastic Search Started Already!");
			return;
		}

		// Start Node Builder
		Builder settings = ImmutableSettings.settingsBuilder();
		settings.put("client.transport.sniff", true);
		settings.build();

		// Check Model
		if (this.isLocalMode()) {
			Logger.info("Starting Elastic Search for Play! in Local Mode");
			NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
			Node node = nb.node();
			client = node.client();

		} else {
			Logger.info("Connecting Play! to Elastic Search in Client Mode");
			TransportClient c = new TransportClient(settings);
			if (Play.configuration.getProperty("elasticsearch.client") == null) {
				throw new RuntimeException("Configuration required - elasticsearch.client when local model is disabled!");
			}
			String[] hosts = getHosts().trim().split(",");
			boolean done = false;
			for (String host : hosts) {
				String[] parts = host.split(":");
				if (parts.length != 2) {
					throw new RuntimeException("Invalid Host: " + host);
				}
				Logger.info("Transport Client - Host: %s Port: %s", parts[0], parts[1]);
				c.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.valueOf(parts[1])));
				done = true;
			}
			if (done == false) {
				throw new RuntimeException("No Hosts Provided for Elastic Search!");
			}
			client = c;
		}

		// Bind Admin
		Router.addRoute("GET", "/es-admin", "elasticsearch.ElasticSearchAdmin.index");

		// Check Client
		if (client == null) {
			throw new RuntimeException("Elastic Search Client cannot be null - please check the configuration provided and the health of your Elastic Search instances.");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <M> ModelMapper<M> getMapper(Class<M> clazz) {
		if (mappers.containsKey(clazz)) {
			return (ModelMapper<M>) mappers.get(clazz);
		}
		
		ModelMapper<M> mapper = MapperFactory.getMapper(clazz);
		mappers.put(clazz, mapper);
		
		return mapper;
	}
	
	private static void startIndexIfNeeded(Class<Model> clazz) {
		if (!indicesStarted.contains(clazz)) {
			ModelMapper<Model> mapper = getMapper(clazz);
			Logger.info("Start Index for Class: %s", clazz);
			ElasticSearchAdapter.startIndex(client(), mapper);
			indicesStarted.add(clazz);
		}
	}
	
	private static boolean isInterestingEvent(String event) {
		return event.endsWith(".objectPersisted") || event.endsWith(".objectUpdated") || event.endsWith(".objectDeleted");
	}

	/**
	 * This is the method that will be sending data to ES instance
	 * 
	 * @see play.PlayPlugin#onEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onEvent(String message, Object context) {
		// Log Debug
		Logger.info("Received %s Event, Object: %s", message, context);

		if (isInterestingEvent(message) == false) {
			return;
		}
		
		Logger.debug("Processing %s Event", message);

		// Check if object is searchable
		if (MappingUtil.isSearchable(context.getClass()) == false) {
			return;
		}
		
		// Sanity check, we only index models
		Validate.isTrue(context instanceof Model, "Only play.db.Model subclasses can be indexed");
		
		// Start index if needed
		@SuppressWarnings("unchecked")
		Class<Model> clazz = (Class<Model>) context.getClass();
		startIndexIfNeeded(clazz);

		// Define Event
		ElasticSearchIndexEvent event = null;
		if (message.endsWith(".objectPersisted") || message.endsWith(".objectUpdated")) {
			// Index Model
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.INDEX);

		} else if (message.endsWith(".objectDeleted")) {
			// Delete Model from Index
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.DELETE);
		}

		// Sync with Elastic Search
		Logger.info("Elastic Search Index Event: %s", event);
		if (event != null) {
			ElasticSearchDeliveryMode deliveryMode = getDeliveryMode();
			IndexEventHandler handler = deliveryMode.getHandler();
			handler.handle(event);
		}
	}
	
	<M extends Model> void index(M model) {
		@SuppressWarnings("unchecked")
		Class<Model> clazz = (Class<Model>) model.getClass();
		
		// Check if object is searchable
		if (MappingUtil.isSearchable(clazz) == false) {
			throw new IllegalArgumentException("model is not searchable");
		}
		
		startIndexIfNeeded(clazz);
		
		ElasticSearchIndexEvent event = new ElasticSearchIndexEvent(model, Type.INDEX);
		ElasticSearchDeliveryMode deliveryMode = getDeliveryMode();
		IndexEventHandler handler = deliveryMode.getHandler();
		handler.handle(event);
	}

}
