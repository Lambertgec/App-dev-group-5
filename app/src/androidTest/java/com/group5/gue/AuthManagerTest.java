package com.group5.gue.data.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.group5.gue.data.Result;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Unit;

@RunWith(AndroidJUnit4.class)
public class AuthManagerTest {

    private static final long AUTH_TIMEOUT_SECONDS = 60;
    private static final String PREFS_NAME = "auth_manager_tests";
    private static final String PENDING_ACCOUNTS_KEY = "pending_accounts";
    private static final String ACCOUNT_DELIMITER = "::";

    private Context appContext;
    private AuthManager authManager;
    private UserRepository userRepository;

    @Before
    public void setUp() throws InterruptedException {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        authManager = AuthManager.Companion.getInstance(appContext);
        userRepository = UserRepository.Companion.getInstance();
        forceLogout();
        cleanupPendingAccountsFromPreviousRuns();
        forceLogout();
    }

    @After
    public void tearDown() throws InterruptedException {
        forceLogout();
    }

    @Test
    public void signInWithEmail_invalidCredentials_returnsErrorResult() throws InterruptedException {
        Result<User> result = awaitSignIn("not-an-account@example.com", "wrong-password");

        assertTrue("Expected sign-in failure for invalid credentials", result instanceof Result.Error);
    }

    @Test
    public void signInWithEmail_correctEmailWrongPassword_returnsErrorResult() throws InterruptedException {
        Credential credential = newCredential("auth-wrong-pass");

        Result<User> signUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected sign-up success", signUp instanceof Result.Success);

        Result<Void> logout = awaitLogout();
        assertTrue("Expected logout success", logout instanceof Result.Success);

        Result<User> wrongPasswordResult = awaitSignIn(credential.email, credential.password + "-bad");
        assertTrue("Expected sign-in failure for wrong password", wrongPasswordResult instanceof Result.Error);
        Exception error = ((Result.Error<User>) wrongPasswordResult).getError();
        assertNotNull(error);
        assertNotNull(error.getMessage());

        Result<User> signInAgain = awaitSignIn(credential.email, credential.password);
        if (signInAgain instanceof Result.Success) {
            Result<Void> deleteResult = awaitDeleteAccount();
            assertTrue("Expected cleanup delete-account success", deleteResult instanceof Result.Success);
        }
        removePendingCredential(credential);
    }

    @Test
    public void signUpWithEmail_tooShortPassword_returnsErrorResult() throws InterruptedException {
        String email = "short-pass-" + System.currentTimeMillis() + "@example.com";
        Result<User> result = awaitSignUp(email, "123");

        assertTrue("Expected sign-up failure for too short password", result instanceof Result.Error);
        Exception error = ((Result.Error<User>) result).getError();
        assertNotNull(error);
        assertNotNull(error.getMessage());
    }

    @Test
    public void signUpWithEmail_duplicateEmail_secondSignupFails() throws InterruptedException {
        Credential credential = newCredential("auth-duplicate");

        Result<User> firstSignUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected first sign-up success", firstSignUp instanceof Result.Success);

        Result<User> secondSignUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected second sign-up to fail for duplicate email", secondSignUp instanceof Result.Error);

        Result<Void> deleteResult = awaitDeleteAccount();
        assertTrue("Expected delete-account success during cleanup", deleteResult instanceof Result.Success);
        removePendingCredential(credential);
    }

    @Test
    public void signUpWithEmail_passwordBoundary_lengthFiveFailsLengthSixSucceeds() throws InterruptedException {
        String suffix = String.format(Locale.US, "%d", System.currentTimeMillis());
        String emailFail = "pw-boundary-fail-" + suffix + "@example.com";
        Credential successCredential = new Credential("pw-boundary-ok-" + suffix + "@example.com", "Ab#123");
        registerPendingCredential(successCredential);

        Result<User> lengthFive = awaitSignUp(emailFail, "Ab#12");
        assertTrue("Expected 5-char password sign-up failure", lengthFive instanceof Result.Error);

        Result<User> lengthSix = awaitSignUp(successCredential.email, successCredential.password);
        assertTrue("Expected 6-char password sign-up success", lengthSix instanceof Result.Success);

        Result<Void> deleteResult = awaitDeleteAccount();
        assertTrue("Expected delete-account success for boundary test cleanup", deleteResult instanceof Result.Success);
        removePendingCredential(successCredential);
    }

