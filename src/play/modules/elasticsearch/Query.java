package play.modules.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.search.SearchResults;
import play.modules.elasticsearch.transformer.JPATransformer;
import play.modules.elasticsearch.transformer.Transformer;

/**
 * An elastic search query
 *
 * @param <T> the generic model to search for
 */
public class Query<T extends Model> {

	private final Class<T> clazz;
	private final QueryBuilder builder;
	private final List<AbstractFacetBuilder> facets;

	private int from = -1;
	private int size = -1;

	private boolean hydrate = false;

	Query(Class<T> clazz, QueryBuilder builder) {
		Validate.notNull(clazz, "clazz cannot be null");
		Validate.notNull(builder, "builder cannot be null");
		this.clazz = clazz;
		this.builder = builder;
		this.facets = new ArrayList<AbstractFacetBuilder>();
	}

	/**
	 * Sets from
	 * 
	 * @param from record index to start from
	 * @return self
	 */
	public Query from(int from) {
		this.from = from;

		return this;
	}

	/**
	 * Sets fetch size
	 * 
	 * @param size the fetch size
	 * @return self
	 */
	public Query size(int size) {
		this.size = size;

		return this;
	}

	/**
	 * Controls entity hydration
	 * 
	 * @param hydrate hydrate entities
	 * @return self
	 */
	public Query hydrate(boolean hydrate) {
		this.hydrate = hydrate;

		return this;
	}

	/**
	 * Adds a facet
	 * 
	 * @param facet the facet
	 * @return self
	 */
	public Query addFacet(AbstractFacetBuilder facet) {
		Validate.notNull(facet, "facet cannot be null");
		facets.add(facet);

		return this;
	}

	/**
	 * Runs the query
	 * 
	 * @return the search results
	 */
	public SearchResults<T> fetch() {
		// Build request
		SearchRequestBuilder request = ElasticSearch.builder(builder, clazz);
		
		// Facets
		for (AbstractFacetBuilder facet : facets) {
			request.addFacet(facet);
		}
		
		// Paging
		if (from > -1) {
			request.setFrom(from);
		}
		if (size > -1) {
			request.setSize(size);
		}

		// Only load id field for hydrate
		if (hydrate) {
			request.addField("_id");
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("ES Query: %s", builder.toString());
		}

		SearchResponse searchResponse = request.execute().actionGet();
		SearchResults searchResults = null;
		if (hydrate) {
			searchResults = JPATransformer.toSearchResults(searchResponse, clazz);
		} else {
			searchResults = Transformer.toSearchResults(searchResponse, clazz);
		}
		return searchResults;
	}
}
