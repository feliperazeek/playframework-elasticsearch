package play.modules.elasticsearch.annotations.analysis;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * interface that defines an analyzer in the of an analysis
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchAnalyzer {

    String name();

    String tokenizer();

    String[] filtersNames() default {};
}