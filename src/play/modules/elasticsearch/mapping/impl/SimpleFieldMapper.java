package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * Field mapper for simple, single-valued types
 * 
 * @param <M>
 *            the generic model type which owns this field
 */
public class SimpleFieldMapper<M> extends AbstractFieldMapper<M> {

	public SimpleFieldMapper(Field field) {
		super(field);
	}

	@Override
	public void addToMapping(XContentBuilder builder, String prefix) throws IOException {
		String name = getFieldName();
		String type = getIndexType();

		if (prefix != null) {
			addField(prefix + name, type, builder);
		} else {
			addField(name, type, builder);
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder, String prefix) throws IOException {
		String name = getFieldName();
		Object value = getFieldValue(model);

		if (value != null) {
			if (prefix != null) {
				builder.field(prefix + name, value);
			} else {
				builder.field(name, value);
			}
		}
	}

	@Override
	public boolean inflate(M model, Map<String, Object> map, String prefix) {
		String name = getFieldName();
		String indexName = (prefix != null) ? prefix + name : name;

		if (map.containsKey(name)) {
			Object value = map.get(indexName);
			if (value != null) {
				ReflectionUtil.setFieldValue(model, name, value);
				return true;
			}
		}

		return false;
	}

}
