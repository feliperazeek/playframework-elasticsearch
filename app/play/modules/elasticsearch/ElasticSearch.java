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

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.xcontent.XContentQueryBuilder;

import play.Play;
import play.db.Model;
import play.modules.elasticsearch.adapter.ElasticSearchAdapter;
import play.modules.elasticsearch.search.SearchResults;
import play.modules.elasticsearch.transformer.Transformer;

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
	 * Search.
	 *
	 * @param <T> the generic type
	 * @param queryBuilder the query builder
	 * @param clazz the clazz
	 * @return the search results
	 */
	public static <T extends Model> SearchResults search(XContentQueryBuilder queryBuilder, Class<T> clazz) {
		String index = ElasticSearchAdapter.getIndexName(clazz);
		SearchResponse searchResponse = client().prepareSearch(index).setSearchType(SearchType.QUERY_THEN_FETCH).setQuery(queryBuilder).execute().actionGet();
		SearchResults searchResults = Transformer.toSearchResults(searchResponse, clazz);
		return searchResults;
	}

}
