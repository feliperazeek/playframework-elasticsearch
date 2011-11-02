package play.modules.elasticsearch.mapping;

/**
 * Mapping exception
 */
public class MappingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MappingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MappingException(String arg0) {
		super(arg0);
	}

}
