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
package play.modules.elasticsearch.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.annotations.ElasticSearchIgnore;
import play.modules.elasticsearch.annotations.ElasticSearchable;
import play.modules.elasticsearch.util.ExceptionUtil;
import play.modules.elasticsearch.util.ReflectionUtil;

/**
 * The Class ElasticSearchAdapter.
 */
public abstract class ElasticSearchAdapter {

	/** The play-specific fields to ignore. */
	private static List<String> IGNORE_FIELDS = new ArrayList<String>();
	static {
		IGNORE_FIELDS.add("avoidCascadeSaveLoops");
		IGNORE_FIELDS.add("willBeSaved");
		IGNORE_FIELDS.add("serialVersionId");
		IGNORE_FIELDS.add("serialVersionUID");
	}

	/**
	 * Start index.
	 * 
	 * @param client
	 *            the client
	 * @param clazz
	 *            the clazz
	 */
	public static <T extends Model> void startIndex(Client client, Class<T> clazz) {
		createIndex(client, getIndexName(clazz));
	}

	/**
	 * Creates the index.
	 * 
	 * @param client
	 *            the client
	 * @param indexName
	 *            the index name
	 */
	private static void createIndex(Client client, String indexName) {
		try {
			Logger.debug("Starting Elastic Search Index %s", indexName);
			CreateIndexResponse response = client.admin().indices().create(new CreateIndexRequest(indexName)).actionGet();
			Logger.debug("Response: %s", response);

		} catch (IndexAlreadyExistsException iaee) {
			Logger.debug("Index already exists: %s", indexName);

		} catch (Throwable t) {
			Logger.warn(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * Index model.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param client
	 *            the client
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void indexModel(Client client, T model) throws Exception {
		Logger.debug("Index Model: %s", model);

		// Check Client
		if (client == null) {
			Logger.error("Elastic Search Client is null, aborting");
			return;
		}

		// Define Content Builder
		XContentBuilder contentBuilder = null;

		// Index Model
		try {
			// Define Index Name
			String indexName = getIndexName(model.getClass());
			String typeName = getTypeName(model.getClass());
			Logger.debug("Index Name: %s", indexName);

			contentBuilder = XContentFactory.jsonBuilder().startObject();
			addModelToDocument(model, getFieldsToIndex(model.getClass()), "", contentBuilder, 0);
			contentBuilder = contentBuilder.endObject().prettyPrint();
			IndexResponse response = client.prepareIndex(indexName, typeName, model._key().toString()).setSource(contentBuilder).execute().actionGet();

			// Log Debug
			Logger.info("Index Response: %s", response);

		} finally {
			if (contentBuilder != null) {
				contentBuilder.close();
			}
		}
	}
	
	/**
	 * Get a list of fields to index
	 * 
	 * @param clazz the clazz
	 * @return the fields to index
	 */
	private static List<Field> getFieldsToIndex(Class<?> clazz) {
		// Get list of fields that should not be ignored (@ElasticSearchIgnore)
		List<Field> indexableFields = ReflectionUtil.getAllFieldsWithoutAnnotation(clazz, ElasticSearchIgnore.class);
		List<Field> fieldsToIndex = new ArrayList<Field>();
		
		for (Field field : indexableFields) {
			String name = field.getName();
			
			// Exclude fields on our ignore list
			if (StringUtils.isNotBlank(name) && (IGNORE_FIELDS.contains(name) == false)) {
				fieldsToIndex.add(field);
			}
		}
		
		return fieldsToIndex;
	}
	
	/**
	 * Get a list of fields to index
	 * 
	 * @param clazz the clazz
	 * @param embedded the annotation which specifies the fields to embed
	 * @return the fields to index
	 */
	private static List<Field> getFieldsToIndex(Class<?> clazz, ElasticSearchEmbedded embedded) {
		
		// Shortcut for case where no fields are specified
		if( embedded.fields().length == 0 ) {
			return getFieldsToIndex(clazz);
		}
		
		List<Field> indexableFields = ReflectionUtil.getAllFields(clazz);
		List<String> selectedFields = Arrays.asList(embedded.fields());
		List<Field> fieldsToIndex = new ArrayList<Field>();
		
		for (Field field : indexableFields) {
			String name = field.getName();
			
			// Exclude fields on our ignore list
			if (StringUtils.isNotBlank(name) && (IGNORE_FIELDS.contains(name) == false)) {
				
				// Include fields specified on the ElasticSearchEmbedded annotation
				if( selectedFields.contains(name) ) {
					fieldsToIndex.add(field);
				}
			}
		}
		
		if( selectedFields.size() != fieldsToIndex.size() ) {
			Logger.warn("Not all fields specified in ElasticSearchEmbedded could be found (model: %s, fields: %s)", clazz, selectedFields);
		}
		
		return fieldsToIndex;
	}
	
	/**
	 * Adds a model to the search document, tracking embedded models
	 * 
	 * @param model the model to add
	 * @param fieldsToInclude the fields to index, or null if all fields should be indexed
	 * @param prefix the prefix to prepend to the field names
	 * @param contentBuilder the content builder
	 * @param depth the current recursion depth
	 */
	private static void addModelToDocument(Object model, List<Field> fieldsToInclude, String prefix, XContentBuilder contentBuilder, int depth) throws Exception {
		
		if (depth > 2) {
			Logger.warn("3-level recursion detected, ignoring further recursion");
			return;
		}

		// Loop into each field
		for (Field field : fieldsToInclude) {
			String name = field.getName();
			Object value = ReflectionUtil.getFieldValue(model, field);
			
			if (value != null) {
				
				// Check if this is an embedded object
				if (field.isAnnotationPresent(ElasticSearchEmbedded.class)) {
					Logger.info("Field: %s%s will be embedded", prefix, name);
					
					ElasticSearchEmbedded embedded = field.getAnnotation(ElasticSearchEmbedded.class);
					List<Field> embeddedFields = getFieldsToIndex(value.getClass(), embedded);
					String embeddedPrefix = prefix + name + ".";
					
					addModelToDocument(value, embeddedFields, embeddedPrefix, contentBuilder, depth++);
				} else {
					// Plain field
					Logger.debug("Field: %s%s, Value: %s", prefix, name, value);
					contentBuilder.field(prefix + name, value);
				}
			} else {
				Logger.debug("No Value for Field: " + name);
			}
		}
	}

	/**
	 * Delete model.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param client
	 *            the client
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void deleteModel(Client client, T model) throws Exception {
		Logger.debug("Delete Model: %s", model);
		Class<T> clazz = (Class<T>) model.getClass();
		DeleteResponse response = client.prepareDelete(getIndexName(clazz), getTypeName(clazz), String.valueOf(model._key())).setOperationThreaded(false).execute().actionGet();
		Logger.debug("Delete Response: %s", response);

	}

	/**
	 * Gets the index name.
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the index name
	 */
	public static String getIndexName(Class clazz) {
		ElasticSearchable meta = (ElasticSearchable) clazz.getAnnotation(ElasticSearchable.class);
		
		if (meta.indexName().length() > 0) {
			return meta.indexName();
		} else {
			return getTypeName(clazz);
		}
	}
	
	/**
	 * Gets the type name.
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the index name
	 */
	private static String getTypeName(Class clazz) {
		return clazz.getName().toLowerCase().trim().replace('.', '_');
	}

}
