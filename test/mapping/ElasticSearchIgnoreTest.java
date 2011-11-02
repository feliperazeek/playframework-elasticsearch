package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Simple tests for the ElasticSearchIgnore annotation
 */
public class ElasticSearchIgnoreTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestModel extends Model {
		public String test;

		@ElasticSearchIgnore
		public String test2;
	}

	@Test
	public void testModelWithIgnoredFields() throws IOException {
		ModelMapper<TestModel> mapper = MapperFactory.getMapper(TestModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("test");
		mapping.field("type", "string");
		mapping.endObject();

		// Play model id
		mapping.startObject("id");
		mapping.field("type", "long");
		mapping.endObject();

		mapping.endObject();
		mapping.endObject();
		mapping.endObject();

		assertEquals(mapping.string(), generatedMapping.string());
	}

}
