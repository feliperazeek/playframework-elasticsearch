package play.modules.elasticsearch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class with this interface with have a _ttl field in elastic search index:
 * http://www.elasticsearch.org/guide/reference/mapping/ttl-field.html
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSearchTtl {

	String value();

}
