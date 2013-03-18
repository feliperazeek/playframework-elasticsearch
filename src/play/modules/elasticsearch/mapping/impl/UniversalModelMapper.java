package play.modules.elasticsearch.mapping.impl;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

import play.db.Model;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Mapper implmementation used for retrieval from multiple indices
 * 
 * @author filip.stefanak@gmail.com
 * 
 */
public class UniversalModelMapper implements ModelMapper<Model> {

	@Override
	public Class<Model> getModelClass() {
		return Model.class;
	}

	@Override
	public String getIndexName() {
		return "_all";
	}

	@Override
	public String getTypeName() {
		return "_all";
	}

	@Override
	public String getDocumentId(final Model model) {
		return "_all";
	}

	@Override
	public void addMapping(final XContentBuilder builder) throws IOException {
		throw new UnsupportedOperationException("Unsupported call to UniversalModelMapper");
	}

	@Override
	public void addModel(final Model model, final XContentBuilder builder) throws IOException {
		throw new UnsupportedOperationException("Unsupported call to UniversalModelMapper");
	}

	@Override
	public Model createModel(final Map<String, Object> map) {
		throw new UnsupportedOperationException("Model mapping is not supported with UniversalModelMapper");
	}
	
	public void addSettings(XContentBuilder builder) throws IOException {
		throw new UnsupportedOperationException("Model mapping is not supported with UniversalModelMapper");
	}

}
