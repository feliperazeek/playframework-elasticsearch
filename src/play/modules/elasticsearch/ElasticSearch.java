package play.modules.elasticsearch;

import play.Play;
import org.elasticsearch.client.Client;

import play.modules.elasticsearch.ElasticSearchPlugin;

public abstract class ElasticSearch {
	
	public static Client client() {
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		return plugin.client();
	}

}
