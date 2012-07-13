package play.modules.elasticsearch.mapping;

import java.lang.reflect.Field;

/**
 * Factory for retrieving {@link ModelMapper}s and {@link FieldMapper}s
 */
public interface MapperFactory {
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
	<M> ModelMapper<M> getMapper(Class<M> clazz) throws MappingException;

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
	<M> FieldMapper<M> getMapper(Field field) throws MappingException;

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
	<M> FieldMapper<M> getMapper(Field field, String prefix) throws MappingException;
}
