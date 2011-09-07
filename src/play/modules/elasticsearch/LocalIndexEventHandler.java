package play.modules.elasticsearch;

/**
 * Handler which processes events locally
 */
public class LocalIndexEventHandler implements IndexEventHandler {

	/** Flag that indicates if the indexer has been started */
	private static boolean indexerStarted = false;

	@Override
	public void handle(ElasticSearchIndexEvent event) {
		if (indexerStarted == false) {
			new ElasticSearchIndexer().now();
			indexerStarted = true;
		}
		ElasticSearchIndexer.stream.publish(event);
	}

	static void markIndexerStarted() {
		indexerStarted = true;
	}

}
