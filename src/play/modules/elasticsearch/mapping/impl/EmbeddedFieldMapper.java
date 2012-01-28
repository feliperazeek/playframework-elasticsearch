package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.annotations.ElasticSearchEmbedded.Mode;
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
	private final List<FieldMapper<Object>> fields;

	public EmbeddedFieldMapper(Field field, String prefix) {
		super(field, prefix);

		embed = field.getAnnotation(ElasticSearchEmbedded.class);

		// Set correct prefix in case we are in embedded mode
		String embedPrefix = null;
		if (embed.mode() == Mode.embedded) {
			if (embed.prefix().length() > 0) {
				embedPrefix = prefix(embed.prefix());
			} else {
				embedPrefix = getFieldName() + ".";
			}
		}

		// Add fieldmappers for embedded fields
		fields = new ArrayList<FieldMapper<Object>>();
		for (Field embeddedField : getFieldsToIndex(field.getType(), embed)) {
			fields.add(MapperFactory.getMapper(embeddedField, embedPrefix));
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
	public void addToMapping(XContentBuilder builder) throws IOException {
		String indexFieldName = getIndexField();

		switch (embed.mode()) {
		case embedded:
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder);
			}
			break;
		case object:
		case nested:
			builder.startObject(indexFieldName);
			builder.field("type", embed.mode().toString());
			builder.startObject("properties");
			for (FieldMapper<?> mapper : fields) {
				mapper.addToMapping(builder);
			}
			builder.endObject();
			builder.endObject();
			break;
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder) throws IOException {
		String name = getIndexField();
		Object value = getFieldValue(model);

		if (value != null) {
			switch (embed.mode()) {
			case embedded:
				for (FieldMapper<Object> mapper : fields) {
					mapper.addToDocument(value, builder);
				}
				break;
			case object:
			case nested:
				builder.startObject(name);
				for (FieldMapper<Object> mapper : fields) {
					mapper.addToDocument(value, builder);
				}
				builder.endObject();
				break;
			}
		}
	}

	@Override
	public boolean inflate(M model, Map<String, Object> map) {
		String name = getFieldName();

		// Create new target instance
		Object value = ReflectionUtil.newInstance(getFieldType());

		// Keep track if we found any field (indicator of non-null output)
		boolean nonNullValue = false;

		switch (embed.mode()) {
		case embedded:
			for (FieldMapper<Object> mapper : fields) {
				if (mapper.inflate(value, map)) {
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
					mapper.inflate(value, nestedMap);
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
