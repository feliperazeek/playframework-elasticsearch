package mapping;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Test for collection properties
 */
public class CollectionMappingTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestModel extends Model {
		public List<String> strings;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestEmbeddedModel extends Model {
		@ElasticSearchEmbedded
		public Set<Embedded> objects;
	}

	public static class Embedded {
		public String include;

		@ElasticSearchIgnore
		public Integer exclude;
	}

	/**
	 * Tests if a collection of primitives mapping works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSimpleCollection() throws IOException {
		ModelMapper<TestModel> mapper = MapperFactory.getMapper(TestModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("strings");
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

	/**
	 * Tests if a collection of objects mapping works
	 * 
	 * @throws IOException
	 */
	@Test
	public void testObjectCollection() throws IOException {
		ModelMapper<TestEmbeddedModel> mapper = MapperFactory.getMapper(TestEmbeddedModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		// Collection
		mapping.startObject("objects");
		mapping.startObject("properties");

		mapping.startObject("include");
		mapping.field("type", "string");
		mapping.endObject();

		mapping.endObject();
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
