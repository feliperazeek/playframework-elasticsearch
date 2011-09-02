package play.modules.elasticsearch;

import org.apache.commons.lang.Validate;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.search.SearchResults;
import play.modules.elasticsearch.transformer.JPATransformer;
import play.modules.elasticsearch.transformer.Transformer;

public class Query<T extends Model> {

	private final Class<T> clazz;
	private final SearchRequestBuilder builder;

	private int from = -1;
	private int size = -1;

	private boolean hydrate = false;

	Query(Class<T> clazz, SearchRequestBuilder builder) {
		Validate.notNull(clazz, "clazz cannot be null");
		Validate.notNull(builder, "builder cannot be null");
		this.clazz = clazz;
		this.builder = builder;
	}

	public Query from(int from) {
		this.from = from;

		return this;
	}

	public Query size(int size) {
		this.size = size;

		return this;
	}

	public Query hydrate(boolean hydrate) {
		this.hydrate = hydrate;

		return this;
	}

	public Query addFacet(AbstractFacetBuilder facet) {
		Validate.notNull(facet, "facet cannot be null");
		builder.addFacet(facet);

		return this;
	}

	public SearchResults<T> fetch() {
		// Paging
		if (from > -1) {
			builder.setFrom(from);
		}
		if (size > -1) {
			builder.setSize(size);
		}

		// Only load id field for hydrate
		if (hydrate) {
			builder.addField("_id");
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("ES Query: %s", builder.toString());
		}

		SearchResponse searchResponse = builder.execute().actionGet();
		SearchResults searchResults = null;
		if (hydrate) {
			searchResults = JPATransformer.toSearchResults(searchResponse, clazz);
		} else {
			searchResults = Transformer.toSearchResults(searchResponse, clazz);
		}
		return searchResults;
	}
}
