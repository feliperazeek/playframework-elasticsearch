package play.modules.elasticsearch.mapping;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * Maps a model field to an Elastic Search index
 * 
 * @param <M>
 *            the model type
 */
public interface FieldMapper<M> {

	/**
	 * Adds to mapping
	 * 
	 * @param builder
	 * @throws IOException
	 */
	public void addToMapping(XContentBuilder builder) throws IOException;

	/**
	 * Adds to document
	 * 
	 * @param model
	 * @param builder
	 * @throws IOException
	 */
	public void addToDocument(M model, XContentBuilder builder) throws IOException;

	/**
	 * Inflates a model
	 * 
	 * @param model
	 * @param map
	 * @return True if a value was inflated, false otherwise, when no value was
	 *         present
	 */
	public boolean inflate(M model, Map<String, Object> map);

}
