package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.modules.elasticsearch.mapping.impl.DefaultMapperFactory;
import play.test.UnitTest;

public abstract class MappingTest extends UnitTest {
	/**
	 * Creates a new builder
	 * 
	 * @return the builder
	 * @throws IOException
	 */
	protected static XContentBuilder builder() throws IOException {
		return XContentFactory.jsonBuilder().prettyPrint();
	}

	/**
	 * Writes the mapping to a builder and returns the builder
	 * 
	 * @param mapper
	 *            the mapper
	 * @return the builder
	 * @throws MappingException
	 * @throws IOException
	 */
	protected static XContentBuilder mappingFor(ModelMapper<?> mapper) throws IOException {
		XContentBuilder builder = builder();
		builder.startObject();
		mapper.addMapping(builder);
		builder.endObject();

		return builder;
	}

	/**
	 * Retrieves a mapper for the given class
	 * 
	 * @param <M>
	 *            the model type
	 * @param clazz
	 *            the model class
	 * @return the model mapper
	 * @throws MappingException
	 */
	protected static <M> ModelMapper<M> getMapper(Class<M> clazz) throws MappingException {
		return new DefaultMapperFactory().getMapper(clazz);
	}
}
