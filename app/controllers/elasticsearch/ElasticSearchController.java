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
package controllers.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.elasticsearch.common.base.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import play.Play;
import play.data.validation.MaxSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.modules.elasticsearch.ElasticSearch;
import play.modules.elasticsearch.Query;
import play.modules.elasticsearch.search.SearchResults;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;

/**
 * The Class ElasticSearchController.
 */
public class ElasticSearchController extends Controller {

	/**
	 * Adds the type.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public static void addType() throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		if (type == null) {
			throw new RuntimeException("Please define class for search interface with");
		}
		renderArgs.put("type", type);
	}

	/**
	 * Index.
	 */
	public static void index() {
		if (getControllerClass() == ElasticSearchController.class) {
			forbidden();
		}
		render("ELASTIC_SEARCH/index.html");
	}

	/**
	 * Search.
	 *
	 * @param page the page
	 * @param search the search
	 * @param searchFields the search fields
	 * @param orderBy the order by
	 * @param order the order
	 */
	public static void search(int page, String search, String searchFields, String orderBy, String order) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		SearchResults<Model> results = type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where"));
		List<Model> objects = results.objects;
		Long count = results.totalCount;
		Long totalCount = type.count(null, null, (String) request.args.get("where"));
		try {
			render(type, objects, count, totalCount, page, orderBy, order);
		} catch (TemplateNotFoundException e) {
			render("ELASTIC_SEARCH/search.html", type, objects, count, totalCount, page, orderBy, order);
		}
	}

	/**
	 * Show.
	 *
	 * @param id the id
	 */
	public static void show(String id) {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		Model object = type.findById(id);
		notFoundIfNull(object);
		try {
			render(type, object);
		} catch (TemplateNotFoundException e) {
			render("ELASTIC_SEARCH/show.html", type, object);
		}
	}

	// ~~~~~~~~~~~~~
	/**
	 * The Interface For.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface For {

		/**
		 * Value.
		 *
		 * @return the class<? extends model>
		 */
		Class<? extends Model> value();
	}

	/**
	 * The Interface Exclude.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Exclude {
	}

	/**
	 * The Interface Hidden.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Hidden {
	}

	// ~~~~~~~~~~~~~
	/**
	 * Gets the page size.
	 *
	 * @return the page size
	 */
	static int getPageSize() {
		return Integer.parseInt(Play.configuration.getProperty("elasticsearch.mashup.pageSize", "30"));
	}

	/**
	 * The Class ObjectType.
	 */
	public static class ObjectType implements Comparable<ObjectType> {

		/** The controller class. */
		public Class<? extends Controller> controllerClass;

		/** The entity class. */
		public Class<? extends Model> entityClass;

		/** The name. */
		public String name;

		/** The model name. */
		public String modelName;

		/** The controller name. */
		public String controllerName;

		/** The key name. */
		public String keyName;

		/**
		 * Instantiates a new object type.
		 *
		 * @param modelClass the model class
		 */
		public ObjectType(Class<? extends Model> modelClass) {
			modelName = modelClass.getSimpleName();
			entityClass = modelClass;
			keyName = Model.Manager.factoryFor(entityClass).keyName();
		}

		/**
		 * Instantiates a new object type.
		 *
		 * @param modelClass the model class
		 * @throws ClassNotFoundException the class not found exception
		 */
		@SuppressWarnings("unchecked")
		public ObjectType(String modelClass) throws ClassNotFoundException {
			this((Class<? extends Model>) Play.classloader.loadClass(modelClass));
		}

		/**
		 * For class.
		 *
		 * @param modelClass the model class
		 * @return the object type
		 * @throws ClassNotFoundException the class not found exception
		 */
		public static ObjectType forClass(String modelClass) throws ClassNotFoundException {
			return new ObjectType(modelClass);
		}

		/**
		 * Gets the.
		 *
		 * @param controllerClass the controller class
		 * @return the object type
		 */
		public static ObjectType get(Class<? extends Controller> controllerClass) {
			Class<? extends Model> entityClass = getEntityClassForController(controllerClass);
			if (entityClass == null || !Model.class.isAssignableFrom(entityClass)) {
				return null;
			}
			ObjectType type = new ObjectType(entityClass);
			type.name = getName(controllerClass.getSimpleName().replace("$", ""));
			type.controllerName = controllerClass.getSimpleName().toLowerCase().replace("$", "");
			type.controllerClass = controllerClass;
			return type;
		}

		/**
		 * Gets the name.
		 *
		 * @param name the name
		 * @return the name
		 */
		private static String getName(String name) {
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for (char c : name.toCharArray()) {
				count++;
				if (count == 1) {
					sb.append(String.valueOf(c).toUpperCase());

				} else {
					if (Character.isUpperCase(c)) {
						sb.append(" ");
					}
					sb.append(c);
				}
			}
			return sb.toString();
		}

		/**
		 * Gets the entity class for controller.
		 *
		 * @param controllerClass the controller class
		 * @return the entity class for controller
		 */
		@SuppressWarnings("unchecked")
		public static Class<? extends Model> getEntityClassForController(Class<? extends Controller> controllerClass) {
			if (controllerClass.isAnnotationPresent(For.class)) {
				return ((controllerClass.getAnnotation(For.class))).value();
			}
			for (Type it : controllerClass.getGenericInterfaces()) {
				if (it instanceof ParameterizedType) {
					ParameterizedType type = (ParameterizedType) it;
					if (((Class<?>) type.getRawType()).getSimpleName().equals("ELASTIC_SEARCHWrapper")) {
						return (Class<? extends Model>) type.getActualTypeArguments()[0];
					}
				}
			}
			String name = controllerClass.getSimpleName().replace("$", "");
			name = "models." + name.substring(0, name.length() - 1);
			try {
				return (Class<? extends Model>) Play.classloader.loadClass(name);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		/**
		 * Gets the list action.
		 *
		 * @return the list action
		 */
		public Object getListAction() {
			return Router.reverse(controllerClass.getName().replace("$", "") + ".list");
		}

		/**
		 * Gets the blank action.
		 *
		 * @return the blank action
		 */
		public Object getBlankAction() {
			return Router.reverse(controllerClass.getName().replace("$", "") + ".blank");
		}
		
		/**
		 * Builds a query which searches ES with the given search criteria
		 * 
		 * @param search the search
		 * @param searchFields the searchfields
		 * @param where the where
		 * @return the builder
		 */
		private BoolQueryBuilder buildQueryBuilder(String search, String searchFields, String where) {
			BoolQueryBuilder qb = boolQuery();
			
			if( Strings.isNullOrEmpty(search)) {
				qb.must(QueryBuilders.matchAllQuery());
			} else {
				// FIXME Currently we search in all fields and ignore searchFields
				qb.must(wildcardQuery("_all", "*" + search + "*"));
			}
			
			return qb;
		}

		/**
		 * Count.
		 *
		 * @param search the search
		 * @param searchFields the search fields
		 * @param where the where
		 * @return the long
		 */
		public Long count(String search, String searchFields, String where) {
			BoolQueryBuilder qb = buildQueryBuilder(search, searchFields, where);			
			Query<?> query = ElasticSearch.query(qb, entityClass);
			query.from(0).size(0);
			SearchResults<?> result = query.fetch();
			
			return result.totalCount;
		}

		/**
		 * Find page.
		 *
		 * @param page the page
		 * @param search the search
		 * @param searchFields the search fields
		 * @param orderBy the order by
		 * @param order the order
		 * @param where the where
		 * @return the list
		 */
		@SuppressWarnings("unchecked")
		public <M extends Model> SearchResults<M> findPage(int page, String search, String searchFields, String orderBy, String order, String where) {
			BoolQueryBuilder qb = buildQueryBuilder(search, searchFields, where);			
			Query<M> query = (Query<M>) ElasticSearch.query(qb, entityClass);
			// FIXME Currently we ignore the orderBy and order fields
			query.from((page - 1) * getPageSize()).size(getPageSize());
			query.hydrate(true);
			
			return query.fetch();
		}

		/**
		 * Find by id.
		 *
		 * @param id the id
		 * @return the model
		 */
		public Model findById(Object id) {
			if (id == null) {
				return null;
			}
			return Model.Manager.factoryFor(entityClass).findById(id);
		}

		/**
		 * Gets the fields.
		 *
		 * @return the fields
		 */
		public List<ObjectField> getFields() {
			List<ObjectField> fields = new ArrayList<ObjectField>();
			for (Model.Property f : Model.Manager.factoryFor(entityClass).listProperties()) {
				ObjectField of = new ObjectField(f);
				if (of.type != null) {
					fields.add(of);
				}
			}
			return fields;
		}

		/**
		 * Gets the field.
		 *
		 * @param name the name
		 * @return the field
		 */
		public ObjectField getField(String name) {
			for (ObjectField field : getFields()) {
				if (field.name.equals(name)) {
					return field;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(ObjectType other) {
			return modelName.compareTo(other.modelName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return modelName;
		}

		/**
		 * The Class ObjectField.
		 */
		public static class ObjectField {

			/** The property. */
			private Model.Property property;

			/** The type. */
			public String type = "unknown";

			/** The name. */
			public String name;

			/** The multiple. */
			public boolean multiple;

			/** The required. */
			public boolean required;

			/**
			 * Instantiates a new object field.
			 *
			 * @param property the property
			 */
			public ObjectField(Model.Property property) {
				Field field = property.field;
				this.property = property;
				if (CharSequence.class.isAssignableFrom(field.getType())) {
					type = "text";
					if (field.isAnnotationPresent(MaxSize.class)) {
						int maxSize = field.getAnnotation(MaxSize.class).value();
						if (maxSize > 100) {
							type = "longtext";
						}
					}
					if (field.isAnnotationPresent(Password.class)) {
						type = "password";
					}
				}
				if (Number.class.isAssignableFrom(field.getType()) || field.getType().equals(double.class) || field.getType().equals(int.class) || field.getType().equals(long.class)) {
					type = "number";
				}
				if (Boolean.class.isAssignableFrom(field.getType()) || field.getType().equals(boolean.class)) {
					type = "boolean";
				}
				if (Date.class.isAssignableFrom(field.getType())) {
					type = "date";
				}
				if (property.isRelation) {
					type = "relation";
				}
				if (property.isMultiple) {
					multiple = true;
				}
				if (field.getType().isEnum()) {
					type = "enum";
				}
				if (property.isGenerated) {
					type = null;
				}
				if (field.isAnnotationPresent(Required.class)) {
					required = true;
				}
				if (field.isAnnotationPresent(Hidden.class)) {
					type = "hidden";
				}
				if (field.isAnnotationPresent(Exclude.class)) {
					type = null;
				}
				if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
					type = null;
				}
				name = field.getName();
			}

			/**
			 * Gets the choices.
			 *
			 * @return the choices
			 */
			public List<Object> getChoices() {
				return property.choices.list();
			}
		}
	}
}
