package mapping;

import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.test.UnitTest;

/**
 * Simple tests for the ElasticSearchable annotation
 */
public class ElasticSearchableTest extends UnitTest {

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
		MapperFactory.getMapper(TestNotSearchable.class);
	}

	@Test(expected = MappingException.class)
	public void testModelNotSearchable() {
		MapperFactory.getMapper(TestModelNotSearchable.class);
	}

	@Test
	public void testModel() {
		ModelMapper<TestModel> mapper = MapperFactory.getMapper(TestModel.class);
		assertNotNull(mapper);
	}

	@Test
	public void testModelWithIndexName() {
		ModelMapper<TestModelWithIndexName> mapper = MapperFactory
				.getMapper(TestModelWithIndexName.class);
		assertNotNull(mapper);
		assertEquals("testindex", mapper.getIndexName());
	}

}
