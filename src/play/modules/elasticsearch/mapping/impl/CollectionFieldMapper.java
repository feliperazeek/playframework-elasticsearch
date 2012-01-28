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

	public CollectionFieldMapper(Field field) {
		super(field);

		if (!Collection.class.isAssignableFrom(field.getType())) {
			throw new MappingException("field must be of Collection type");
		}

		ElasticSearchEmbedded embed = field.getAnnotation(ElasticSearchEmbedded.class);
		nestedMode = (embed != null);

		// Find fields to use for embedded objects
		fields = new ArrayList<FieldMapper<Object>>();
		if (nestedMode) {
			Class<?> itemClass = getCollectionType();
			type = MappingUtil.detectFieldType(itemClass);
			List<Field> fieldsToIndex = EmbeddedFieldMapper.getFieldsToIndex(itemClass, embed);

			for (Field embeddedField : fieldsToIndex) {
				fields.add(MapperFactory.getMapper(embeddedField));
			}
		} else {
			type = MappingUtil.detectFieldType(getCollectionType());
		}
	}

	private Class<?> getCollectionType() {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		return (Class<?>) type.getActualTypeArguments()[0];
	}

	@Override
	public void addToMapping(XContentBuilder builder, String prefix) throws IOException {
		if (nestedMode) {
			// Embedded mode
			builder.startObject(field.getName());
			builder.startObject("properties");
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder, null);
			}
			builder.endObject();
			builder.endObject();
		} else {
			// Flat mode (array of primitives)
			addField(field.getName(), type, builder);
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder, String prefix) throws IOException {
		String name = field.getName();
		Collection<?> value = (Collection<?>) ReflectionUtil.getFieldValue(model, field);

		if (value != null) {
			builder.startArray(name);

			if (nestedMode) {
				// Embedded mode uses mapping
				for (Object object : (Collection<?>) value) {
					builder.startObject();
					for (FieldMapper<Object> mapper : fields) {
						mapper.addToDocument(object, builder, null);
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
	public boolean inflate(M model, Map<String, Object> map, String prefix) {
		String name = getFieldName();
		final List<Object> input = (List<Object>) map.get(name);
		final Collection<Object> output = (Collection<Object>) getFieldValue(model);
		final Class<?> type = getCollectionType();

		// If we have input and output, continue
		if (input != null && output != null) {
			if (nestedMode) {
				// Embedded mode uses mapping
				for (Object inputItem : input) {
					// Fetch input item fields
					Map<String, Object> inputItemFields = (Map<String, Object>) inputItem;

					// Create new target instance
					Object outputItem = ReflectionUtil.newInstance(type);

					for (FieldMapper<Object> mapper : fields) {
						mapper.inflate(outputItem, inputItemFields, null);
					}

					output.add(outputItem);
				}
			} else {
				// Flat mode uses primitive values or toString
				for (Object inputItem : input) {
					// Try to convert
					Object outputItem = MappingUtil.convertValue(inputItem, type);

					// This should only succeed for simple types
					if (type.isAssignableFrom(outputItem.getClass())) {
						output.add(outputItem);
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

}