    @Test
    public void signUpWithEmail_invalidEmailFormat_returnsErrorResult() throws InterruptedException {
        Result<User> result = awaitSignUp("not-an-email", "ValidPass#123");

        assertTrue("Expected sign-up failure for invalid email format", result instanceof Result.Error);
        Exception error = ((Result.Error<User>) result).getError();
        assertNotNull(error);
        assertNotNull(error.getMessage());
    }

    @Test
    public void signInWithEmail_sqlInjectionLikePayload_returnsErrorResult() throws InterruptedException {
        Result<User> result = awaitSignIn("' OR 1=1 --", "' OR '1'='1");

        assertTrue("Expected sign-in failure for SQL injection-like payload", result instanceof Result.Error);
        Exception error = ((Result.Error<User>) result).getError();
        assertNotNull(error);
        assertNotNull(error.getMessage());
    }

    @Test
    public void getUserFromSession_afterLogout_returnsNoAuthenticatedUserError() throws InterruptedException {
        Result<Void> logoutResult = awaitLogout();
        assertTrue("Expected logout success", logoutResult instanceof Result.Success);

        Result<User> result = awaitGetUserFromSession();

        assertTrue("Expected getUserFromSession to fail when logged out", result instanceof Result.Error);
        Exception error = ((Result.Error<User>) result).getError();
        assertNotNull(error);
        assertTrue(error.getMessage() != null && error.getMessage().contains("No authenticated user"));
    }

    @Test
    public void signUpThenSignInAndSessionLookup_returnsConsistentUserIdentity() throws InterruptedException {
        Credential credential = newCredential("auth-flow");

        Result<User> signUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected sign-up success", signUp instanceof Result.Success);
        User created = ((Result.Success<User>) signUp).getData();
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(credential.email.substring(0, credential.email.indexOf('@')), created.getName());

        Result<Void> logoutAfterSignUp = awaitLogout();
        assertTrue("Expected logout success", logoutAfterSignUp instanceof Result.Success);

        Result<User> signIn = awaitSignIn(credential.email, credential.password);
        assertTrue("Expected sign-in success", signIn instanceof Result.Success);
        User signedIn = ((Result.Success<User>) signIn).getData();
        assertNotNull(signedIn);
        assertEquals(created.getId(), signedIn.getId());

        Result<User> fromSession = awaitGetUserFromSession();
        assertTrue("Expected session lookup success", fromSession instanceof Result.Success);
        User sessionUser = ((Result.Success<User>) fromSession).getData();
        assertNotNull(sessionUser);
        assertEquals(created.getId(), sessionUser.getId());
        assertEquals(signedIn.getName(), sessionUser.getName());

        Result<Void> deleteResult = awaitDeleteAccount();
        assertTrue("Expected delete-account success after flow assertion", deleteResult instanceof Result.Success);
        removePendingCredential(credential);
    }

    @Test
    public void createAccountThenDeleteAccount_signInAfterDeleteFails() throws InterruptedException {
        Credential credential = newCredential("auth-delete");

        Result<User> signUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected sign-up success", signUp instanceof Result.Success);

        Result<Void> deleteResult = awaitDeleteAccount();
        assertTrue("Expected delete-account success", deleteResult instanceof Result.Success);
        removePendingCredential(credential);

        Result<User> signInAfterDelete = awaitSignIn(credential.email, credential.password);
        assertTrue("Expected sign-in failure after delete-account", signInAfterDelete instanceof Result.Error);
    }

