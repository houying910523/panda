package com.mv.data.panda.common.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mv.data.panda.common.UnLoggedInException;
import com.mv.data.panda.model.User;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * author: houying
 * date  : 17-3-1
 * desc  :
 */
@Service
public class UserCache {

    private Cache<String, User> cache;

    public UserCache() {
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(7, TimeUnit.DAYS)
                .build();
    }

    public boolean isLogin(String username) {
        return cache.getIfPresent(username) != null;
    }

    public User getUser(String username) {
        return cache.getIfPresent(username);
    }

    public User getLoginUser(String username) throws UnLoggedInException {
        User user = cache.getIfPresent(username);
        if (user == null) {
            throw new UnLoggedInException();
        }
        return user;
    }

    public void validate(String username) {
        User user = cache.getIfPresent(username);
        if (user == null) {
            throw new UnLoggedInException();
        }
    }

    public void add(User user) {
        cache.put(user.getUsername(), user);
    }

    public void remove(String username) {
        cache.invalidate(username);
    }
}
