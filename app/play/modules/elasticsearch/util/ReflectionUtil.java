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
package play.modules.elasticsearch.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import play.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class ReflectionUtil.
 */
public abstract class ReflectionUtil {

	/** The Constant annotationFieldsCache. */
	private static final ConcurrentMap<ClassAnnotationHolder, AnnotationFieldsHolder> annotationFieldsCache = new ConcurrentHashMap<ClassAnnotationHolder, AnnotationFieldsHolder>();

	/** The Constant classFieldsCache. */
	private static final ConcurrentMap<String, List<Field>> classFieldsCache = new ConcurrentHashMap<String, List<Field>>();

	/** The Constant DATE_FORMAT. */
	public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSS";

	/**
	 * Instantiates a new reflection util.
	 */
	private ReflectionUtil() {
		// private
	}

	/**
	 * Gets the all fields.
	 *
	 * @param originalClass the original class
	 * @return the all fields
	 */
	public static List<Field> getAllFields(final Class<?> originalClass) {

		Class<?> clazz = originalClass;

		// class name is the key
		final String className = clazz.getCanonicalName();

		// return from cache
		if (classFieldsCache.containsKey(className)) {
			List<Field> fields = classFieldsCache.get(className);
			return fields;
		}

		// Init Counter
		int count = 0;

		// Init List
		final List<Field> fields = new ArrayList<Field>();

		// Get all the fields including superclasses
		while (clazz != null) {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
			count++;
		}

		// Check Count
		if (count > 10) {
			Logger.warn("Too many iterations on ReflectionUtil.getAllFields() - class: " + originalClass);
		}

		// make the list unmodifiable
		List<Field> unmodifiableFields = Collections.<Field> unmodifiableList(fields);

		// put into cache
		classFieldsCache.put(className, unmodifiableFields);

		// Return List
		return unmodifiableFields;
	}

	/**
	 * Gets the field value.
	 *
	 * @param object the object
	 * @param fieldName the field name
	 * @return the field value
	 */
	public static Object getFieldValue(Object object, String fieldName) {
		try {
			Class<?> clazz = object.getClass();

			// Get all the fields including superclasses
			while (clazz != null) {
				for (Field field : clazz.getDeclaredFields()) {
					if (field.getName().equals(fieldName)) {
						field.setAccessible(true);
						return field.get(object);
					}
				}
				clazz = clazz.getSuperclass();
			}
		} catch (Exception e) {
			Logger.warn(ExceptionUtil.getStackTrace(e));
			// throw new RuntimeException( e );
		}
		return null;
	}

