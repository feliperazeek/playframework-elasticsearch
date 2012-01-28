package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.util.ReflectionUtil;
import scala.actors.threadpool.Arrays;

/**
 * Field mapper for embedded objects
 * 
 * @param <M>
 *            the generic model type which owns this field
 */
public class EmbeddedFieldMapper<M> extends AbstractFieldMapper<M> {

	private final ElasticSearchEmbedded embed;
	private final String prefix;
	private final List<FieldMapper<Object>> fields;

	public EmbeddedFieldMapper(Field field) {
		super(field);

		embed = field.getAnnotation(ElasticSearchEmbedded.class);

		// Check for prefix
		if (embed.prefix().length() > 0) {
			this.prefix = embed.prefix();
		} else {
			this.prefix = field.getName() + ".";
		}

		// Add fieldmappers for embedded fields
		fields = new ArrayList<FieldMapper<Object>>();
		for (Field embeddedField : getFieldsToIndex(field.getType(), embed)) {
			fields.add(MapperFactory.getMapper(embeddedField));
		}
	}

	static List<Field> getFieldsToIndex(Class<?> clazz, ElasticSearchEmbedded meta) {
		@SuppressWarnings("unchecked")
		List<String> fieldsToIndex = Arrays.asList(meta.fields());
		List<Field> clazzFields = ReflectionUtil.getAllFields(clazz);
		List<Field> fields = new ArrayList<Field>();

		// Make sure the user has not requested unknown fields
		if (fieldsToIndex.size() > 0) {
			for (String fieldName : fieldsToIndex) {
				boolean knownField = false;
				for (Field clazzField : clazzFields) {
					if (clazzField.getName().equals(fieldName)) {
						knownField = true;
						break;
					}
				}

				if (!knownField) {
					throw new MappingException("Unknown field specified in " + meta);
				}
			}
		}

		// Set up fields
		for (Field embeddedField : clazzFields) {
			if (PlayModelMapper.shouldIgnoreField(embeddedField)) {
				continue;
			}

			// If no fields were requested and it's marked, ignore it
			if (fieldsToIndex.size() == 0
					&& PlayModelMapper.userRequestedIgnoreField(embeddedField)) {
				continue;
			}

			// If specific fields are requested, and this is not one of them,
			// ignore it
			if (fieldsToIndex.size() > 0 && !fieldsToIndex.contains(embeddedField.getName())) {
				continue;
			}

			// Add it
			fields.add(embeddedField);
		}

		return fields;
	}

	@Override
	public void addToMapping(XContentBuilder builder, String prefix) throws IOException {
		String name = field.getName();

		switch (embed.mode()) {
		case embedded:
			String prefixToUse = (prefix != null) ? prefix + this.prefix : this.prefix;
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder, prefixToUse);
			}
			break;
		case object:
		case nested:
			builder.startObject(name);
			builder.field("type", embed.mode().toString());
			builder.startObject("properties");
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder, null);
			}
			builder.endObject();
			builder.endObject();
			break;
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder, String prefix) throws IOException {
		String name = field.getName();
		Object value = ReflectionUtil.getFieldValue(model, field);

		if (value != null) {
			switch (embed.mode()) {
			case embedded:
				String prefixToUse = (prefix != null) ? prefix + this.prefix : this.prefix;
				for (FieldMapper<Object> mapper : fields) {
					mapper.addToDocument(value, builder, prefixToUse);
				}
				break;
			case object:
			case nested:
				builder.startObject(name);
				for (FieldMapper<Object> mapper : fields) {
					mapper.addToDocument(value, builder, null);
				}
				builder.endObject();
				break;
			}
		}
	}

	@Override
	public boolean inflate(M model, Map<String, Object> map, String prefix) {
		String name = getFieldName();

		// Create new target instance
		Object value = ReflectionUtil.newInstance(getFieldType());

		// Keep track if we found any field (indicator of non-null output)
		boolean nonNullValue = false;

		switch (embed.mode()) {
		case embedded:
			String prefixToUse = (prefix != null) ? prefix + this.prefix : this.prefix;
			for (FieldMapper<Object> mapper : fields) {
				if (mapper.inflate(value, map, prefixToUse)) {
					nonNullValue = true;
				}
			}
			break;
		case object:
		case nested:
			Object input = map.get(name);
			if (input != null) {
				Map<String, Object> nestedMap = (Map<String, Object>) input;

				for (FieldMapper<Object> mapper : fields) {
					mapper.inflate(value, nestedMap, null);
				}

				nonNullValue = true;
			}
			break;
		}

		if (nonNullValue) {
			ReflectionUtil.setFieldValue(model, name, value);
			return true;
		} else {
			return false;
		}
	}

}
