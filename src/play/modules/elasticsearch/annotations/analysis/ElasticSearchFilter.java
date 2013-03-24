package play.modules.elasticsearch.annotations.analysis;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface that defines a filter;
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchFilter {

    String name();

    String typeName();

    ElasticSearchSetting[] settings() default {};
}