package models.dao;

import java.util.concurrent.Callable;

import javax.inject.Singleton;

import models.SourceConfiguration;
import play.Logger;
import play.Logger.ALogger;
import play.cache.Cache;
import play.utils.cache.CachedFinder;
import play.utils.dao.CachedDAO;
import play.utils.dao.EntityNotFoundException;
import play.utils.dao.TimestampListener;

import com.pickleproject.shopping.ConfigurationDAO;

@Singleton
public class SourceConfigurationDAO extends CachedDAO<Long, SourceConfiguration> implements ConfigurationDAO<Long, SourceConfiguration> {
	
	private static ALogger log = Logger.of(SourceConfigurationDAO.class);

	/** expire in 7*24 hours */
	private static final int CACHE_EXPIRATION = 7 * 24 * 3600;

	public SourceConfigurationDAO() {
		super(Long.class, SourceConfiguration.class);
		addListener(new TimestampListener<Long, SourceConfiguration>());
	}

	public SourceConfiguration getWithSourceKey(final String sourceKey) {
		String cacheKey = "SourceConfigurationDAO.bySourceKey." + sourceKey;
		try {
			return Cache.getOrElse(cacheKey, new Callable<SourceConfiguration>() {
				public SourceConfiguration call() throws Exception {
					return find().where().eq("sourceKey", sourceKey).findUnique();
				}
			}, CACHE_EXPIRATION);
		} catch (Exception e) {
			log.error("exception occured while retrieving from cache", e);
			return null;
		}
	}

	@Override
	public Long create(SourceConfiguration m) {
		Long k = super.create(m);
		String sourceKey = m.getSourceKey();
		String cacheKey = "SourceConfigurationDAO.bySourceKey." + sourceKey;
		Cache.set(cacheKey, m);
		return k;
	}

	@Override
	public void remove(Long key) throws EntityNotFoundException {
		SourceConfiguration ref = find().ref(key);
		if (ref == null) throw new EntityNotFoundException(key);
		String sourceKey = ref.getSourceKey();
		String cacheKey = "SourceConfigurationDAO.bySourceKey." + sourceKey;
		Cache.set(cacheKey, null);
		ref.delete();
		CachedFinder<Long, SourceConfiguration> find = (CachedFinder<Long, SourceConfiguration>) find();
		find.clean(key);
	}

	@Override
	public void update(SourceConfiguration m) {
		super.update(m);
		String sourceKey = m.getSourceKey();
		String cacheKey = "SourceConfigurationDAO.bySourceKey." + sourceKey;
		Cache.set(cacheKey, m);
	}

}
