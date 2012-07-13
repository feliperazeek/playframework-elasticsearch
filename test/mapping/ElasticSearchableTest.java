package mapping;

import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Simple tests for the ElasticSearchable annotation
 */
public class ElasticSearchableTest extends MappingTest {

	private static class TestNotSearchable {

	}

	@SuppressWarnings("serial")
	private static class TestModelNotSearchable extends Model {

	}

	@SuppressWarnings("serial")
	@ElasticSearchable
	private static class TestModel extends Model {

	}

	@SuppressWarnings("serial")
	@ElasticSearchable(indexName = "testindex")
	private static class TestModelWithIndexName extends Model {

	}

	@Test(expected = MappingException.class)
	public void testNotSearchable() {
		getMapper(TestNotSearchable.class);
	}

	@Test(expected = MappingException.class)
	public void testModelNotSearchable() {
		getMapper(TestModelNotSearchable.class);
	}

	@Test
	public void testModel() {
		ModelMapper<TestModel> mapper = getMapper(TestModel.class);
		assertNotNull(mapper);
	}

	@Test
	public void testModelWithIndexName() {
		ModelMapper<TestModelWithIndexName> mapper = getMapper(TestModelWithIndexName.class);
		assertNotNull(mapper);
		assertEquals("testindex", mapper.getIndexName());
	}

}
