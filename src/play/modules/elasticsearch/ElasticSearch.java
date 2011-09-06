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

import play.modules.elasticsearch.adapter.ElasticSearchAdapter;
import play.modules.elasticsearch.search.SearchResults;
import play.modules.elasticsearch.transformer.JPATransformer;
import play.modules.elasticsearch.transformer.Transformer;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Play;
import play.db.Model;

// TODO: Auto-generated Javadoc
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
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		return plugin.client();
	}
	
	/**
	 * Build a SearchRequestBuilder
	 * 
	 * @param <T>
	 *            the generic type
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the search request builder
	 */
	private static <T extends Model> SearchRequestBuilder builder(QueryBuilder queryBuilder, Class<T> clazz) {
		String index = ElasticSearchAdapter.getIndexName(clazz);
		SearchRequestBuilder builder = client().prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH).setQuery(queryBuilder);
		return builder;
	}
	
	/**
	 * Build a Query
	 * 
	 * @param <T>
	 *            the generic type
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the query
	 */
	public static <T extends Model> Query query(QueryBuilder queryBuilder, Class<T> clazz) {
		return new Query(clazz, builder(queryBuilder, clazz));
	}

	/**
	 * Search with optional facets.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	public static <T extends Model> SearchResults search(QueryBuilder queryBuilder, Class<T> clazz, AbstractFacetBuilder... facets) {
		return search(queryBuilder, clazz, false, facets);
	}
	
	/**
	 * Search with optional facets. Hydrates entities
	 * 
	 * @param <T>
	 *            the generic type
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	public static <T extends Model> SearchResults searchAndHydrate(QueryBuilder queryBuilder, Class<T> clazz, AbstractFacetBuilder... facets) {
		return search(queryBuilder, clazz, true, facets);
	}
	
	/**
	 * Faceted search, hydrates entities if asked to do so.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * @param hydrate
	 * 			  hydrate JPA entities
	 * @param facets
	 *            the facets
	 * 
	 * @return the search results
	 */
	private static <T extends Model> SearchResults search(QueryBuilder queryBuilder, Class<T> clazz, boolean hydrate, AbstractFacetBuilder... facets) {
		SearchRequestBuilder builder = builder(queryBuilder, clazz);
		for( AbstractFacetBuilder facet : facets ) {
			builder.addFacet(facet);
		}
		
		// Only load id field for hydrate
		if( hydrate ) {
			builder.addField("_id");
		}
		
		if( Logger.isDebugEnabled() ) {
			Logger.debug("ES Query: %s", builder.toString());
		}
		
		SearchResponse searchResponse = builder.execute().actionGet();
		SearchResults searchResults = null;
		if( hydrate ) {
			searchResults = JPATransformer.toSearchResults(searchResponse, clazz);
		} else {
			searchResults = Transformer.toSearchResults(searchResponse, clazz);
		}
		return searchResults;
	}

}
