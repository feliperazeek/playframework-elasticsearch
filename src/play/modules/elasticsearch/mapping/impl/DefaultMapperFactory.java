package play.modules.elasticsearch.mapping.impl;

import java.lang.reflect.Field;
import java.util.Collection;

import play.modules.elasticsearch.annotations.ElasticSearchEmbedded;
import play.modules.elasticsearch.mapping.FieldMapper;
import play.modules.elasticsearch.mapping.MapperFactory;
import play.modules.elasticsearch.mapping.MappingException;
import play.modules.elasticsearch.mapping.MappingUtil;
import play.modules.elasticsearch.mapping.ModelMapper;

/**
 * Factory for {@link ModelMapper}s
 */
public class DefaultMapperFactory implements MapperFactory {

	/**
	 * Gets a {@link ModelMapper} for the specified model class
	 * 
	 * @param <M>
	 *            the model type
	 * @param clazz
	 *            the model class
	 * @throws MappingException
	 *             in case of mapping problems
	 * @return the model mapper
	 */
	@SuppressWarnings("unchecked")
	public <M> ModelMapper<M> getMapper(Class<M> clazz) throws MappingException {
		if (clazz.equals(play.db.Model.class)) {
			return (ModelMapper<M>) new UniversalModelMapper();
		}
		
		if (!MappingUtil.isSearchable(clazz)) {
			throw new MappingException("Class must be annotated with @ElasticSearchable");
		}

		if (play.db.Model.class.isAssignableFrom(clazz)) {
			return (ModelMapper<M>) new PlayModelMapper<play.db.Model>(this,
					(Class<play.db.Model>) clazz);
		} else {
			throw new MappingException(
					"No mapper available for non-play.db.Model models at this time");
		}
	}

	/**
	 * Gets a {@link FieldMapper} for the specified field
	 * 
	 * @param <M>
	 *            the model type
	 * @param field
	 *            the field
	 * @throws MappingException
	 *             in case of mapping problems
	 * @return the field mapper
	 */
	public <M> FieldMapper<M> getMapper(Field field) throws MappingException {

		return getMapper(field, null);

	}

	/**
	 * Gets a {@link FieldMapper} for the specified field, using a prefix in the
	 * index
	 * 
	 * @param <M>
	 *            the model type
	 * @param field
	 *            the field
	 * @throws MappingException
	 *             in case of mapping problems
	 * @return the field mapper
	 */
	public <M> FieldMapper<M> getMapper(Field field, String prefix) throws MappingException {

		if (Collection.class.isAssignableFrom(field.getType())) {
			return new CollectionFieldMapper<M>(this, field, prefix);

		} else if (field.isAnnotationPresent(ElasticSearchEmbedded.class)) {
			return new EmbeddedFieldMapper<M>(this, field, prefix);

		} else {
			return new SimpleFieldMapper<M>(field, prefix);

		}

	}

}
