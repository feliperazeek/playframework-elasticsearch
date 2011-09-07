package play.modules.elasticsearch;


public interface IndexEventHandler {
	public void handle(ElasticSearchIndexEvent event);
}
