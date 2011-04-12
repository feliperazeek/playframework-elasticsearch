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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import play.modules.elasticsearch.adapter.ElasticSearchAdapter;
import play.modules.elasticsearch.util.ExceptionUtil;
import play.mvc.Router;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchPlugin.
 */
public class ElasticSearchPlugin extends PlayPlugin {

	/** The started. */
	private static boolean started = false;

	/** The model index. */
	private static Map<Class<?>, Boolean> modelIndex = null;

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
	 * This method is called when the application starts - It will start ES
	 * instance
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// Start Model Map
		modelIndex = new HashMap<Class<?>, Boolean>();

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
		Router.addRoute("GET", "/es-admin/", "ElasticSearchAdmin.index");
		Router.addRoute("GET", "/es-admin/lib", "staticDir:elasticsearch_public");

		// Check Client
		if (client == null) {
			throw new RuntimeException("Elastic Search Client cannot be null - please check the configuration provided and the health of your Elastic Search instances.");
		}
	}

	/**
	 * Checks if is elastic searchable.
	 * 
	 * @param o
	 *            the o
	 * @return true, if is elastic searchable
	 */
	private boolean isElasticSearchable(Object o) {
		Class<?> clazz = o.getClass();
		while (clazz != null) {
			// Logger.info("Class: %s", clazz);
			for (Annotation a : clazz.getAnnotations()) {
				// Logger.info("Class: %s - Annotation: %s", clazz,
				// a.toString());
				if (a.toString().indexOf("ElasticSearchable") > -1) {
					return true;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return false;
	}

	/**
	 * This is the method that will be sending data to ES instance
	 * 
	 * @see play.PlayPlugin#onEvent(java.lang.String, java.lang.Object)
	 */
	@Override
	public void onEvent(String message, Object context) {
		// Log Debug
		// Logger.info("Event: %s - Object: %s", message, context);

		// Just accept JPA events
		if (!StringUtils.startsWith(message, "JPASupport.")) {
			return;
		}

		// Check if object has annotation
		boolean isSearchable = this.isElasticSearchable(context);
		// Logger.info("Searchable: %s", isSearchable);
		if (isSearchable == false) {
			// Logger.debug("Not marked to be elastic searchable!");
			return;
		}

		// Get Plugin
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);

		// Check if the index has been started
		Class<?> clazz = context.getClass();
		if (modelIndex.containsKey(clazz) == false) {
			Logger.info("Start Index for Class: %s", clazz);
			ElasticSearchAdapter.startIndex(plugin.client(), clazz);
		}

		// Do Work
		try {
			// Log Debug
			// Logger.info("Elastic Search - " + message + " Event for: " +
			// context);

			// Check Event Type
			if (message.equals("JPASupport.objectPersisted") || message.equals("JPASupport.objectUpdated")) {
				// Index Model
				ElasticSearchAdapter.indexModel(plugin.client(), (Model) context);

			} else if (message.equals("JPASupport.objectDeleted")) {
				// Delete Model fromIndex
				ElasticSearchAdapter.deleteModel(plugin.client(), (Model) context);
			}

			// Log Debug
			Logger.debug("Elastic Event Done!");

		} catch (Exception e) {
			Logger.error(e, "Problem updating entity %s on event %s with error %s", context, message, e.getMessage());
		}
	}

}