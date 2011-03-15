package play.modules.elasticsearch.query;

import static org.elasticsearch.index.query.xcontent.FilterBuilders.*;
import static org.elasticsearch.index.query.xcontent.QueryBuilders.*;

import org.elasticsearch.client.Client;

import play.Play;
import play.modules.elasticsearch.ElasticSearchPlugin;


public class ElasticSearchSearchQueryBuilder {
	
	
	/**
	public ActionFuture<SearchResponse> search(SearchRequest searchRequest) {
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		Client client = plugin.client();
		return client.search(searchRequest);
	} */
	

}
