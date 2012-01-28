package play.modules.elasticsearch.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import play.Logger;
import play.data.binding.Binder;
import play.db.Model;
import play.db.jpa.JPQL;
import play.exceptions.UnexpectedException;
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
	public SearchResults<T> toSearchResults(SearchResponse searchResponse, Class<T> clazz) {
		// Get Total Records Found
		long count = searchResponse.hits().totalHits();

		// Get key information
		Model.Factory factory = Model.Manager.factoryFor(clazz);
		Class<?> keyType = factory.keyType();

		// Loop on each one
		List<Object> ids = new ArrayList<Object>();
		for (SearchHit h : searchResponse.hits()) {
			try {
				ids.add(Binder.directBind(h.getId(), keyType));
			} catch (Exception e) {
				throw new UnexpectedException(
						"Could not convert the ID from index to corresponding type", e);
			}
		}

		Logger.debug("Model IDs returned by ES: %s", ids);

		List<T> objects = null;

		if (ids.size() > 0) {
			// Fetch JPA entities from database while preserving ES result order
			objects = loadFromDb(clazz, ids);
			sortByIds(objects, ids);

			// Make sure all items exist in the database
			if (objects.size() != ids.size()) {
				throw new IllegalStateException(
						"Please re-index, not all indexed items are available in the database");
			}
		} else {
			objects = Collections.emptyList();
		}

		Logger.debug("Models after sorting: %s", objects);

		// Return Results
		return new SearchResults<T>(count, objects, searchResponse.facets());
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
	 * Sort list of objects according to the order of their keys as defined by
	 * ids
	 * 
	 * @param <T>
	 * @param objects
	 * @param ids
	 */
	private static <T extends Model> void sortByIds(List<T> objects, final List<Object> ids) {
		Collections.sort(objects, new Comparator<T>() {

			@Override
			public int compare(T arg0, T arg1) {
				Integer idx1 = ids.indexOf(arg0._key());
				Integer idx2 = ids.indexOf(arg1._key());

				return idx1.compareTo(idx2);
			}
		});
	}
}
