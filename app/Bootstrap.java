import models.SampleModel;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * @author felipera
 *
 */
public class Bootstrap {
	
	@OnApplicationStart
	public class ElasticSearchModel extends Job {
	 
	    public void doJob() {
	        // Check if the database is empty
	        //if (SampleModel.count() == 0) {
	        	Fixtures.load("initial-data.yml");
	        //}
	    }
	 
	}

}
