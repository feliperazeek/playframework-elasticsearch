package mapping;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Tests for simple mappings (primitives)
 */
public class SimpleMappingTest extends MappingTest {

	@SuppressWarnings("serial")
	@ElasticSearchable
	public static class TestModel extends Model {
		public String _string;
		public Integer _integer1;
		public int _integer2;
		public Short _short1;
		public short _short2;
		public Long _long1;
		public long _long2;
		public Float _float1;
		public float _float2;
		public Double _double1;
		public double _double2;
		public Byte _byte1;
		public byte _byte2;
		public Date _date;
		public Boolean _boolean1;
		public boolean _boolean2;
	}

	@Test
	public void testSimpleMapping() throws IOException {
		ModelMapper<TestModel> mapper = MapperFactory.getMapper(TestModel.class);
		assertNotNull(mapper);

		// Get generated mapping
		XContentBuilder generatedMapping = mappingFor(mapper);

		// Build mapping locally for verification
		XContentBuilder mapping = builder();
		mapping.startObject();
		mapping.startObject(mapper.getTypeName());
		mapping.startObject("properties");

		mapping.startObject("_string");
		mapping.field("type", "string");
		mapping.endObject();

		mapping.startObject("_integer1");
		mapping.field("type", "integer");
		mapping.endObject();

		mapping.startObject("_integer2");
		mapping.field("type", "integer");
		mapping.endObject();

		mapping.startObject("_short1");
		mapping.field("type", "short");
		mapping.endObject();

		mapping.startObject("_short2");
		mapping.field("type", "short");
		mapping.endObject();

		mapping.startObject("_long1");
		mapping.field("type", "long");
		mapping.endObject();

		mapping.startObject("_long2");
		mapping.field("type", "long");
		mapping.endObject();

		mapping.startObject("_float1");
		mapping.field("type", "float");
		mapping.endObject();

		mapping.startObject("_float2");
		mapping.field("type", "float");
		mapping.endObject();

		mapping.startObject("_double1");
		mapping.field("type", "double");
		mapping.endObject();

		mapping.startObject("_double2");
		mapping.field("type", "double");
		mapping.endObject();

		mapping.startObject("_byte1");
		mapping.field("type", "byte");
		mapping.endObject();

		mapping.startObject("_byte2");
		mapping.field("type", "byte");
		mapping.endObject();

		mapping.startObject("_date");
		mapping.field("type", "date");
		mapping.endObject();

		mapping.startObject("_boolean1");
		mapping.field("type", "boolean");
		mapping.endObject();

		mapping.startObject("_boolean2");
		mapping.field("type", "boolean");
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
