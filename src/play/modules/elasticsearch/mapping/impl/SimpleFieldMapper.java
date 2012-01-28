package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.lang.reflect.Field;

import org.elasticsearch.common.xcontent.XContentBuilder;

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
		String type = getFieldType();

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

}
