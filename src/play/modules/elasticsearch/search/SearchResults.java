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
package play.modules.elasticsearch.search;

import java.util.List;

import org.elasticsearch.search.facet.Facets;

import play.db.Model;

/**
 * The Class SearchResults.
 *
 * @param <T> the generic type
 */
public class SearchResults<T extends Model> {

	/** The total count. */
	public long totalCount;

	/** The objects. */
	public List<T> objects;

    /** The result scores (same order as the objects). */
    public List<Float> scores;

    /** The sort values (same order as the objects). */
    public List<Object[]> sortValues;

    /** The facets. */
	public Facets facets;


	/**
	 * Instantiates a new search results.
	 *
	 * @param totalCount the total count
	 * @param objects the objects
	 * @param facets the facets
	 */
	public SearchResults(long totalCount, List<T> objects, Facets facets) {
        this(totalCount, objects, null, null, facets);
	}

    public SearchResults(long totalCount, List<T> objects, List<Float> scores, List<Object[]> sortValues, Facets facets) {
        this.totalCount = totalCount;
        this.objects = objects;
        this.scores = scores;
        this.sortValues = sortValues;
        this.facets = facets;
    }

}
