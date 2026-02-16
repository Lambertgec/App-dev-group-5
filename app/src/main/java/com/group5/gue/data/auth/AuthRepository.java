package com.group5.gue.data.auth;

import android.content.Context;

import com.group5.gue.data.Result;
import com.group5.gue.data.model.User;
import com.group5.gue.data.repository.UserRepository;

public class AuthRepository {

    private static volatile AuthRepository instance;
    private final AuthDataSource dataSource;
    private final UserRepository userRepository;
    private User cachedUser; //this should persist throughout the app

    private AuthRepository(Context context) {
        this.dataSource = new AuthDataSource(context.getApplicationContext());
        this.userRepository = new UserRepository();
    }

    public static AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    public User getCachedUser() {
        return cachedUser;
    }

    public void resolveSession(UserIdCallback callback) {
        dataSource.getCachedUserId(userId -> {
            if (userId != null) {
                userRepository.getUserById(userId, result -> {
                    if (result instanceof Result.Success) {
                        cachedUser = ((Result.Success<User>) result).getData();
                    }
                    if (callback != null) {
                        callback.onResult(userId);
                    }
                });
                return;
            } else {
                cachedUser = null;
            }
            if (callback != null) {
                callback.onResult(userId);
            }
        });
    }


    public void logout(AuthCallback callback) {
        dataSource.logout(callback);
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        dataSource.signInWithEmail(email, password, callback);
    }

    public void signUpWithEmail(String email, String password, AuthCallback callback) {
        dataSource.signUpWithEmail(email, password, callback);
    }

    public void signInWithGoogle(AuthCallback callback) {
        dataSource.signInWithGoogle(callback);
    }
}
