package com.group5.gue.data.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.group5.gue.data.Result;
import com.group5.gue.data.model.Role;
import com.group5.gue.data.model.User;

import java.util.List;

public class AuthRepository {

    private static volatile AuthRepository instance;
    private final AuthDataSource dataSource;
    private User cachedUser;

    private AuthRepository(Context context) {
        this.dataSource = new AuthDataSource(context.getApplicationContext());
        String userId = this.dataSource.getCachedUserId();
        if (userId != null) {
            this.cachedUser = new User(userId, Role.USER); // make api call to get user profile
        }
    }

    public static AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        if (cachedUser != null) {
            return true;
        }
        String userId = dataSource.getCachedUserId();
        if (userId != null) {
            cachedUser = new User(userId, Role.USER);
            return true;
        }
        return false;
    }

    public User getCachedUser() {
        return cachedUser;
    }

    public void resolveSession(UserIdCallback callback) {
        dataSource.getCachedUserIdAsync(userId -> {
            if (userId != null) {
                cachedUser = new User(userId, Role.USER);
            } else {
                cachedUser = null;
            }
            if (callback != null) {
                callback.onResult(userId);
            }
        });
    }

    public void logout(AuthCallback callback) {
        dataSource.logout(wrapLogoutCallback(callback));
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        dataSource.signInWithEmail(email, password, wrapCallback(callback));
    }

    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        dataSource.signUpWithEmail(email, password, wrapCallback(callback));
    }

    public void signInWithGoogle(AuthCallback callback) {
        dataSource.signInWithGoogle(wrapCallback(callback));
    }

    private AuthCallback wrapCallback(AuthCallback callback) {
        return result -> {
            if (result instanceof Result.Success) {
                cachedUser = ((Result.Success<User>) result).getData();
            } else if (result instanceof Result.Error) {
                cachedUser = null;
            }
            if (callback != null) {
                callback.onResult(result);
            }
        };
    }

    private AuthCallback wrapLogoutCallback(AuthCallback callback) {
        return result -> {
            cachedUser = null;
            if (callback != null) {
                callback.onResult(result);
            }
        };
    }

    public void fetchFriends(java.util.function.Consumer<List<String>> callback) {
        dataSource.getFollowedUsers(list -> {
            callback.accept(list);
            return null;
        });
    }
}
