/**
 * 
 */
package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
import play.modules.elasticsearch.ElasticSearchable;

// TODO: Auto-generated Javadoc
/**
 * The Class SampleModel.
 *
 * @author felipera
 */
@Entity
@ElasticSearchable
public class SampleModel extends Model {
	
	/** The field1. */
	public String field1;
	
	/** The field2. */
	public String field2;

}