    @Test
    public void logout_isIdempotent_whenCalledTwice() throws InterruptedException {
        Credential credential = newCredential("auth-logout-idempotent");

        Result<User> signUp = awaitSignUp(credential.email, credential.password);
        assertTrue("Expected sign-up success", signUp instanceof Result.Success);

        Result<Void> firstLogout = awaitLogout();
        assertTrue("Expected first logout success", firstLogout instanceof Result.Success);

        Result<Void> secondLogout = awaitLogout();
        assertTrue("Expected second logout success", secondLogout instanceof Result.Success);

        Result<User> fromSession = awaitGetUserFromSession();
        assertTrue("Expected no authenticated user after repeated logout", fromSession instanceof Result.Error);

        Result<User> signInAgain = awaitSignIn(credential.email, credential.password);
        if (signInAgain instanceof Result.Success) {
            Result<Void> deleteResult = awaitDeleteAccount();
            assertTrue("Expected delete-account success after logout idempotency test", deleteResult instanceof Result.Success);
        }
        removePendingCredential(credential);
    }

    @Test
    public void signInWithGoogle_returnsAuthenticatedUser_whenGoogleCredentialIsAvailable() throws InterruptedException {
        Result<User> googleResult = awaitGoogleSignIn();

        if (googleResult instanceof Result.Error) {
            Exception error = ((Result.Error<User>) googleResult).getError();
            String message = error != null ? error.getMessage() : "Unknown Google sign-in error";
            Assume.assumeTrue("Skipping: Google sign-in credential unavailable on this device. " + message, false);
        }

        User user = ((Result.Success<User>) googleResult).getData();
        assertNotNull("Expected Google sign-in user", user);
        assertNotNull("Expected non-null user id after Google sign-in", user.getId());

        Result<User> fromSession = awaitGetUserFromSession();
        assertTrue("Expected session lookup success after Google sign-in", fromSession instanceof Result.Success);
        User sessionUser = ((Result.Success<User>) fromSession).getData();
        assertNotNull(sessionUser);
        assertEquals("Expected session user id to match Google sign-in user id", user.getId(), sessionUser.getId());
    }

    @Test
    public void googleFirstLogin_createsOrLoadsUserProfile_withStableIdentity() throws InterruptedException {
        Result<User> firstGoogle = awaitGoogleSignIn();
        if (firstGoogle instanceof Result.Error) {
            Exception error = ((Result.Error<User>) firstGoogle).getError();
            String message = error != null ? error.getMessage() : "Unknown Google sign-in error";
            Assume.assumeTrue("Skipping: Google sign-in credential unavailable on this device. " + message, false);
        }

        User firstUser = ((Result.Success<User>) firstGoogle).getData();
        assertNotNull(firstUser);
        assertNotNull(firstUser.getId());

        Result<Void> logout = awaitLogout();
        assertTrue("Expected logout success after first Google login", logout instanceof Result.Success);

        Result<User> secondGoogle = awaitGoogleSignIn();
        assertTrue("Expected second Google login success", secondGoogle instanceof Result.Success);
        User secondUser = ((Result.Success<User>) secondGoogle).getData();
        assertNotNull(secondUser);
        assertEquals("Expected Google identity to remain stable across logins", firstUser.getId(), secondUser.getId());
        assertNotNull("Expected profile name for Google user", secondUser.getName());
    }

