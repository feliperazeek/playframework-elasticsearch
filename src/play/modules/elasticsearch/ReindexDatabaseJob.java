package play.modules.elasticsearch;

import java.util.Set;

import javax.persistence.metamodel.ManagedType;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Job;

public class ReindexDatabaseJob extends Job<Void> {

	@Override
	public void doJob() throws Exception {
		final Set<ManagedType<?>> types = JPA.em().getMetamodel().getManagedTypes();
		for (final ManagedType managedType : types) {
			final Class javaClass = managedType.getJavaType();
			Logger.info(javaClass.toString());
		}
	}

}
