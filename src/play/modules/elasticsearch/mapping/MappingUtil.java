package play.modules.elasticsearch.mapping;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import play.Logger;
import play.modules.elasticsearch.annotations.ElasticSearchField;
import play.modules.elasticsearch.annotations.ElasticSearchField.Index;
import play.modules.elasticsearch.annotations.ElasticSearchField.Store;
import play.modules.elasticsearch.util.ExceptionUtil;

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
	 * Adds a field to the content builder
	 * 
	 * @param builder
	 *            the content builder
	 * @param name
	 *            the field name
	 * @param type
	 *            the field type
	 * @param meta
	 *            the ElasticSearchField annotation (optional) *
	 * @throws IOException
	 */
	public static void addField(XContentBuilder builder, String name, String type,
			ElasticSearchField meta) throws IOException {
		Validate.notEmpty(name, "name cannot be empty");
		Validate.notEmpty(type, "type cannot be empty");

		builder.startObject(name);
		builder.field("type", type);

		// Check for other settings
		if (meta != null) {
			if (meta.index() != Index.NOT_SET) {
				builder.field("index", meta.index().toString());
			}
			if (meta.store() != Store.NOT_SET) {
				builder.field("store", meta.store().toString());
			}
		}

		builder.endObject();
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

	public static Object convertValue(final Object value, final Class<?> targetType) {
		if (targetType.equals(value.getClass())) {
			// Types match
			return value;
		}

		// Types do not match, perform conversion where needed
		if (targetType.equals(String.class)) {
			return value.toString();
		} else if (targetType.equals(BigDecimal.class)) {
			return new BigDecimal(value.toString());
		} else if (targetType.equals(Date.class)) {
			return convertToDate(value);

			// Use Number intermediary where possible
		} else if (targetType.equals(Integer.class)) {
			if (value instanceof Number) {
				return Integer.valueOf(((Number) value).intValue());
			} else {
				return Integer.valueOf(value.toString());
			}
		} else if (targetType.equals(Long.class)) {
			if (value instanceof Number) {
				return Long.valueOf(((Number) value).longValue());
			} else {
				return Long.valueOf(value.toString());
			}
		} else if (targetType.equals(Double.class)) {
			if (value instanceof Number) {
				return Double.valueOf(((Number) value).doubleValue());
			} else {
				return Double.valueOf(value.toString());
			}
		} else if (targetType.equals(Float.class)) {
			if (value instanceof Number) {
				return Float.valueOf(((Number) value).floatValue());
			} else {
				return Float.valueOf(value.toString());
			}

			// Fallback to simply returning the value
		} else {
			return value;
		}
	}

	/**
	 * Convert to date.
	 * 
	 * @param value
	 *            the value
	 * @return the date
	 */
	private static Date convertToDate(Object value) {
		Date date = null;
		if (value != null && !"".equals(value)) {
			if (value instanceof Long) {
				date = new Date(((Long) value).longValue());

			} else if (value instanceof String) {
				String val = (String) value;
				int dateLength = String.valueOf(Long.MAX_VALUE).length();
				if (dateLength == val.length()) {
					date = new Date(Long.valueOf(val).longValue());
				} else {
					date = getDate(val);
				}
			} else {
				date = (Date) value;
			}
		}
		return date;
	}

	/**
	 * Gets the date.
	 * 
	 * @param val
	 *            the val
	 * @return the date
	 */
	private static Date getDate(String val) {
		try {
			// Use ES internal converter
			return XContentBuilder.defaultDatePrinter.parseDateTime(val).toDate();
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t), val);
		}
		return null;
	}
}
