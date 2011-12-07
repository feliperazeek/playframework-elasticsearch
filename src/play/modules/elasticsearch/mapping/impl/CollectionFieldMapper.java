package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingException;
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
			type = detectFieldType(itemClass);
			List<Field> fieldsToIndex = EmbeddedFieldMapper.getFieldsToIndex(itemClass, embed);

			for (Field embeddedField : fieldsToIndex) {
				fields.add(MapperFactory.getMapper(embeddedField));
			}
		} else {
			type = detectFieldType(getCollectionType());
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
			addField(field.getName(), type, meta, builder);
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

}
