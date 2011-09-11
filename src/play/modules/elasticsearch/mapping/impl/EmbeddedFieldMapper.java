package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
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
		List<String> fieldsToIndex = Arrays.asList(meta.fields());
		List<Field> fields = new ArrayList<Field>();

		// Set up fields
		for (Field embeddedField : ReflectionUtil.getAllFields(clazz)) {
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

}
