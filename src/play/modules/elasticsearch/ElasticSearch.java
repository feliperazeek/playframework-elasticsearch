package play.modules.elasticsearch;

import org.elasticsearch.client.Client;

import play.Play;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearch.
 */
public abstract class ElasticSearch {

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public static Client client() {
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		return plugin.client();
	}

}
