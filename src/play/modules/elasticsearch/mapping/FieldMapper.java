package play.modules.elasticsearch.mapping;

import java.io.IOException;

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
	 * @param prefix
	 * @throws IOException
	 */
	public void addToMapping(XContentBuilder builder, String prefix) throws IOException;

	/**
	 * Adds to document
	 * 
	 * @param model
	 * @param builder
	 * @param prefix
	 * @throws IOException
	 */
	public void addToDocument(M model, XContentBuilder builder, String prefix) throws IOException;

}
