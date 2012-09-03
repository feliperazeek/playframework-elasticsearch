package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.modules.elasticsearch.annotations.ElasticSearchTtl;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.ModelMapper;

public class TtlTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	@ElasticSearchTtl("10s")
	public static class ObjectToMap {

		public String name;

	}

	@Test
	public void testFieldProperties() throws IOException {
		ModelMapper<ObjectToMap> mapper = getMapper(ObjectToMap.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("_ttl");
		mapping.field("enabled", true);
		mapping.field("default", "10s");
		mapping.endObject();
		mapping.startObject("properties");

		// Order matters, see AbstractFieldMapper
		mapping.startObject("name");
		mapping.field("type", "string");
		mapping.endObject();

		mapping.endObject();

		mapping.endObject();

		assertEquals(mapping.string(), generatedMapping.string());
	}

}
