package play.modules.elasticsearch.mapping;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Date;

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

	/**
	 * Detect the ElasticSearch field type for a {@code Class}
	 * 
	 * @param clazz
	 * @return
	 */
	public static String detectFieldType(Class<?> clazz) {
		// Core types
		if (String.class.isAssignableFrom(clazz)) {
			return "string";
		} else if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
			return "integer";
		} else if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
			return "short";
		} else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
			return "long";
		} else if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
			return "float";
		} else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
			return "double";
		} else if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)) {
			return "byte";
		} else if (Date.class.isAssignableFrom(clazz)) {
			return "date";
		} else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
			return "boolean";
		}

		// Fall back to string mapping
		return "string";
	}
}
