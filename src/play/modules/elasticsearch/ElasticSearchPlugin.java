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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import play.modules.elasticsearch.mapping.impl.DefaultMapperFactory;
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

	/** Signals whether to save events instead of processing them asynchronously. */
	private static boolean blockEvents = false;

	/** The mapper factory */
	private static MapperFactory mapperFactory = new DefaultMapperFactory();

	/** The mappers index. */
	private static Map<Class<?>, ModelMapper<?>> mappers = null;

	/** The started indices. */
	private static Set<Class<?>> indicesStarted = null;

	/** Index type -> Class lookup */
	private static Map<String, Class<?>> modelLookup = null;

	/** The client. */
	private static Client client = null;

	private static final Queue<Model> blockedIndexOperations = new ConcurrentLinkedQueue<Model>();

	private static final Queue<Model> blockedDeleteOperations = new ConcurrentLinkedQueue<Model>();

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public static Client client() {
		return client;
	}

	public static void setMapperFactory(final MapperFactory factory) {
		mapperFactory = factory;
		mappers.clear();
	}

	/**
	 * Checks if is local mode.
	 * 
	 * @return true, if is local mode
	 */
	private boolean isLocalMode() {
		try {
			final String client = Play.configuration.getProperty("elasticsearch.client");
			final Boolean local = Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.local", "true"));

			if (client == null) {
				return true;
			}
			if (client.equalsIgnoreCase("false") || client.equalsIgnoreCase("true")) {
				return true;
			}

			return local;
		} catch (final Exception e) {
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
		final String s = Play.configuration.getProperty("elasticsearch.client");
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
		final String s = Play.configuration.getProperty("elasticsearch.delivery");
		if (s == null) {
			return ElasticSearchDeliveryMode.LOCAL;
		}
		if ("CUSTOM".equals(s))
			return ElasticSearchDeliveryMode.createCustomIndexEventHandler(Play.configuration.getProperty(
					"elasticsearch.customIndexEventHandler", "play.modules.elasticsearch.LocalIndexEventHandler"));
		return ElasticSearchDeliveryMode.valueOf(s.toUpperCase());
	}

	/**
	 * This method is called when the application starts - It will start ES instance
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// (re-)set caches
		mappers = new HashMap<Class<?>, ModelMapper<?>>();
		modelLookup = new HashMap<String, Class<?>>();
		indicesStarted = new HashSet<Class<?>>();
		ReflectionUtil.clearCache();

		// Make sure it doesn't get started more than once
		if ((client != null) || started) {
			Logger.debug("Elastic Search Started Already!");
			return;
		}

		// Start Node Builder
		final Builder settings = ImmutableSettings.settingsBuilder();
		//settings.put("client.transport.sniff", true);

		// Import anything from play configuration that starts with elasticsearch.native.
		Enumeration<Object> keys = Play.configuration.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith("elasticsearch.native.")) {
				String nativeKey = key.replaceFirst("elasticsearch.native.", "");
				Logger.error("Adding native [" + nativeKey + "," + Play.configuration.getProperty(key) + "]");
				settings.put(nativeKey, Play.configuration.getProperty(key));
			}
		}

		settings.build();

		// Check Model
		if (this.isLocalMode()) {
			Logger.info("Starting Elastic Search for Play! in Local Mode");
			final NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
			final Node node = nb.node();
			client = node.client();

		} else {
			Logger.info("Connecting Play! to Elastic Search in Client Mode");
			final TransportClient c = new TransportClient(settings);
			if (Play.configuration.getProperty("elasticsearch.client") == null) {
				throw new RuntimeException(
						"Configuration required - elasticsearch.client when local model is disabled!");
			}
			final String[] hosts = getHosts().trim().split(",");
			boolean done = false;
			for (final String host : hosts) {
				final String[] parts = host.split(":");
				if (parts.length != 2) {
					throw new RuntimeException("Invalid Host: " + host);
				}
				Logger.info("Transport Client - Host: %s Port: %s", parts[0], parts[1]);
				if (Integer.valueOf(parts[1]) == 9200)
					Logger.info("Note: Port 9200 is usually used by the HTTP Transport. You might want to use 9300 instead.");
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
			throw new RuntimeException(
					"Elastic Search Client cannot be null - please check the configuration provided and the health of your Elastic Search instances.");
		}
	}

	@SuppressWarnings("unchecked")
	public static <M> ModelMapper<M> getMapper(final Class<M> clazz) {
		if (mappers.containsKey(clazz)) {
			return (ModelMapper<M>) mappers.get(clazz);
		}

		final ModelMapper<M> mapper = mapperFactory.getMapper(clazz);
		mappers.put(clazz, mapper);
		modelLookup.put(mapper.getTypeName(), clazz);

		return mapper;
	}

	private static void startIndexIfNeeded(final Class<Model> clazz) {
		if (!indicesStarted.contains(clazz)) {
			final ModelMapper<Model> mapper = getMapper(clazz);
			Logger.info("Start Index for Class: %s", clazz);
			ElasticSearchAdapter.startIndex(client(), mapper);
			indicesStarted.add(clazz);
		}
	}

	private static boolean isInterestingEvent(final String event) {
		return event.endsWith(".objectPersisted") || event.endsWith(".objectUpdated")
				|| event.endsWith(".objectDeleted");
	}

	/**
	 * This is the method that will be sending data to ES instance
	 * 
	 * @see play.PlayPlugin#onEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onEvent(final String message, final Object context) {
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
		final Class<Model> clazz = (Class<Model>) context.getClass();
		startIndexIfNeeded(clazz);

		// Define Event
		ElasticSearchIndexEvent event = null;
		if (message.endsWith(".objectPersisted") || message.endsWith(".objectUpdated")) {
			// Index Model
			if (blockEvents) {
				// If blocked, just enqueue the operation
				blockedIndexOperations.offer((Model) context);
				return;
			}
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.INDEX);

		} else if (message.endsWith(".objectDeleted")) {
			// Delete Model from Index
			if (blockEvents) {
				// If blocked, just enqueue the operation
				blockedDeleteOperations.offer((Model) context);
				return;
			}
			event = new ElasticSearchIndexEvent((Model) context, ElasticSearchIndexEvent.Type.DELETE);
		}

		// Sync with Elastic Search
		Logger.info("Elastic Search Index Event: %s", event);
		if (event != null) {
			final ElasticSearchDeliveryMode deliveryMode = getDeliveryMode();
			final IndexEventHandler handler = deliveryMode.getHandler();
			handler.handle(event);
		}
	}

	<M extends Model> void index(final M model) {
		@SuppressWarnings("unchecked")
		final Class<Model> clazz = (Class<Model>) model.getClass();

		// Check if object is searchable
		if (MappingUtil.isSearchable(clazz) == false) {
			throw new IllegalArgumentException("model is not searchable");
		}

		startIndexIfNeeded(clazz);

		final ElasticSearchIndexEvent event = new ElasticSearchIndexEvent(model, Type.INDEX);
		final ElasticSearchDeliveryMode deliveryMode = getDeliveryMode();
		final IndexEventHandler handler = deliveryMode.getHandler();
		handler.handle(event);
	}

	public static void batchProcessBlockedOperations() {
		Model model;
		try {
			while ((model = blockedIndexOperations.poll()) != null) {
				@SuppressWarnings("unchecked")
				final ModelMapper<Model> mapper = (ModelMapper<Model>) getMapper(model.getClass());

				ElasticSearchAdapter.indexModel(client, mapper, model);
			}
			while ((model = blockedDeleteOperations.poll()) != null) {
				@SuppressWarnings("unchecked")
				final ModelMapper<Model> mapper = (ModelMapper<Model>) getMapper(model.getClass());
				ElasticSearchAdapter.deleteModel(client, mapper, model);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isBlockEvents() {
		return blockEvents;
	}

	public static void setBlockEvents(final boolean blockEvents) {
		ElasticSearchPlugin.blockEvents = blockEvents;
	}

	/**
	 * Looks up the model class based on the index type name
	 * 
	 * @param indexType
	 * @return Class of the Model
	 */
	public static Class<?> lookupModel(final String indexType) {
		return modelLookup.get(indexType);
	}

}