	/**
	 * Gets the all field names with type.
	 *
	 * @param originalClass the original class
	 * @param type the type
	 * @return the all field names with type
	 */
	public static List<String> getAllFieldNamesWithType(final Class<?> originalClass, Class<?> type) {

		Class<?> clazz = originalClass;

		// Init List
		List<String> fieldNames = new ArrayList<String>();

		// Init Counter
		int count = 0;

		// Get all the fields including superclasses
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (type.isAssignableFrom(field.getType())) {
					fieldNames.add(field.getName());
				}
			}
			clazz = clazz.getSuperclass();
			count++;
		}

		// Check Count
		if (count > 10) {
			Logger.warn("Too many iterations on ReflectionUtil.getFieldNamesWithType() - class: " + originalClass + " - " + type);
		}

		// Return List
		return fieldNames;
	}

	/**
	 * Checks for annotation.
	 *
	 * @param field the field
	 * @param clazz the clazz
	 * @return true, if successful
	 */
	public static boolean hasAnnotation(Field field, Class<? extends Annotation> clazz) {
		return field.isAnnotationPresent(clazz);
	}

	/**
	 * New instance.
	 *
	 * @param className the class name
	 * @return the object
	 */
	public static Object newInstance(String className) {
		Class<?> clazz;
		try {
			clazz = Class.forName(className);
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * New instance.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @return the t
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the field names with annotation.
	 *
	 * @param mainClass the main class
	 * @param annotationClass the annotation class
	 * @return the field names with annotation
	 */
	public static List<String> getFieldNamesWithAnnotation(Class<?> mainClass, Class<? extends Annotation> annotationClass) {

		final ClassAnnotationHolder holder = new ClassAnnotationHolder(mainClass, annotationClass);

		// get from cache, if present
		if (annotationFieldsCache.containsKey(holder)) {
			AnnotationFieldsHolder fieldHolder = annotationFieldsCache.get(holder);
			return fieldHolder.getFieldNames();
		}

		// Init List
		List<Field> fieldsWithAnnotation = new ArrayList<Field>();

		List<Field> fields = ReflectionUtil.getAllFields(mainClass);

		// Do Work
		for (Field field : fields) {
			if (field.isAnnotationPresent(annotationClass)) {
				fieldsWithAnnotation.add(field);
			}
		}

		final AnnotationFieldsHolder fieldHolder = new AnnotationFieldsHolder(fieldsWithAnnotation);

		// put into cache
		annotationFieldsCache.put(holder, fieldHolder);

		// Return List
		return fieldHolder.getFieldNames();
	}

	/**
	 * Gets the all field names without annotation.
	 *
	 * @param clazz the clazz
	 * @param annotationClass the annotation class
	 * @return the all field names without annotation
	 */
	public static List<String> getAllFieldNamesWithoutAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		String className = clazz.getName() + ".";
		List<String> fieldNames = new ArrayList<String>();

		// Get all the fields including superclasses
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(annotationClass)) {
					fieldNames.add(className + field.getName());
				}
			}
			clazz = clazz.getSuperclass();
		}
		return fieldNames;
	}

	/**
	 * Gets the all fields without annotation.
	 *
	 * @param clazz the clazz
	 * @param annotationClass the annotation class
	 * @return the all fields without annotation
	 */
	public static List<Field> getAllFieldsWithoutAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		List<Field> list = new ArrayList<Field>();

		// Get all the fields including superclasses
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(annotationClass)) {
					list.add(field);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return list;
	}

	/**
	 * Sets the field value.
	 *
	 * @param object the object
	 * @param fieldName the field name
	 * @param value the value
	 */
	public static void setFieldValue(Object object, String fieldName, Object value) {
		Field field = getField(object, fieldName);
		setFieldValue(object, field, value);
	}

	/**
	 * Sets the field value.
	 *
	 * @param object the object
	 * @param field the field
	 * @param value the value
	 */
	private static void setFieldValue(Object object, Field field, Object value) {
		// make accessible
		field.setAccessible(true);

		Type type = field.getType();
		Class<?> fieldClass = (Class<?>) type;

		try {
			if (fieldClass.equals(String.class)) {
				field.set(object, value);
			} else if (fieldClass.equals(BigDecimal.class)) {
				field.set(object, new BigDecimal((String) value));
			} else if (fieldClass.equals(Date.class)) {
				field.set(object, convertToDate(value));
			} else if (fieldClass.equals(Integer.class)) {
				field.setInt(object, Integer.valueOf((String) value));
			} else if (fieldClass.equals(Long.class)) {
				field.setLong(object, Long.valueOf((String) value));
			} else if (fieldClass.equals(Double.class)) {
				field.set(object, Double.valueOf((String) value));
			} else {
				field.set(object, value);
			}

		} catch (IllegalArgumentException e) {
			Logger.error(ExceptionUtil.getStackTrace(e));
		} catch (IllegalAccessException e) {
			Logger.error(ExceptionUtil.getStackTrace(e));
		}
	}

	/**
	 * Gets the field.
	 *
	 * @param object the object
	 * @param fieldName the field name
	 * @return the field
	 */
	private static Field getField(final Object object, String fieldName) {

		Object obj = object;

		// make sure object is not null
		if (obj == null) {
			return null;
		}

		Class<?> clazz = obj.getClass();
		try {
			while (clazz != null) {
				for (Field f : clazz.getDeclaredFields()) {
					if (f.getName().equalsIgnoreCase(fieldName)) {
						return f;
					}
				}
				clazz = clazz.getSuperclass();
			}
		} catch (SecurityException e) {
			Logger.error(ExceptionUtil.getStackTrace(e));
		}
		return null;
	}

	/**
	 * Gets the parent class by type.
	 *
	 * @param object the object
	 * @param clazz the clazz
	 * @return the parent class by type
	 */
	public static Class<?> getParentClassByType(Object object, Class<?> clazz) {
		Class<?> clz = object.getClass();
		while (clz != null) {
			if (clz.equals(clazz)) {
				return clz;
			}
			clz = clz.getSuperclass();
		}
		return null;
	}

	/**
	 * Checks if is abstract.
	 *
	 * @param clazz the clazz
	 * @return true, if is abstract
	 */
	public static boolean isAbstract(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		return (modifiers & Modifier.ABSTRACT) > 0;
	}

	/**
	 * Checks if is interface.
	 *
	 * @param clazz the clazz
	 * @return true, if is interface
	 */
	public static boolean isInterface(Class<?> clazz) {
		int modifiers = clazz.getModifiers();
		return (modifiers & Modifier.INTERFACE) > 0;
	}

	/**
	 * Checks if is concrete.
	 *
	 * @param clazz the clazz
	 * @return true, if is concrete
	 */
	public static boolean isConcrete(Class<?> clazz) {
		return !(isInterface(clazz) || isAbstract(clazz));
	}

	/**
	 * Convert to date.
	 *
	 * @param value the value
	 * @return the date
	 */
	private static Date convertToDate(Object value) {
		Date date = null;
		if (value != null && !"".equals(value)) {
			if (value instanceof Long) {
				date = new Date(((Long) value).longValue());

			} else if (value instanceof String) {
				String val = (String) value;
				int dateLength = String.valueOf(Long.MAX_VALUE).length();
				if (dateLength == val.length()) {
					date = new Date(Long.valueOf(val).longValue());
				} else {
					date = getDate(val);
				}
			} else {
				date = (Date) value;
			}
		}
		return date;
	}

	/**
	 * Gets the date.
	 *
	 * @param val the val
	 * @return the date
	 */
	private static Date getDate(String val) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.parse(val);
		} catch (Throwable t) {
			Logger.error(ExceptionUtil.getStackTrace(t), val);
		}
		return null;
	}

	/**
	 * The Class ClassAnnotationHolder.
	 */
	private static class ClassAnnotationHolder {

		/** The main class. */
		private final Class<?> mainClass;

		/** The annotation class. */
		private final Class<?> annotationClass;

		/**
		 * Instantiates a new class annotation holder.
		 *
		 * @param mainClass the main class
		 * @param annotationClass the annotation class
		 */
		public ClassAnnotationHolder(Class<?> mainClass, Class<?> annotationClass) {
			super();
			this.mainClass = mainClass;
			this.annotationClass = annotationClass;
		}

		/**
		 * Gets the main class.
		 *
		 * @return the main class
		 */
		@SuppressWarnings("unused")
		public Class<?> getMainClass() {
			return mainClass;
		}

		/**
		 * Gets the annotation class.
		 *
		 * @return the annotation class
		 */
		@SuppressWarnings("unused")
		public Class<?> getAnnotationClass() {
			return annotationClass;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((annotationClass == null) ? 0 : annotationClass.hashCode());
			result = prime * result + ((mainClass == null) ? 0 : mainClass.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			final ClassAnnotationHolder other = (ClassAnnotationHolder) obj;
			if (annotationClass == null) {
				if (other.annotationClass != null) {
					return false;
				}
			} else if (!annotationClass.equals(other.annotationClass)) {
				return false;
			}
			if (mainClass == null) {
				if (other.mainClass != null) {
					return false;
				}
			} else if (!mainClass.equals(other.mainClass)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * The Class AnnotationFieldsHolder.
	 */
	private static class AnnotationFieldsHolder {

		/** The fields. */
		private final List<Field> fields;

		/** The field names. */
		private List<String> fieldNames;

		/**
		 * Instantiates a new annotation fields holder.
		 *
		 * @param fields the fields
		 */
		public AnnotationFieldsHolder(List<Field> fields) {
			super();
			this.fields = Collections.<Field> unmodifiableList(fields);
			initFieldNames();
		}

		/**
		 * Gets the fields.
		 *
		 * @return the fields
		 */
		@SuppressWarnings("unused")
		public List<Field> getFields() {
			return fields;
		}

		/**
		 * Gets the field names.
		 *
		 * @return the field names
		 */
		public List<String> getFieldNames() {
			return fieldNames;
		}

		/**
		 * Inits the field names.
		 */
		private void initFieldNames() {

			List<String> names = new ArrayList<String>();

			for (Field field : fields) {
				names.add(field.getName());
			}

			// make this an unmodifiableList
			fieldNames = Collections.<String> unmodifiableList(names);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldNames == null) ? 0 : fieldNames.hashCode());
			result = prime * result + ((fields == null) ? 0 : fields.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			final AnnotationFieldsHolder other = (AnnotationFieldsHolder) obj;
			if (fieldNames == null) {
				if (other.fieldNames != null) {
					return false;
				}
			} else if (!fieldNames.equals(other.fieldNames)) {
				return false;
			}
			if (fields == null) {
				if (other.fields != null) {
					return false;
				}
			} else if (!fields.equals(other.fields)) {
				return false;
			}
			return true;
		}
	}

}