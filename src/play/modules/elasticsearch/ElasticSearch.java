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

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Play;
import play.db.Model;
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
	 * @param queryBuilder
	 *            the query builder
	 * @param clazz
	 *            the clazz
	 * 
	 * @return the search request builder
	 */
	static <T extends Model> SearchRequestBuilder builder(QueryBuilder query, Class<T> clazz) {
		ModelMapper<T> mapper = ElasticSearchPlugin.getMapper(clazz);
		String index = mapper.getIndexName();
		SearchRequestBuilder builder = client().prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH).setQuery(query);
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
	public static <T extends Model> Query<T> query(QueryBuilder query, Class<T> clazz) {
		return new Query<T>(clazz, query);
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
	public static <T extends Model> SearchResults<T> search(QueryBuilder query, Class<T> clazz, AbstractFacetBuilder... facets) {
		return search(query, clazz, false, facets);
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
	public static <T extends Model> SearchResults<T> searchAndHydrate(QueryBuilder queryBuilder, Class<T> clazz, AbstractFacetBuilder... facets) {
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
	private static <T extends Model> SearchResults<T> search(QueryBuilder query, Class<T> clazz, boolean hydrate, AbstractFacetBuilder... facets) {
		// Build a query for this search request
		Query<T> search = query(query, clazz);
		
		// Control hydration
		search.hydrate(hydrate);
		
		// Add facets
		for( AbstractFacetBuilder facet : facets ) {
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
	public static <T extends Model> void index(T model) {
		ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
		plugin.index(model);
	}

}
