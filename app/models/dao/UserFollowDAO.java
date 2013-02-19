package models.dao;

import java.util.Collection;
import java.util.concurrent.Callable;

import javax.inject.Singleton;

import models.User;
import models.UserFollow;
import models.UserFollowPK;
import play.db.ebean.Model.Finder;
import play.utils.cache.InterimCache;
import play.utils.dao.CachedDAO;
import play.utils.dao.TimestampListener;

import com.avaje.ebean.Page;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@Singleton
public class UserFollowDAO extends CachedDAO<UserFollowPK, UserFollow> {

	public static final int PAGE_SIZE = 20;
	
	protected InterimCache<Integer> followerCountCache = new InterimCache<Integer>("UserFollowerCountCache", 86400);//24 hrs
	protected InterimCache<Integer> followingCountCache = new InterimCache<Integer>("UserFollowingCountCache", 86400);//24 hrs
	protected InterimCache<Page<User>> followerPageCache = new InterimCache<Page<User>>("UserFollowerPageCache", 86400);//24 hrs
	protected InterimCache<Page<User>> followingPageCache = new InterimCache<Page<User>>("UserFollowingPageCache", 86400);//24 hrs
	
	protected Multimap<String, String> userPages = HashMultimap.create();

	protected Finder<UserFollowPK, UserFollow> find = new Finder<UserFollowPK, UserFollow>(
			UserFollowPK.class, UserFollow.class);

	private UserDAO userDAO;

	public UserFollowDAO(UserDAO userDAO) {
		super(UserFollowPK.class, UserFollow.class);
		this.userDAO = userDAO;
		addListener(new TimestampListener<UserFollowPK, UserFollow>());
		addListener(new UserFollowDAOFollowCacheCleaner(this));
	}

	public UserFollow get(String sourceKey, String targetKey) {
		UserFollowPK key = new UserFollowPK(sourceKey, targetKey);
		return find().byId(key);
	}

	public Integer getFollowerCount(final User u) {
		String key = u.getKey();
		final Integer count = followerCountCache.get("." + key, new Callable<Integer>() {
			public Integer call() throws Exception {
				return find.where().eq("target_key", u.getKey()).findRowCount();
			}
		});
		return count;
	}

	public Integer getFollowingCount(final User u) {
		String key = u.getKey();
		final Integer count = followingCountCache.get("." + key, new Callable<Integer>() {
			public Integer call() throws Exception {
				return find.where().eq("source_key", u.getKey()).findRowCount();
			}
		});
		return count;
	}
	
	public Page<User> getFollowerUsers(final User u, final int page) {
		final String key = u.getKey();
		final String cacheKey = "." + key + ".pg." + page;
		final Page<User> count = followerPageCache.get(cacheKey, new Callable<Page<User>>() {
			public Page<User> call() throws Exception {
				userPages.put(key, cacheKey);
				Page<UserFollow> followPage = page(page, PAGE_SIZE, "created_on desc", "target_key", u);
				PageAdapter<UserFollow, User> userPage = new UserFollowerPageAdapter(followPage, userDAO);
				return userPage;
			}
		});
		return count;
	}
	
	public Page<User> getFollowingUsers(final User u, final int page) {
		final String key = u.getKey();
		final String cacheKey = "." + key + ".pg." + page;
		final Page<User> count = followingPageCache.get(cacheKey, new Callable<Page<User>>() {
			public Page<User> call() throws Exception {
				userPages.put(key, cacheKey);
				Page<UserFollow> followPage = page(page, PAGE_SIZE, "created_on desc", "source_key", u);
				PageAdapter<UserFollow, User> userPage = new UserFollowingPageAdapter(followPage, userDAO);
				return userPage;
			}
		});
		return count;
	}
	
	public void cleanCache(User u) {
		cleanCache(u.getKey());
	}
	
	public void cleanCache(String key) {
		followerCountCache.set("." + key, null); 
		followingCountCache.set("." + key, null);
		Collection<String> pageKeys = userPages.get(key);
		for (String pageKey : pageKeys) {
			followerPageCache.set(pageKey, null);
			followingPageCache.set(pageKey, null);
		}
		userPages.removeAll(key);
	}
}