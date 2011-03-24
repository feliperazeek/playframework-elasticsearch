/**
 * 
 */
package models;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * @author felipera
 *
 */
public class SampleModelTest extends play.test.BaseTest {
	
	public void test1Case() {
		SampleModel t = new SampleModel();
		t.field1 = "field 1";
		t.field2 = "field 2";
		t.save();
	}

}
