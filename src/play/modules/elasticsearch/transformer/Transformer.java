package play.modules.elasticsearch.transformer;

import org.elasticsearch.action.search.SearchResponse;

import play.db.Model;
import play.modules.elasticsearch.search.SearchResults;

/**
 * Transformer for search results
 * 
 * @author Bas
 * 
 * @param <T>
 */
public interface Transformer<T extends Model> {

	/**
	 * Transforms a {@link SearchResponse} into {@link SearchResults}
	 * 
	 * @param searchResponse
	 * @param clazz
	 * @return
	 */
	public SearchResults<T> toSearchResults(SearchResponse searchResponse, Class<T> clazz);
}
