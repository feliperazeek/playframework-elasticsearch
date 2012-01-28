package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;

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
		String name = field.getName();
		String type = getFieldType();

		if (prefix != null) {
			addField(prefix + name, type, builder);
		} else {
			addField(name, type, builder);
		}
	}

	@Override
	public void addToDocument(M model, XContentBuilder builder, String prefix) throws IOException {
		String name = field.getName();
		Object value = ReflectionUtil.getFieldValue(model, field);

		if (value != null) {
			if (prefix != null) {
				builder.field(prefix + name, value);
			} else {
				builder.field(name, value);
			}
		}
	}

	protected String getFieldType() {
		if (meta != null && meta.type().length() > 0) {
			// Type was explicitly set, use it
			return meta.type();

		} else {
			// Detect type automatically
			return detectFieldType(field.getType());
		}
	}

}
