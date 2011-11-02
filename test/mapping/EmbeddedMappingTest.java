package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.annotations.ElasticSearchEmbedded.Mode;
import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Test for embedded properties
 */
public class EmbeddedMappingTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class NotMappedModel extends Model {
		public Embedded embedded;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class EmbeddedModel extends Model {
		@ElasticSearchEmbedded
		public Embedded embedded;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class EmbeddedPrefixModel extends Model {
		@ElasticSearchEmbedded(prefix = "prefix_")
		public Embedded embedded;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class EmbeddedFieldsModel extends Model {
		@ElasticSearchEmbedded(fields = { "include", "exclude" })
		public Embedded embedded;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class NestedModel extends Model {
		@ElasticSearchEmbedded(mode = Mode.nested)
		public Embedded embedded;
	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class ObjectModel extends Model {
		@ElasticSearchEmbedded(mode = Mode.object)
		public Embedded embedded;
	}

	public static class Embedded {
		public String include;

		@ElasticSearchIgnore
		public Integer exclude;
	}

	/**
	 * Tests if an object without {@link ElasticSearchEmbedded} is mapped as a
	 * string.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNotMapped() throws IOException {
		ModelMapper<NotMappedModel> mapper = MapperFactory.getMapper(NotMappedModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("embedded");
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
	 * Tests if an object with {@link ElasticSearchEmbedded} is mapped
	 * correctly, and respects the {@link ElasticSearchIgnore} annotation on the
	 * object fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMapped() throws IOException {
		ModelMapper<EmbeddedModel> mapper = MapperFactory.getMapper(EmbeddedModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("embedded.include");
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
	 * Tests if an object with {@link ElasticSearchEmbedded} is mapped correctly
	 * by including the set prefix, and respects the {@link ElasticSearchIgnore}
	 * annotation on the object fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMappedWithPrefix() throws IOException {
		ModelMapper<EmbeddedPrefixModel> mapper = MapperFactory
				.getMapper(EmbeddedPrefixModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("prefix_include");
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
	 * Tests if an object with {@link ElasticSearchEmbedded} is mapped
	 * correctly, and includes the specified fields, even if they are marked
	 * with {@link ElasticSearchIgnore}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMappedFields() throws IOException {
		ModelMapper<EmbeddedFieldsModel> mapper = MapperFactory
				.getMapper(EmbeddedFieldsModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("embedded.include");
		mapping.field("type", "string");
		mapping.endObject();

		mapping.startObject("embedded.exclude");
		mapping.field("type", "integer");
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
	 * Tests if an object with {@link ElasticSearchEmbedded} is mapped correctly
	 * in nested mode.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNestedMode() throws IOException {
		ModelMapper<NestedModel> mapper = MapperFactory.getMapper(NestedModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("embedded");
		mapping.field("type", "nested");
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

	/**
	 * Tests if an object with {@link ElasticSearchEmbedded} is mapped correctly
	 * in object mode.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testObjectMode() throws IOException {
		ModelMapper<ObjectModel> mapper = MapperFactory.getMapper(ObjectModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("embedded");
		mapping.field("type", "object");
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