    private Result<User> awaitSignUp(String email, String password) throws InterruptedException {
        AtomicReference<Result<User>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        authManager.signUpWithEmail(email, password, result -> {
            ref.set(result);
            latch.countDown();
        });

        assertTrue("Timed out waiting for sign-up callback", latch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        return ref.get();
    }

    private Result<User> awaitSignIn(String email, String password) throws InterruptedException {
        AtomicReference<Result<User>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        authManager.signInWithEmail(email, password, result -> {
            ref.set(result);
            latch.countDown();
        });

        assertTrue("Timed out waiting for sign-in callback", latch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        return ref.get();
    }

    private Result<User> awaitGetUserFromSession() throws InterruptedException {
        AtomicReference<Result<User>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        authManager.getUserFromSession(result -> {
            ref.set(result);
            latch.countDown();
        });

        assertTrue("Timed out waiting for getUserFromSession callback", latch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        return ref.get();
    }

    private Result<User> awaitGoogleSignIn() throws InterruptedException {
        AtomicReference<Result<User>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        authManager.signInWithGoogle(result -> {
            ref.set(result);
            latch.countDown();
        });

        assertTrue("Timed out waiting for Google sign-in callback", latch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        return ref.get();
    }

    private Result<Void> awaitLogout() throws InterruptedException {
        AtomicReference<Result<Void>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        authManager.logout(result -> {
            ref.set(result);
            latch.countDown();
            return Unit.INSTANCE;
        });

        assertTrue("Timed out waiting for logout callback", latch.await(30, TimeUnit.SECONDS));
        return ref.get();
    }

    private Result<Void> awaitDeleteAccount() throws InterruptedException {
        AtomicReference<Result<Void>> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        userRepository.deleteAccount(result -> {
            ref.set(result);
            latch.countDown();
            return Unit.INSTANCE;
        });

        assertTrue("Timed out waiting for delete-account callback", latch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS));
        return ref.get();
    }

    private void forceLogout() throws InterruptedException {
        Result<Void> result = awaitLogout();
        assertTrue("Expected cleanup logout success", result instanceof Result.Success);
    }

    private Credential newCredential(String prefix) {
        String suffix = String.format(Locale.US, "%d", System.currentTimeMillis());
        String email = prefix + "-" + suffix + "@example.com";
        String password = "Pw#" + suffix + "Aa";
        Credential credential = new Credential(email, password);
        registerPendingCredential(credential);
        return credential;
    }

    private void cleanupPendingAccountsFromPreviousRuns() throws InterruptedException {
        List<Credential> pendingCredentials = getPendingCredentials();
        for (Credential pending : pendingCredentials) {
            Result<User> signInResult = awaitSignIn(pending.email, pending.password);
            if (signInResult instanceof Result.Success) {
                Result<Void> deleteResult = awaitDeleteAccount();
                if (deleteResult instanceof Result.Success) {
                    removePendingCredential(pending);
                }
            } else {
                // Remove entries that can no longer be signed into to avoid endless retries.
                removePendingCredential(pending);
            }

            forceLogout();
        }
    }

    private void registerPendingCredential(Credential credential) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> current = new HashSet<>(prefs.getStringSet(PENDING_ACCOUNTS_KEY, new HashSet<>()));
        current.add(encodeCredential(credential));
        prefs.edit().putStringSet(PENDING_ACCOUNTS_KEY, current).apply();
    }

    private void removePendingCredential(Credential credential) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> current = new HashSet<>(prefs.getStringSet(PENDING_ACCOUNTS_KEY, new HashSet<>()));
        current.remove(encodeCredential(credential));
        prefs.edit().putStringSet(PENDING_ACCOUNTS_KEY, current).apply();
    }

    private List<Credential> getPendingCredentials() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> encoded = new HashSet<>(prefs.getStringSet(PENDING_ACCOUNTS_KEY, new HashSet<>()));
        List<Credential> credentials = new ArrayList<>();
        for (String row : encoded) {
            Credential credential = decodeCredential(row);
            if (credential != null) {
                credentials.add(credential);
            }
        }
        return credentials;
    }

    private String encodeCredential(Credential credential) {
        return credential.email + ACCOUNT_DELIMITER + credential.password;
    }

    private Credential decodeCredential(String value) {
        String[] parts = value.split(ACCOUNT_DELIMITER, 2);
        if (parts.length != 2) {
            return null;
        }
        return new Credential(parts[0], parts[1]);
    }

    private static final class Credential {
        final String email;
        final String password;

        Credential(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
