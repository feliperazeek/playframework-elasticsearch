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

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import play.Logger;
import play.db.Model;
import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.mapping.ModelMapper;
import play.modules.elasticsearch.util.ExceptionUtil;

/**
 * The Class ElasticSearchAdapter.
 */
public abstract class ElasticSearchAdapter {

	/**
	 * Start index.
	 * 
	 * @param client
	 *            the client
	 * @param mapper
	 *            the model mapper
	 */
	public static <T extends Model> void startIndex(Client client, ModelMapper<T> mapper) {
		createIndex(client, mapper);
		createType(client, mapper);
	}

	/**
	 * Creates the index.
	 * 
	 * @param client
	 *            the client
	 * @param mapper
	 *            the model mapper
	 */
	private static void createIndex(Client client, ModelMapper<?> mapper) {
		String indexName = mapper.getIndexName();

		try {
			Logger.debug("Starting Elastic Search Index %s", indexName);
			CreateIndexResponse response = client.admin().indices()
					.create(new CreateIndexRequest(indexName)).actionGet();
			Logger.debug("Response: %s", response);

		} catch (IndexAlreadyExistsException iaee) {
			Logger.debug("Index already exists: %s", indexName);

		} catch (Throwable t) {
			Logger.warn(ExceptionUtil.getStackTrace(t));
		}
	}

	/**
	 * Creates the type.
	 * 
	 * @param client
	 *            the client
	 * @param mapper
	 *            the model mapper
	 */
	private static void createType(Client client, ModelMapper<?> mapper) {
		String indexName = mapper.getIndexName();
		String typeName = mapper.getTypeName();

		try {
			Logger.debug("Create Elastic Search Type %s/%s", indexName, typeName);
			PutMappingRequest request = Requests.putMappingRequest(indexName).type(typeName);
			XContentBuilder mapping = MappingUtil.getMapping(mapper);
			Logger.debug("Type mapping: \n %s", mapping.string());
			request.source(mapping);
			PutMappingResponse response = client.admin().indices().putMapping(request).actionGet();
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
	 * @param mapper
	 *            the model mapper
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void indexModel(Client client, ModelMapper<T> mapper, T model)
			throws Exception {
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
			String indexName = mapper.getIndexName();
			String typeName = mapper.getTypeName();
			String documentId = mapper.getDocumentId(model);
			Logger.debug("Index Name: %s", indexName);

			contentBuilder = XContentFactory.jsonBuilder().prettyPrint();
			mapper.addModel(model, contentBuilder);
			Logger.debug("Index json: %s", contentBuilder.string());
			IndexResponse response = client.prepareIndex(indexName, typeName, documentId)
					.setSource(contentBuilder).execute().actionGet();

			// Log Debug
			Logger.info("Index Response: %s", response);

		} finally {
			if (contentBuilder != null) {
				contentBuilder.close();
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
	 * @param mapper
	 *            the model mapper
	 * @param model
	 *            the model
	 * @throws Exception
	 *             the exception
	 */
	public static <T extends Model> void deleteModel(Client client, ModelMapper<T> mapper, T model)
			throws Exception {
		Logger.debug("Delete Model: %s", model);
		String indexName = mapper.getIndexName();
		String typeName = mapper.getTypeName();
		String documentId = mapper.getDocumentId(model);
		DeleteResponse response = client.prepareDelete(indexName, typeName, documentId)
				.setOperationThreaded(false).execute().actionGet();
		Logger.debug("Delete Response: %s", response);

	}

}
