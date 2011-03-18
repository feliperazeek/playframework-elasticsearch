package play.modules.elasticsearch;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;

import play.Logger;
import play.Play;
import play.db.jpa.Model;

// TODO: Auto-generated Javadoc
/**
 * The Interface ElasticSearchEntity.
 * 
 * @param <T>
 *            the generic type
 */
@MappedSuperclass
public class ElasticSearchModel<T extends ElasticSearchModel> extends Model {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Post.
	 */
	@PostPersist
	public void postPersist() {
		try {
			Logger.info("Elastic Search - Index Request: " + this + ", Key: " + id);
			ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
			ElasticSearchAdapter.indexModel(plugin.client(), (T) this);
			Logger.debug("Elastic Event Done!");

		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t));
			throw new RuntimeException(t);
		}
	}

	/**
	 * Post remove.
	 */
	@PostRemove
	public void postRemove() {
		try {
			Logger.info("Elastic Search - Remove Request: " + this + ", Key: " + id);
			ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
			ElasticSearchAdapter.deleteModel(plugin.client(), (T) this);
			Logger.debug("Elastic Delete Event Done!");

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
