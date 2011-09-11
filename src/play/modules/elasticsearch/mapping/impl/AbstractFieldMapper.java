package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchField;
import play.modules.elasticsearch.annotations.ElasticSearchField.Index;
import play.modules.elasticsearch.annotations.ElasticSearchField.Store;
import play.modules.elasticsearch.mapping.FieldMapper;

/**
 * Abstract base class for {@link FieldMapper}s
 * 
 * @param <M>
 *            the model type
 */
public abstract class AbstractFieldMapper<M> implements FieldMapper<M> {

	protected final Field field;
	protected final ElasticSearchField meta;

	public AbstractFieldMapper(Field field) {
		Validate.notNull(field, "field cannot be null");
		this.field = field;
		this.meta = field.getAnnotation(ElasticSearchField.class);
	}

	/**
	 * Adds a field to the content builder
	 * 
	 * @param name
	 *            the field name
	 * @param type
	 *            the field type
	 * @param meta
	 *            the ElasticSearchField annotation (optional)
	 * @param builder
	 *            the content builder
	 * @throws IOException
	 */
	protected static void addField(String name, String type, ElasticSearchField meta,
			XContentBuilder builder) throws IOException {
		// We need at least a type
		if (type != null) {
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
	}

	protected static String detectFieldType(Class<?> clazz) {
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
