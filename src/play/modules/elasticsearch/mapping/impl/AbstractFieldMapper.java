package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.lang.Validate;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchField;
import play.modules.elasticsearch.annotations.ElasticSearchField.Index;
import play.modules.elasticsearch.annotations.ElasticSearchField.Store;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.util.ReflectionUtil;

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
	protected void addField(String name, String type, XContentBuilder builder) throws IOException {
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

	/**
	 * Gets the name of the field we represent
	 * 
	 * @return
	 */
	protected String getFieldName() {
		return field.getName();
	}

	/**
	 * Gets the field type for the field we represent
	 * 
	 * @return
	 */
	protected Class<?> getFieldType() {
		return field.getType();
	}

	/**
	 * Gets the ElasticSearch field type for the field we represent
	 * 
	 * @return
	 */
	protected String getIndexType() {
		if (meta != null && meta.type().length() > 0) {
			// Type was explicitly set, use it
			return meta.type();

		} else {
			// Detect type automatically
			return MappingUtil.detectFieldType(field.getType());
		}
	}

	/**
	 * Gets the value of the field we represent, given a model instance
	 * 
	 * @param model
	 * @return
	 */
	protected Object getFieldValue(M model) {
		return ReflectionUtil.getFieldValue(model, field);
	}

}
