package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * Field mapper for collection type; maps to array by default
 * 
 * @param <M>
 *            the generic model type which owns this field
 */
public class CollectionFieldMapper<M> extends AbstractFieldMapper<M> {

	private final boolean nestedMode;
	private final String type;
	private final List<FieldMapper<Object>> fields;

	public CollectionFieldMapper(Field field, String prefix) {
		super(field, prefix);

		if (!Collection.class.isAssignableFrom(field.getType())) {
			throw new MappingException("field must be of Collection type");
		}

		ElasticSearchEmbedded embed = field.getAnnotation(ElasticSearchEmbedded.class);
		nestedMode = (embed != null);

		// Detect object type in collection
		type = MappingUtil.detectFieldType(getCollectionType());

		// Find fields to use for embedded objects
		if (nestedMode) {
			Class<?> itemClass = getCollectionType();
			List<Field> fieldsToIndex = EmbeddedFieldMapper.getFieldsToIndex(itemClass, embed);
			fields = new ArrayList<FieldMapper<Object>>();

			for (Field embeddedField : fieldsToIndex) {
				fields.add(MapperFactory.getMapper(embeddedField));
			}
		} else {
			fields = null;
		}
	}

	private Class<?> getCollectionType() {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		return (Class<?>) type.getActualTypeArguments()[0];
	}

	@Override
	public void addToMapping(XContentBuilder builder) throws IOException {
		String indexFieldName = getIndexField();

		if (nestedMode) {
			// Embedded mode
			builder.startObject(indexFieldName);
			builder.startObject("properties");
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder);
			}
			builder.endObject();
			builder.endObject();
		} else {
			// Flat mode (array of primitives)
			MappingUtil.addField(builder, indexFieldName, type, meta);
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder) throws IOException {
		String indexFieldName = getIndexField();
		Collection<?> value = (Collection<?>) getFieldValue(model);

		if (value != null) {
			builder.startArray(indexFieldName);

			if (nestedMode) {
				// Embedded mode uses mapping
				for (Object object : (Collection<?>) value) {
					builder.startObject();
					for (FieldMapper<Object> mapper : fields) {
						mapper.addToDocument(object, builder);
					}
					builder.endObject();
				}
			} else {
				boolean isStringType = type.equals("string");

				// Flat mode uses primitive values or toString
				for (Object object : (Collection<?>) value) {
					// Use toString for string type
					if (isStringType) {
						builder.value(object.toString());
					} else {
						builder.value(object);
					}
				}
			}

			builder.endArray();
		}
	}

	@Override
	public boolean inflate(M model, Map<String, Object> map) {
		String indexFieldName = getIndexField();
		final List<Object> indexValue = (List<Object>) map.get(indexFieldName);
		final Collection<Object> modelValue = (Collection<Object>) getFieldValue(model);
		final Class<?> type = getCollectionType();

		// If we have input and output, continue
		if (indexValue != null && modelValue != null) {
			if (nestedMode) {
				// Embedded mode uses mapping
				for (Object indexItem : indexValue) {
					// Fetch input item fields
					Map<String, Object> indexItemMap = (Map<String, Object>) indexItem;

					// Create new target instance
					Object outputItem = ReflectionUtil.newInstance(type);

					for (FieldMapper<Object> mapper : fields) {
						mapper.inflate(outputItem, indexItemMap);
					}

					modelValue.add(outputItem);
				}
			} else {
				// Flat mode uses primitive values or toString
				for (Object indexItem : indexValue) {
					// Try to convert
					Object modelItem = MappingUtil.convertValue(indexItem, type);

					// This should only succeed for simple types
					if (type.isAssignableFrom(modelItem.getClass())) {
						modelValue.add(modelItem);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

}
