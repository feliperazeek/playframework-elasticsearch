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
			Logger.info("Elastic Event Start - Object: " + this + ", Key: " + id);
			ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
			ElasticSearchAdapter.indexModel(plugin.client(), (T) this);
			Logger.info("Elastic Event Done!");

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Post remove.
	 */
	@PostRemove
	public void postRemove() {
		try {
			Logger.info("Elastic Delete Event Start - Object: " + this
					+ ", Key: " + id);
			ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
			ElasticSearchAdapter.deleteModel(plugin.client(), (T) this);
			Logger.info("Elastic Delete Event Done!");

		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

}
