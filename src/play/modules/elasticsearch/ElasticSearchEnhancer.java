package play.modules.elasticsearch;

import javassist.CtClass;
import javassist.CtMethod;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.modules.elasticsearch.ElasticSearchPlugin;

public class ElasticSearchEnhancer extends Enhancer {

	@Override
	public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
        CtClass ctClass = makeClass(applicationClass);
        
        if ( this.hasAnnotation(ctClass, "play.modules.elasticsearch.annotation.ElasticSearchModel") == false ) {
        	return;
        }

        String method = "public static org.elasticsearch.client.Client searchClient() { ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class); return plugin.client();  }";
        CtMethod count = CtMethod.make(method, ctClass);
        ctClass.addMethod(count);
        
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();
        
        Logger.info("Enhanced Model: %s", applicationClass.name);
        
        ElasticSearchPlugin plugin = Play.plugin(ElasticSearchPlugin.class);
        plugin.client().admin().indices().prepareCreate(applicationClass.getClass().getCanonicalName()).execute().actionGet();
	}

}
