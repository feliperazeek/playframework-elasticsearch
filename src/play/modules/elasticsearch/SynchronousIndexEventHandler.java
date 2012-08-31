package play.modules.elasticsearch;

/**
 * Handles events in the same thread as they arrive, circumventing indexer. This can be useful for testing or if you want to want to index a lot of entities within a single job that you manage yourself.
 * 
 * @author Filip.Stefanak
 * 
 */
public class SynchronousIndexEventHandler implements IndexEventHandler {

	/**
	 * Just process the event as it is without any fuss
	 */
	@Override
	public void handle(final ElasticSearchIndexEvent event) {
		(new ElasticSearchIndexAction()).invoke(event);
	}

}
