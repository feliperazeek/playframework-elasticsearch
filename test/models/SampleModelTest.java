/** 
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Felipe Oliveira (http://mashup.fm)
 * 
 */
package models;

import models.elasticsearch.ElasticSearchSampleModel;

/**
 * The Class SampleModelTest.
 */
public class SampleModelTest extends play.test.BaseTest {

	/**
	 * Test1 case.
	 */
	public void test1Case() {
		ElasticSearchSampleModel t = new ElasticSearchSampleModel();
		t.field1 = "field 1";
		t.field2 = "field 2";
		t.save();
	}

}
