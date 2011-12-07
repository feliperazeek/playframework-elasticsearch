package play.modules.elasticsearch.mapping;

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public abstract class MappingUtil {

	private MappingUtil() {
		// No public instantiation allowed
	}

	/**
	 * Checks if a class is searchable
	 * 
	 * @param clazz
	 *            the class to check
	 * @return true if searchable, false otherwise
	 */
	public static boolean isSearchable(Class<?> clazz) {
		// TODO Any particular reason not to use clazz.isAnnotationPresent()?
		while (clazz != null) {
			// Logger.info("Class: %s", clazz);
			for (Annotation a : clazz.getAnnotations()) {
				// Logger.info("Class: %s - Annotation: %s", clazz,
				// a.toString());
				if (a.toString().indexOf("ElasticSearchable") > -1) {
					return true;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return false;
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
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		mapper.addMapping(builder);
		builder.endObject();

		return builder;
	}
}
