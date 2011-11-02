package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchField;
import play.modules.elasticsearch.annotations.ElasticSearchField.Index;
import play.modules.elasticsearch.annotations.ElasticSearchField.Store;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Tests for {@link ElasticSearchField} properties
 */
public class FieldPropertiesTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestModel extends Model {
		@ElasticSearchField(index = Index.not_analyzed, store = Store.yes, type = "test")
		public String field;
	}

	@Test
	public void testFieldProperties() throws IOException {
		ModelMapper<TestModel> mapper = MapperFactory.getMapper(TestModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		// Order matters, see AbstractFieldMapper
		mapping.startObject("field");
		mapping.field("type", "test");
		mapping.field("index", "not_analyzed");
		mapping.field("store", "yes");
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
