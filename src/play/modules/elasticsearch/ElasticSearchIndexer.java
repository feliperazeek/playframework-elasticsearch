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

import play.Logger;
import play.db.jpa.NoTransaction;
import play.jobs.Job;
import play.libs.F.Promise;
import play.modules.elasticsearch.util.ExceptionUtil;

/**
 * Indexer job.
 * 
 * <p>The indexing process runs in a separate thread
 * <p>The {@link NoTransaction} annotation prevents the JPAPlugin from creating a hibernate session, 
 * which would remain open for as long as this job is active
 */
@NoTransaction
public class ElasticSearchIndexer extends Job<Void> {

	/** Index Stream */
	public static play.libs.F.EventStream<ElasticSearchIndexEvent> stream = new play.libs.F.EventStream<ElasticSearchIndexEvent>();

	/**
	 * 
	 * @see play.jobs.Job#doJob()
	 */
	@Override
	public void doJob() {
		// In case Play starts this job, we don't need to start it again
		LocalIndexEventHandler.markIndexerStarted();

		while (true) {
			try {
				Promise<ElasticSearchIndexEvent> promise = ElasticSearchIndexer.stream.nextEvent();
				ElasticSearchIndexEvent indexEvent = promise.get();
				ElasticSearchIndexAction indexAction = new ElasticSearchIndexAction();
				indexAction.invoke(indexEvent);

			} catch (Throwable t) {
				Logger.error(ExceptionUtil.getStackTrace(t));
			}
		}
	}

}
