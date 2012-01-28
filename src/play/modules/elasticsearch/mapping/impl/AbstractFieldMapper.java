package play.modules.elasticsearch.mapping.impl;

import java.lang.reflect.Field;

import org.apache.commons.lang.Validate;

import play.modules.elasticsearch.annotations.ElasticSearchField;
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
	private final String prefix, indexField;

	public AbstractFieldMapper(Field field, String prefix) {
		Validate.notNull(field, "field cannot be null");
		this.field = field;
		this.meta = field.getAnnotation(ElasticSearchField.class);
		this.prefix = prefix;

		// Maybe this a premature optimization, but getIndexField() will be
		// called a lot
		indexField = prefix(field.getName());
	}

	/**
	 * Gets the prefix to use when indexing this field
	 * 
	 * @return
	 */
	protected String getPrefix() {
		return prefix;
	}

	/**
	 * Prefixes a value with our prefix, if we have one
	 * 
	 * @param value
	 * @return
	 */
	protected String prefix(String value) {
		if (prefix != null) {
			return prefix + value;
		} else {
			return value;
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
	 * Gets the field we should use in the index
	 * 
	 * @return
	 */
	protected String getIndexField() {
		return indexField;
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
