package play.modules.elasticsearch.mapping;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public abstract class MappingUtil {

	private MappingUtil() {
		// No public instantiation allowed
	}

	/**
	 * Creates an {@link XContentBuilder} which contains a single mapping
	 * 
	 * @param mapper
	 *            the mapping
	 * @return the content builder
	 * @throws IOException
	 */
	public static XContentBuilder getMapping(ModelMapper<?> mapper) throws IOException {
		XContentBuilder builder = XContentFactory.safeJsonBuilder();
		builder.startObject();
		mapper.addMapping(builder);
		builder.endObject();

		return builder;
	}
}
