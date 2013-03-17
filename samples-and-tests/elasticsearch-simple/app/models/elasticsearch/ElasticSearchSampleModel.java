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
package models.elasticsearch;

import javax.persistence.Entity;

import play.db.jpa.Model;

// TODO: Auto-generated Javadoc
/**
 * The Class ElasticSearchSampleModel.
 */
@Entity
@play.modules.elasticsearch.annotations.ElasticSearchable
public class ElasticSearchSampleModel extends Model {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The field1. */
	public String field1;

	/** The field2. */
	public String field2;

	/**
	 * To String
	 * 
	 * @see play.db.jpa.JPABase#toString()
	 */
	@Override
	public String toString() {
		return "ElasticSearchSampleModel [field1=" + this.field1 + ", field2=" + this.field2 + "]";
	}

}
