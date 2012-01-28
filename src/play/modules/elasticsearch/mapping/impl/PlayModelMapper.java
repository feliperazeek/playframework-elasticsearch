package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.db.Model;
import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * ModelMapper for play.db.Model subclasses.
 * 
 * @param <M>
 *            the model type
 */
public class PlayModelMapper<M extends Model> implements ModelMapper<M> {

	/** The play-specific fields to ignore. */
	private static List<String> IGNORE_FIELDS = new ArrayList<String>();
	static {
		IGNORE_FIELDS.add("avoidCascadeSaveLoops");
		IGNORE_FIELDS.add("willBeSaved");
		IGNORE_FIELDS.add("serialVersionId");
		IGNORE_FIELDS.add("serialVersionUID");
	}

	private final Class<M> clazz;
	private final ElasticSearchable meta;
	private final List<FieldMapper<M>> mapping;

	public PlayModelMapper(Class<M> clazz) {
		Validate.notNull(clazz, "Clazz cannot be null");
		this.clazz = clazz;
		this.meta = clazz.getAnnotation(ElasticSearchable.class);

		// Create mapping
		mapping = getMapping(clazz);
	}

	static boolean shouldIgnoreField(Field field) {
		String name = field.getName();

		return StringUtils.isBlank(name) || IGNORE_FIELDS.contains(name)
				|| shouldIgnoreJPAField(field);
	}

	/**
	 * Checks if a field should be ignored based on JPA-specifics
	 * 
	 * @param field
	 *            the field to check
	 * @return true if the field should be ignored, false otherwise
	 */
	static boolean shouldIgnoreJPAField(Field field) {
		return field.isAnnotationPresent(Transient.class);
	}

	static boolean userRequestedIgnoreField(Field field) {
		return field.isAnnotationPresent(ElasticSearchIgnore.class);
	}

	/**
	 * Gets a list of {@link FieldMapper}s for the given model class
	 * 
	 * @param <M>
	 *            the model type
	 * @param clazz
	 *            the model class
	 * @return the list of FieldMappers
	 */
	private static final <M extends Model> List<FieldMapper<M>> getMapping(Class<M> clazz) {
		List<FieldMapper<M>> mapping = new ArrayList<FieldMapper<M>>();

		List<Field> indexableFields = ReflectionUtil.getAllFields(clazz);

		for (Field field : indexableFields) {

			// Exclude fields on our ignore list
			if (shouldIgnoreField(field) || userRequestedIgnoreField(field)) {
				continue;
			}

			FieldMapper<M> mapper = MapperFactory.getMapper(field);
			mapping.add(mapper);
		}

		return mapping;
	}

	@Override
	public Class<M> getModelClass() {
		return clazz;
	}

	@Override
	public String getIndexName() {
		if (meta.indexName().length() > 0) {
			return meta.indexName();
		} else {
			return getTypeName();
		}
	}

	@Override
	public String getTypeName() {
		return clazz.getName().toLowerCase().trim().replace('.', '_');
	}

	@Override
	public String getDocumentId(M model) {
		return String.valueOf(model._key());
	}

	@Override
	public void addMapping(XContentBuilder builder) throws IOException {
		builder.startObject(getTypeName());
		builder.startObject("properties");

		for (FieldMapper<M> field : mapping) {
			field.addToMapping(builder);
		}

		builder.endObject();
		builder.endObject();
	}

	@Override
	public void addModel(M model, XContentBuilder builder) throws IOException {
		builder.startObject();

		for (FieldMapper<M> field : mapping) {
			field.addToDocument(model, builder);
		}

		builder.endObject();
	}

	@Override
	public M createModel(Map<String, Object> map) {
		M model = ReflectionUtil.newInstance(clazz);

		for (FieldMapper<M> field : mapping) {
			field.inflate(model, map);
		}

		return model;
	}

}
