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
import play.modules.elasticsearch.util.ExceptionUtil;
import play.Logger;

/**
 * The Class ElasticSearchIndexAction.
 */
public class ElasticSearchIndexAction implements play.libs.F.Action<ElasticSearchIndexEvent> {

	/**
	 * Invoke Action
	 * 
	 * @see play.libs.F.Action#invoke(java.lang.Object)
	 */
	@Override
	public void invoke(ElasticSearchIndexEvent message) {
		// Log Debug
		Logger.info("Elastic Search - %s Event", message);

		// Index Event
		try {
			switch(message.getType()) {
				case INDEX:
					ElasticSearchAdapter.indexModel(ElasticSearchPlugin.client(), message.getObject());
					break;
				case DELETE:
					ElasticSearchAdapter.deleteModel(ElasticSearchPlugin.client(), message.getObject());
					break;
			}
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
		}
	}

}
