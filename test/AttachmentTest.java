import java.io.File;
import java.util.List;

import javax.persistence.Entity;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.junit.Test;

import play.db.jpa.Model;
import play.modules.elasticsearch.ElasticSearch;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.search.SearchResults;
import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;

public class AttachmentTest extends UnitTest {
  MyModel model;
  @Before
  public static void before(){
    Fixtures.deleteDatabase();
  }
  
  @Entity
  @ElasticSearchable
  public static class MyModel extends Model {
    String title;
    File pdfFile;
  }
  
  @Test
  public void mappingIndexAndSearch(){
    model = new MyModel();
    model.title = "myTitle";
    model.pdfFile = new File("test/model.pdf");
    model.save();
    
    // index in progress --
    try {
      Thread.sleep(2500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    
    QueryStringQueryBuilder queryString = QueryBuilders.queryString("nice");
    SearchResults<MyModel> list = ElasticSearch.searchAndHydrate(
        queryString,
        MyModel.class);
    List<MyModel> models = list.objects;

    assertTrue(!models.isEmpty());
    assertTrue(models.get(0).title.equals("myTitle"));
  }

  
  @Test
  public void mappingIndexAndWrongSearch(){
    model = new MyModel();
    model.title = "myTitle";
    model.pdfFile = new File("test/model.pdf");
    model.save();
    
    // index in progress --
    try {
      Thread.sleep(2500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    
    QueryStringQueryBuilder queryString = QueryBuilders.queryString("wrongSearch");
    SearchResults<MyModel> list = ElasticSearch.searchAndHydrate(
        queryString,
        MyModel.class);
    List<MyModel> models = list.objects;

    assertTrue(models.isEmpty());
  }
}
