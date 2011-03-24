package play.modules.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import play.Logger;
import play.db.Model;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchAdapter.
 * 
 */
public abstract class ElasticSearchAdapter {

	/** The IGNOR e_ fields. */
	private static List<String> IGNORE_FIELDS = new ArrayList<String>();
	static {
		IGNORE_FIELDS.add("avoidCascadeSaveLoops");
		IGNORE_FIELDS.add("willBeSaved");
		IGNORE_FIELDS.add("serialVersionId");
		IGNORE_FIELDS.add("serialVersionUID");
	}

	/**
	 * Start index.
	 * 
	 * @param client
	 *            the client
	 * @param clazz
	 *            the clazz
	 */
	public static void startIndex(Client client, Class<?> clazz) {
		index(client, getIndexName(clazz));
	}

	/**
	 * Start index.
	 * 
	 * @param client
	 *            the client
	 * @param clazz
	 *            the clazz
	 */
	public static void startIndex(Client client, String clazz) {
		index(client, getIndexName(clazz));
	}

	/**
	 * Index.
	 * 
	 * @param client
	 *            the client
	 * @param indexName
	 *            the index name
	 */
	private static void index(Client client, String indexName) {
		try {
			Logger.debug("Starting Elastic Search Index %s", indexName);
			CreateIndexResponse response = client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
			Logger.debug("Response: %s", response);

		} catch (IndexAlreadyExistsException iaee) {
			Logger.debug("Index already exists: %s", indexName);

		} catch (Throwable t) {
			Logger.warn(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * Index model.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param client
	 *            the client
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void indexModel(Client client, T model) throws Exception {
		// Log Debug
		Logger.debug("Start Index Model: %s", model);

		// Define Content Builder
		XContentBuilder contentBuilder = null;

		// Index Model
		try {
			// Define Index Name
			String indexName = getIndexName(model);
			Logger.debug("Index Name: %s", indexName);

			// Get list fields that should not be ignored (@ElasticSearchIgnore)
			List<String> fields = ReflectionUtil.getAllFieldNamesWithoutAnnotation(model.getClass(), ElasticSearchIgnore.class);
			contentBuilder = XContentFactory.jsonBuilder().startObject();

			// Loop into each field
			for (String name : fields) {
				name = name.replaceFirst(model.getClass().getCanonicalName() + ".", "");
				if (StringUtils.isNotBlank(name) && IGNORE_FIELDS.contains(name) == false) {
					Object value = ReflectionUtil.getFieldValue(model, name);
					if (value != null) {
						Logger.debug("Field: " + name + ", Value: " + value);
						contentBuilder = contentBuilder.field(name, value);
					} else {
						Logger.debug("No Value for Field: " + name);
					}
				}
			}
			contentBuilder = contentBuilder.endObject().prettyPrint();
			IndexResponse response = client.prepareIndex(indexName, indexName, model._key().toString()).setSource(contentBuilder).execute().actionGet();

			// Log Debug
			Logger.info("Index Response: %s", response);

		} finally {
			if (contentBuilder != null) {
				contentBuilder.close();
			}
		}
	}

	/**
	 * Delete model.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param client
	 *            the client
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void deleteModel(Client client, T model) throws Exception {
		Logger.debug("Delete Model: %s", model);
		DeleteResponse response = client.prepareDelete(getIndexName(model), getIndexName(model), String.valueOf(model._key())).setOperationThreaded(false).execute().actionGet();
		Logger.debug("Delete Response: %s", response);

	}

	/**
	 * Gets the index name.
	 * 
	 * @param model
	 *            the model
	 * @return the index name
	 */
	private static String getIndexName(Model model) {
		return getIndexName(model.getClass());
	}

	/**
	 * Gets the index name.
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the index name
	 */
	private static String getIndexName(Class<?> clazz) {
		return getIndexName(clazz.getName());
	}

	/**
	 * Gets the index name.
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the index name
	 */
	private static String getIndexName(String clazz) {
		Logger.debug("Class: %s", clazz);
		String value = clazz.toLowerCase().trim().replace('.', '_');
		Logger.debug("Index Name: %s", value);
		return value;
	}

}
