package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchField;
import play.modules.elasticsearch.annotations.ElasticSearchField.Index;
import play.modules.elasticsearch.annotations.ElasticSearchField.Store;
import play.modules.elasticsearch.annotations.ElasticSearchMultiField;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.ModelMapper;

public class MultiFieldTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestModel extends Model {
		@ElasticSearchMultiField({
				@ElasticSearchField(index = Index.not_analyzed, store = Store.yes, type = "string"),
				@ElasticSearchField(index = Index.analyzed, store = Store.no, type = "string")
		})
		public String name;
	}

	@Test
	public void testFieldProperties() throws IOException {
		ModelMapper<TestModel> mapper = getMapper(TestModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		// Order matters, see AbstractFieldMapper
		mapping.startObject("name");
		mapping.field("type", "multi_field");
		mapping.startObject("fields");
		mapping.startObject("untouched");
		mapping.field("type", "string");
		mapping.field("index", "not_analyzed");
		mapping.field("store", "yes");
		mapping.endObject();
		mapping.startObject("name");
		mapping.field("type", "string");
		mapping.field("index", "analyzed");
		mapping.field("store", "no");
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
