package play.modules.elasticsearch.annotations;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.common.collect.Lists;

public class ElasticSearchFieldDescriptor {

	private List<ElasticSearchField> annotations = Lists.newArrayList();

	public ElasticSearchFieldDescriptor(Field field) {
		if (field.getAnnotation(ElasticSearchField.class) != null) {
			this.annotations = Lists.newArrayList(field.getAnnotation(ElasticSearchField.class));
		} else if (field.getAnnotation(ElasticSearchMultiField.class) != null) {
			this.annotations = Lists.newArrayList(field.getAnnotation(ElasticSearchMultiField.class).value());
		}
	}

	public String type() {
		return annotations.get(0).type();
	}

	public Collection<ElasticSearchField> getFields() {
		return annotations;
	}

	public boolean hasType() {
		return hasField() && getField().type().length() > 0;
	}

	public ElasticSearchField getField() {
		return annotations.get(0);
	}

	public boolean isMultiField() {
		return annotations.size() > 1;
	}

	public boolean hasField() {
		return annotations.isEmpty() == false;
	}
}
