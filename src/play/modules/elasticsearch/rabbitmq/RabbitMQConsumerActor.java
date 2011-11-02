package play.modules.elasticsearch.rabbitmq;

import play.Logger;
import akka.actor.UntypedActor;
import play.modules.elasticsearch.ElasticSearchIndexAction;
import play.modules.elasticsearch.ElasticSearchIndexEvent;


/**
 * The Class RabbitMQConsumerActor.
 */
public class RabbitMQConsumerActor extends UntypedActor {

	/**
	 * Receive Message
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object o) throws Exception {
		// Log Debug
		Logger.info("RabbitMQ Consumer Actor: %s", o);
		
		// Check Message Type
		if ( o instanceof ElasticSearchIndexEvent ) {
			// Get Index Event
			ElasticSearchIndexEvent indexEvent = (ElasticSearchIndexEvent)o;
			
			// Fire Index Action
			ElasticSearchIndexAction indexAction = new ElasticSearchIndexAction();
			indexAction.invoke(indexEvent);
			
		} else {
			// Log Debug
			throw new RuntimeException("Unknown Message: " + o);
		}
	}

}
