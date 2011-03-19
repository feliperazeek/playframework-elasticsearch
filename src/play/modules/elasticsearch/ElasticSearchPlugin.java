package play.modules.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.jpa.Model;
import play.mvc.Router;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchPlugin.
 */
public class ElasticSearchPlugin extends PlayPlugin {

	/** The _session. */
	private ThreadLocal<Client> _session = new ThreadLocal<Client>();

	/** The started. */
	public static boolean started = false;

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public Client client() {
		return _session.get();
	}

	/**
	 * Elastic Search Start.
	 * 
	 * @see play.PlayPlugin#onApplicationStart()
	 */
	@Override
	public void onApplicationStart() {
		// Make sure it doesn't get started more than once
		if (_session.get() != null || started) {
			Logger.debug("Elastic Search Started Already!");
			return;
		}

		// Start Node Builder
		Builder settings = ImmutableSettings.settingsBuilder();
		settings.build();
		NodeBuilder nb = nodeBuilder().settings(settings);

		// Check Local Mode
		boolean localMode = false;
		if (!Play.configuration.containsKey("elasticsearch.local")) {
			localMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.local"));
			nb = nb.local(localMode);
		}

		// Check Client Mode
		boolean clientMode = false;
		if (!Play.configuration.containsKey("elasticsearch.client")) {
			clientMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.client"));
			nb = nb.client(clientMode);
		}

		// Default to Local Mode
		if (localMode == false && clientMode == false) {
			localMode = true;
			nb = nb.local(localMode);
		}

		// Log Debug
		if (localMode) {
			Logger.info("Starting Elastic Search for Play! in Local Mode");
		} else {
			Logger.info("Connecting Play! to Elastic Search in Client Mode");
		}

		// Mark as Started
		started = true;
		Node node = nb.node();
		Client client = node.client();

		// Start Indexes
		try {
			// Start Indexes
			String models = Play.configuration.getProperty("elasticsearch.models", "");
			String[] classes = StringUtils.split(models, ",");
			for ( String clazz : classes ) {
				Logger.info("Start Index for Class: %s", clazz);
				ElasticSearchAdapter.startIndex(client, clazz);
			}

		} catch (Throwable t) {
			Logger.warn(ExceptionUtil.getStackTrace(t));
			throw new RuntimeException(t);
		}
		
		// Bind Admin
		Router.addRoute("GET", "/es-admin/", "ElasticSearchAdmin.index");
		Router.addRoute("GET", "/es-admin/lib", "staticDir:public");

		// Bind Client to Thread Local
		_session.set(client);

	}
	
	private boolean isElasticSearchable(Object o) {
		for ( Annotation a : o.getClass().getAnnotations() ) {
			if ( ElasticSearchable.class.equals(a.getClass()) ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onEvent(String message, Object context) {
		// Just accept JPA events
		if (!StringUtils.startsWith(message, "JPASupport.")) {
			return;
		}
		
		// Check if object has annotation
		if ( this.isElasticSearchable(context) == false ) {
			Logger.debug("Not marked to be elastic searchable!");
			return;
		}

		// Do Work
		try {
			// Get Plugin
			ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
			
			// Log Debug
			Logger.info("Elastic Search - " + message + " Event for: " + context);
			
			// Check Event Type
			if (message.equals("JPASupport.objectPersisted") || message.equals("JPASupport.objectUpdated")) {
				// Index Model
				ElasticSearchAdapter.indexModel(plugin.client(), (Model)context);
				
			} else if (message.equals("JPASupport.objectDeleted")) {
				// Delete Model fromIndex
				ElasticSearchAdapter.deleteModel(plugin.client(), (Model)context);
			}
			
			// Log Debug
			Logger.debug("Elastic Event Done!");
		
		} catch (Exception e) {
			Logger.error(e, "Problem updating entity %s on event %s with error %s", context, message, e.getMessage());
		}
	}

}