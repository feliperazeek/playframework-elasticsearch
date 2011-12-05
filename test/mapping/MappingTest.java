package mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.ModelMapper;
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
}
