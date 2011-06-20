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
package play.modules.elasticsearch;

import play.db.Model;

/**
 * The Class ElasticSearchIndexEvent.
 */
public class ElasticSearchIndexEvent {

	/** The object. */
	private Model object;

	/** The type. */
	private Type type;

	/**
	 * Instantiates a new elastic search index event.
	 * 
	 * @param object
	 *            the object
	 * @param type
	 *            the type
	 */
	public ElasticSearchIndexEvent(Model object, Type type) {
		super();
		this.object = object;
		this.type = type;
	}

	/**
	 * The Enum Type.
	 */
	public static enum Type {

		/** The DELETE. */
		DELETE,

		/** The INDEX. */
		INDEX;

	}

	/**
	 * Gets the object.
	 * 
	 * @return the object
	 */
	public Model getObject() {
		return this.object;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * To String
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ElasticSearchIndexEvent [object=" + this.object + ", type=" + this.type + "]";
	}

}
