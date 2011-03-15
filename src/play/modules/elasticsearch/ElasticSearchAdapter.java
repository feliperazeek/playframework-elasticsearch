package play.modules.elasticsearch;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.ElasticSearchPlugin;

import java.util.Date;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
import static org.elasticsearch.index.query.xcontent.FilterBuilders.*;
import static org.elasticsearch.index.query.xcontent.QueryBuilders.*;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import java.io.*;

public abstract class ElasticSearchAdapter {

	public static byte[] convert(Object obj) throws IOException {
		ObjectOutputStream os = null;

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
		os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
		os.flush();
		os.writeObject(obj);
		os.flush();
		byte[] sendBuf = byteStream.toByteArray();
		os.close();
		return sendBuf;
	}

	public static void indexModel(Client client, String indexName, Model model)
			throws Exception {
		Logger.info("Index Model: %s", model);

		// String json = getMapper().writeValueAsString(model);
		// Logger.info("JSON: %s", json);

		// XContent x = XContentFactory.xContent(convert(model));

		IndexRequestBuilder irb = client.prepareIndex(indexName.toLowerCase(),
				indexName.toLowerCase(), String.valueOf(model._key()))
				.setSource(XContentFactory.jsonBuilder());

		// .setSource(XContentFactory.)

		// .setSource(XContentFactory.xContent(convert(model)));
		// irb.execute().actionGet();

	}

	public static void deleteModel(Client client, String indexName, Model model)
			throws Exception {
		Logger.info("Delete Model: %s", model);

		client.prepareDelete(indexName.toLowerCase(), indexName.toLowerCase(),
				String.valueOf(model._key())).setOperationThreaded(false)
				.execute().actionGet();

	}

}
