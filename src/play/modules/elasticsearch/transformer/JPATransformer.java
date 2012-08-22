package play.modules.elasticsearch.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import play.Logger;
import play.Play;
import play.data.binding.Binder;
import play.db.Model;
import play.db.jpa.JPQL;
import play.exceptions.UnexpectedException;
import play.modules.elasticsearch.ElasticSearchPlugin;
import play.modules.elasticsearch.search.SearchResults;

/**
 * Transforms ES SearchResponse to a list of hydrated entities
 * 
 * @author Bas
 * 
 */
public class JPATransformer<T extends Model> implements Transformer<T> {

	/**
	 * To search results.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param searchResponse
	 *            the search response
	 * @param clazz
	 *            the clazz
	 * @return the search results
	 */
	@Override
	public SearchResults<T> toSearchResults(SearchResponse searchResponse, final Class<T> clazz) {
		// Get Total Records Found
		long count = searchResponse.hits().totalHits();

		// Get key information
		Class<T> hitClazz = clazz;
		Model.Factory factory;
		Class<?> keyType = null;
		if (!clazz.equals(Model.class)) {
			factory = Model.Manager.factoryFor(hitClazz);
			keyType = factory.keyType();
		}

		// Store object ids categorized by model
		Map<Class<T>, List<Object>> allIds = new HashMap<Class<T>, List<Object>>();
		// Store original order
		Map<Class<T>, Map<Object, Integer>> order = new HashMap<Class<T>, Map<Object, Integer>>();
		// Store scores and sortValues
		List<Float> scores = new ArrayList<Float>();
		List<Object[]> sortValues = new ArrayList<Object[]>();
		Integer counter = 0;
		// Loop on each one
		for (SearchHit h : searchResponse.hits()) {
			try {
				// get key information if we work on general model
				if (clazz.equals(Model.class)) {
					hitClazz = (Class<T>) ElasticSearchPlugin.lookupModel(h.getType());
					factory = Model.Manager.factoryFor(hitClazz);
					keyType = factory.keyType();
				}

				Object id = Binder.directBind(h.getId(), keyType);

				// add id to the list
				List<Object> modelIds = allIds.get(hitClazz);
				if (modelIds == null) {
					modelIds = new ArrayList<Object>();
					allIds.put(hitClazz, modelIds);
				}
				modelIds.add(id);

				// mark order
				Map<Object, Integer> modelOrder = order.get(hitClazz);
				if (modelOrder == null) {
					modelOrder = new HashMap<Object, Integer>();
					order.put(hitClazz, modelOrder);
				}
				modelOrder.put(id, counter++);

				scores.add(h.score());
				sortValues.add(h.sortValues());

			} catch (Exception e) {
				throw new UnexpectedException(
						"Could not convert the ID from index to corresponding type", e);
			}
		}

		Logger.debug("Model IDs returned by ES: %s", allIds);

		List<T> objects = new ArrayList<T>();

		// iterate over all models
		for (Entry<Class<T>, List<Object>> entry : allIds.entrySet()) {
			// get all ids for the model
			List<T> modelObjects = loadFromDb(entry.getKey(), entry.getValue());
			objects.addAll(modelObjects);
		}

		sortByOrder(objects, order);

		// Make sure all items exist in the database
		if (objects.size() != counter) {
			if (shouldFailOnMissingObjects()) {
				throw new IllegalStateException(
						"Please re-index, not all indexed items are available in the database");
			} else {
				Logger.debug("Some Models not found in DB, continuing...");
			}
		}

		Logger.debug("Models after sorting: %s", objects);

		// Return Results
		return new SearchResults<T>(count, objects, scores, sortValues, searchResponse.facets());
	}

	private boolean shouldFailOnMissingObjects() {
		return Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.failOnMissingObjects", "true"));
	}

	/**
	 * Load entities from database
	 * 
	 * @param <T>
	 * @param clazz
	 * @param ids
	 * @return
	 */
	private static <T extends Model> List<T> loadFromDb(Class<T> clazz, List<Object> ids) {
		// JPA maps the "id" field to the key automatically
		List<T> objects = JPQL.instance.find(clazz.getName(), "id in (?1)", new Object[] { ids })
				.fetch();

		return objects;
	}

	/**
	 * Sort list of objects according to the order of their keys as defined by ids
	 * 
	 * @param <T>
	 * @param objects
	 * @param ids
	 */
	private static <T extends Model> void sortByOrder(List<T> objects, final Map<Class<T>, Map<Object, Integer>> order) {
		Collections.sort(objects, new Comparator<T>() {

			@Override
			public int compare(T arg0, T arg1) {
				Integer idx0 = order.get(arg0.getClass()).get(arg0._key());
				Integer idx1 = order.get(arg1.getClass()).get(arg1._key());

				return idx0.compareTo(idx1);
			}
		});
	}
}
