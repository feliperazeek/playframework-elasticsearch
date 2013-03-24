package play.modules.elasticsearch.annotations.analysis;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * interface that defines the analysis
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchAnalysis {

    ElasticSearchFilter[] filters() default {};

    ElasticSearchAnalyzer[] analyzers() default {};
}
