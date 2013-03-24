package play.modules.elasticsearch.annotations.analysis;

import org.elasticsearch.common.settings.Settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to define settings like custom filters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ElasticSearchSetting {

    /**
     * Name
     */
    String name();

    /**
     * Value
     */
    String value();

}
