package play.modules.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.Model;
import play.modules.elasticsearch.ElasticSearchPlugin;

public class ElasticSearchPlugin extends PlayPlugin {

	private ThreadLocal<Client> _session = new ThreadLocal<Client>();

	public Client client() {
		return _session.get();
	}

	@Override
	public void onApplicationStart() {
		NodeBuilder nb = nodeBuilder();

		boolean localMode = false;
		if (!Play.configuration.containsKey("elasticsearch.local")) {
			localMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.local"));
			nb = nb.local(localMode);
		}

		boolean clientMode = false;
		if (!Play.configuration.containsKey("elasticsearch.client")) {
			clientMode = Boolean.getBoolean(Play.configuration
					.getProperty("elasticsearch.client"));
			nb = nb.client(clientMode);
		}

		if (localMode == false && clientMode == false) {
			localMode = true;
			nb = nb.local(localMode);
		}

		if (localMode) {
			Logger.info("Starting Play! Elastic Search in Local Mode");
		} else {
			Logger.info("Starting Play! Elastic Search in Client Mode");
		}

		Node node = nb.node();
		Client client = node.client();

		_session.set(client);

	}

	@Override
	public void onEvent(String message, Object context) {
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		Client client = plugin.client();

		try {

			ElasticSearchEntity clazz = context.getClass().getAnnotation(
					ElasticSearchEntity.class);
			if (clazz == null) {
				// Logger.info("No ElasticSearchEntity Found - Message: %s, Object: %s, Class: class",
				// message, context, context.getClass().getName());
				return;
			} else {
				Logger.info(
						"@ElasticSearchEntity - Message: %s, Object: %s, Class: class",
						message, context, context.getClass().getName());
			}

			if (!message.startsWith("JPASupport"))
				return;

			if (message.equals("JPASupport.objectPersisted")
					|| message.equals("JPASupport.objectUpdated")) {
				ElasticSearchAdapter.indexModel(this.client(), context
						.getClass().getName(), (Model) context);

			} else if (message.equals("JPASupport.objectDeleted")) {
				ElasticSearchAdapter.deleteModel(this.client(), context
						.getClass().getName(), (Model) context);
			}

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}

	}

}