package play.modules.elasticsearch;

import play.Logger;
import play.db.Model;
import java.util.Date;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.elasticsearch.index.query.xcontent.FilterBuilders.*;
import static org.elasticsearch.index.query.xcontent.QueryBuilders.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;


public class ElasticSearchAdapter {
	
	public static void indexModel(Client client, String indexName, Model model) throws Exception {
		Logger.info("Index Model: %s", model);
		
		client.prepareIndex(indexName, model.getClass().getCanonicalName(), String.valueOf(model._key()))
        .setSource(jsonBuilder()
                    .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Elastic Search")
                    .endObject()
                  )
        .execute()
        .actionGet();
		
	}
	
	public static void deleteModel(Client client, String indexName, Model model) throws Exception {
		Logger.info("Delete Model: %s", model);
		client.prepareDelete(indexName, model.getClass().getCanonicalName(), String.valueOf(model._key()))
        .setOperationThreaded(false)
        .execute()
        .actionGet();
		
	}

}
