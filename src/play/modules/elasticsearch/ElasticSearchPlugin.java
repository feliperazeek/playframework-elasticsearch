package play.modules.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.Model;
import play.modules.elasticsearch.ElasticSearchPlugin;

public class ElasticSearchPlugin extends PlayPlugin {
	
	private ThreadLocal<Client> _session = new ThreadLocal<Client>();
    
    public Client client() {
        return _session.get();
    }
	
	Node node = null;
	Client client = null;

    @Override
    public void onApplicationStart() {
        NodeBuilder nb = nodeBuilder();
        
        boolean local = false;
        if (!Play.configuration.containsKey("elasticsearch.local")) {
        	local = Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.local"));
            nb = nb.local(local);
        }
        
        boolean client = false;
        if (!Play.configuration.containsKey("elasticsearch.client")) {
        	client = Boolean.getBoolean(Play.configuration.getProperty("elasticsearch.client"));
            nb = nb.client(client);
        }
        
        if ( local == false && client == false ) {
        	local = true;
        	nb = nb.local(local);
        }
        
        if ( local ) {
        	Logger.info("Starting Play! Elastic Search in Local Mode");
        } else {
        	Logger.info("Starting Play! Elastic Search in Client Mode");
        }
        
        this.node = nb.node();
        this.client = this.node.client();
        
        _session.set(this.client);
         
    }

    @Override
    public void onEvent(String message, Object context) {
    	ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
        Client client = plugin.client();
        
        try {
        
        Logger.info("Message: %s, Object: %s", message, context);
        
        if (!message.startsWith("JPASupport"))
            return;
        
        if (message.equals("JPASupport.objectPersisted") || message.equals("JPASupport.objectUpdated")) {
        	ElasticSearchAdapter.indexModel( this.client(), context.getClass().getName(), (Model)context );
        	
        } else if (message.equals("JPASupport.objectDeleted")) {
        	ElasticSearchAdapter.deleteModel( this.client(), context.getClass().getName(), (Model)context );
        }
        
        } catch (Throwable t) {
        	throw new RuntimeException( t );
        }
        
    }

}