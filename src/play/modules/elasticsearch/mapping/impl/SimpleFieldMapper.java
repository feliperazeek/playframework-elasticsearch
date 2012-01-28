package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * Field mapper for simple, single-valued types
 * 
 * @param <M>
 *            the generic model type which owns this field
 */
public class SimpleFieldMapper<M> extends AbstractFieldMapper<M> {

	public SimpleFieldMapper(Field field, String prefix) {
		super(field, prefix);
	}

	@Override
	public void addToMapping(XContentBuilder builder) throws IOException {
		String field = getIndexField();
		String type = getIndexType();

		MappingUtil.addField(builder, field, type, meta);
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder) throws IOException {
		String field = getIndexField();
		Object value = getFieldValue(model);

		if (value != null) {
			builder.field(field, value);
		}
	}

	@Override
	public boolean inflate(M model, Map<String, Object> map) {
		String modelFieldName = getFieldName();
		String indexFieldName = getIndexField();

		if (map.containsKey(indexFieldName)) {
			Object value = map.get(indexFieldName);
			if (value != null) {
				ReflectionUtil.setFieldValue(model, modelFieldName, value);
				return true;
			}
		}

		return false;
	}

}
