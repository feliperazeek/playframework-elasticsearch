package analysis;

import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.settings.Settings;
import org.junit.Before;
import org.junit.Test;

import play.modules.elasticsearch.ElasticSearchPlugin;
import play.test.Fixtures;
import play.test.UnitTest;

public class ElasticSearchAnalysisTest extends UnitTest {

	private AdminClient adminClient;
	private Settings indexSettings;
	
	@Before
	public void setUp(){
		
		Fixtures.deleteDatabase();
	    Fixtures.loadModels("analysis-data.yml");
		
		adminClient = ElasticSearchPlugin.client().admin();
		
		// Check custom settings on index
        ClusterStateResponse response = adminClient.cluster().prepareState()
                .execute().actionGet();

        indexSettings = response.getState().metaData().index("models_product").settings();
	}
	
	@Test
	public void testIndexSettingsIsNotNull(){
	
        assertNotNull(indexSettings);
	}
	
	@Test
	public void testElasticSearchAnalyzer(){
        
        assertEquals("standard", indexSettings.get("index.analysis.analyzer.default.tokenizer"));
        assertEquals("lowercase", indexSettings.get("index.analysis.analyzer.default.filter.0"));
        assertEquals("asciifolding", indexSettings.get("index.analysis.analyzer.default.filter.1"));
	}
	
	
	@Test
	public void testElasticFilterSettings(){
        
		assertEquals("length", indexSettings.get("index.analysis.filter.myLength.type"));
        assertEquals("0", indexSettings.get("index.analysis.filter.myLength.min"));
        assertEquals("5", indexSettings.get("index.analysis.filter.myLength.max"));

        assertEquals("edgeNGram", indexSettings.get("index.analysis.filter.myEdgeNGram.type"));
        assertEquals("2", indexSettings.get("index.analysis.filter.myEdgeNGram.min_gram"));
        assertEquals("10", indexSettings.get("index.analysis.filter.myEdgeNGram.max_gram"));
        assertEquals("front", indexSettings.get("index.analysis.filter.myEdgeNGram.side"));
	}

	@Test
	public void testElasticSearchAnalyzerCustomFilters(){
		
		assertEquals("myLength", indexSettings.get("index.analysis.analyzer.default.filter.2"));
		assertEquals("myEdgeNGram", indexSettings.get("index.analysis.analyzer.default.filter.3"));
	}
}