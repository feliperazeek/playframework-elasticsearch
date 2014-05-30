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
package play.modules.elasticsearch.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.ElasticSearchPlugin;
import play.modules.elasticsearch.search.SearchResults;
import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * The original transformer, as written by Felipe.
 * 
 * <p>
 * This transformer only supports basic mappings, without nested collections or
 * models.
 * 
 * @param <T>
 *            the generic type
 */
public class SimpleTransformer<T extends Model> implements Transformer<T> {

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
		long count = searchResponse.getHits().totalHits();

		// Init List
		List<T> objects = new ArrayList<T>();
        List<Float> scores = new ArrayList<Float>();
        List<Object[]> sortValues = new ArrayList<Object[]>();

        // Loop on each one
        Class<T> hitClazz = clazz;
		for (SearchHit h : searchResponse.getHits()) {
			// Init Model Class
			Logger.debug("Starting Record!");
			if (clazz.equals(Model.class)) {
				hitClazz = (Class<T>) ElasticSearchPlugin.lookupModel(h.getType());
			}
			T o = ReflectionUtil.newInstance(hitClazz);
			 
			

			// Get Data Map
			Map<String, Object> map = h.sourceAsMap();
			Logger.debug("Record Map: %s", map);

			// Bind Data
			for (Map.Entry<String, Object> e : map.entrySet()) {
				ReflectionUtil.setFieldValue(o, e.getKey(), e.getValue());
			}

			// Log Debug
			Logger.debug("Model Instance: %s", o);
			objects.add(o);
            scores.add(h.score());
            sortValues.add(h.sortValues());
        }

		// Return Results
		return new SearchResults<T>(count, objects, scores, sortValues, searchResponse.getFacets());
	}

}
