/** 
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Felipe Oliveira (http://mashup.fm)
 * 
 */
package play.modules.elasticsearch;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.FacetBuilder;

import play.Play;
import play.db.Model;
import play.libs.F.Promise;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.modules.elasticsearch.search.SearchResults;

/**
 * The Class ElasticSearch.
 */
public abstract class ElasticSearch {

	/**
	 * Client.
	 * 
	 * @return the client
	 */
	public static Client client() {
		return ElasticSearchPlugin.client();
	}

	/**
	 * Build a SearchRequestBuilder
	 * 
	 * @param <T>
	 *            the generic type
	 * @param query
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the search request builder
	 */
	static <T extends Model> SearchRequestBuilder builder(final QueryBuilder query, final Class<T> clazz) {
		final ModelMapper<T> mapper = ElasticSearchPlugin.getMapper(clazz);
		final String index = mapper.getIndexName();
		final SearchRequestBuilder builder = client().prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH).setQuery(query);
		return builder;
	}

	/**
	 * Build a Query
	 * 
	 * @param <T>
	 *            the generic type
	 * @param query
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the query
	 */
	public static <T extends Model> Query<T> query(final QueryBuilder query, final Class<T> clazz) {
		return new Query<T>(clazz, query);
	}

	/**
	 * Search with optional facets.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param query
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	public static <T extends Model> SearchResults<T> search(final QueryBuilder query, final Class<T> clazz, final FacetBuilder... facets) {
		return search(query, clazz, false, facets);
	}

	/**
	 * Search with optional facets. Hydrates entities
	 * 
	 * @param <T>
	 *            the generic type
	 * @param query
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	public static <T extends Model> SearchResults<T> searchAndHydrate(final QueryBuilder query, final Class<T> clazz, final FacetBuilder... facets) {
		return search(query, clazz, true, facets);
	}

	/**
	 * Faceted search, hydrates entities if asked to do so.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param query
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param hydrate
	 *            hydrate JPA entities
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	private static <T extends Model> SearchResults<T> search(final QueryBuilder query, final Class<T> clazz, final boolean hydrate, final FacetBuilder... facets) {
		// Build a query for this search request
		final Query<T> search = query(query, clazz);

		// Control hydration
		search.hydrate(hydrate);

		// Add facets
		for (final FacetBuilder facet : facets) {
			search.addFacet(facet);
		}

		return search.fetch();
	}

	/**
	 * Indexes the given model
	 * 
	 * @param <T>
	 *            the model type
	 * @param model
	 *            the model
	 */
	public static <T extends Model> void index(final T model) {
		final ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		plugin.index(model);
	}

	/**
	 * Indexes the given model using delivery mode
	 * 
	 * @param <T>
	 *            the model type
	 * @param model
	 *            the model
	 */
	public static <T extends Model> void index(final T model, final ElasticSearchDeliveryMode deliveryMode) {
		final ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		plugin.index(model, deliveryMode);
	}

	/**
	 * Reindexes the given model using provided delivery mode
	 * 
	 * @param deliveryMode
	 *            Delivery mode to use for reindexing tasks. Set null to use the default, synchronous mode.
	 * @param model
	 *            the model
	 */
	public static Promise<Void> reindex(final ElasticSearchDeliveryMode deliveryMode) {
		return new ReindexDatabaseJob(deliveryMode).now();
	}

}
