package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.annotations.analysis.ElasticSearchAnalysis;
import play.modules.elasticsearch.annotations.analysis.ElasticSearchAnalyzer;
import play.modules.elasticsearch.annotations.analysis.ElasticSearchFilter;
import play.modules.elasticsearch.annotations.analysis.ElasticSearchSetting;

@ElasticSearchable(analysis = @ElasticSearchAnalysis(
	filters = {
			@ElasticSearchFilter(name = "myLength", typeName = "length",
                    settings = {
							@ElasticSearchSetting(name = "min", value = "0"),
                            @ElasticSearchSetting(name = "max", value = "5")
                    }),
           @ElasticSearchFilter(name = "myEdgeNGram", typeName = "edgeNGram",
                    settings = {
        		   			@ElasticSearchSetting(name = "min_gram", value = "2"),
        		   			@ElasticSearchSetting(name = "max_gram", value = "10"),
        		   			@ElasticSearchSetting(name = "side", value = "front")
                    })
	},
	analyzers = {
		@ElasticSearchAnalyzer(name="default", tokenizer="standard", filtersNames={"lowercase", "asciifolding", "myLength", "myEdgeNGram"})
}))
@Entity(name="product")
public class Product extends Model {	
	
	public String name;
    public String model;

}