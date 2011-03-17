package play.modules.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
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
		index( client, getIndexName( clazz ) );
	}
	
	/**
	 * Start index.
	 *
	 * @param client the client
	 * @param clazz the clazz
	 */
	public static void startIndex(Client client, String clazz) {
		index( client, getIndexName( clazz ) );
	}
	
	/**
	 * Index.
	 *
	 * @param client the client
	 * @param indexName the index name
	 */
	private static void index(Client client, String indexName) {
		try {
			Logger.info("Starting Elastic Search Index %s", indexName);
			client.admin().indices().create(new CreateIndexRequest(indexName))
					.actionGet();
		} catch (IndexAlreadyExistsException iaee) {
			Logger.info("Index already exists: %s", indexName);
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
	public static <T extends Model> void indexModel(Client client, T model)
			throws Exception {
		// Log Debug
		Logger.info("Start Index Model: %s", model);
		
		// Define Index Name
		String indexName = getIndexName( model );
		Logger.info("Index Name: %s", indexName);

		// Start Mapping Object
		IndexRequestBuilder irb = client.prepareIndex(indexName, indexName, String.valueOf(model._key()));
		XContentBuilder b = XContentFactory.jsonBuilder().startObject();

		// Get list fields that should not be ignored (@ElasticSearchIgnore)
		List<String> fields = ReflectionUtil.getAllFieldNamesWithoutAnnotation(
				model.getClass(), ElasticSearchIgnore.class);

		// Loop into each field
		for (String name : fields) {
			if (StringUtils.isNotBlank(name)
					&& IGNORE_FIELDS.contains(name) == false) {
				if (name.equalsIgnoreCase("id")) {
					name = name.replaceFirst(model.getClass()
							.getCanonicalName() + ".", "");
					Object value = ReflectionUtil.getFieldValue(model, name);
					if (value != null) {
						Logger.info("Field: " + name + ", Value: " + value);
						b.field(name, value);
					} else {
						Logger.info("No Value for Field: " + name);
					}
				} else {
					b.field(name, model._key());
				}
			}
		}
		
		// Done Mapping
		b.endObject();
		
		// Set Builder
		irb.setSource(b);

		// Send Job
		IndexResponse response = irb.execute().actionGet();;

		// Log Debug
		Logger.info("Index Response: %s", response);
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
	public static <T extends Model> void deleteModel(Client client, T model)
			throws Exception {
		Logger.info("Delete Model: %s", model);

		DeleteResponse response = client
				.prepareDelete(getIndexName(model), getIndexName(model),
						String.valueOf(model._key()))
				.setOperationThreaded(false).execute().actionGet();

		Logger.info("Delete Response: %s", response);

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
	 * @param clazz the clazz
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
