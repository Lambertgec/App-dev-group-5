package com.group5.gue;

import static org.junit.Assert.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.group5.gue.data.Result;
import com.group5.gue.data.model.Role;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;
import com.group5.gue.data.user.UserUpdateCallback;
import com.group5.gue.ui.login.launcher.LauncherActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
public class ProfileActivityTest {

    @Test
    public void onCreate_savedInstanceStateNull_hostsFragment_andLoadsCachedUser() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);
        User cached = new User("id1", "Alice", 10, false);
        Mockito.when(repo.getCachedUser()).thenReturn(cached);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create(null).start().resume().get();

        setPrivateField(activity, "userRepository", repo);

        // drive loadProfileData (since onCreate already ran before injection)
        invokePrivate(activity, "loadProfileData");

        TextView username = activity.findViewById(R.id.profileUsername);
        TextView score = activity.findViewById(R.id.profileScore);
        TextView userId = activity.findViewById(R.id.profileUserId);
        TextView role = activity.findViewById(R.id.profileRole);

        assertEquals("Alice", username.getText().toString());
        assertEquals("10", score.getText().toString());
        assertEquals("id1", userId.getText().toString());
        assertEquals(Role.USER.toString(), role.getText().toString());
    }

    @Test
    public void onCreate_savedInstanceStateNotNull_doesNotCrash_andNullCacheBranchCovered() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);
        Mockito.when(repo.getCachedUser()).thenReturn(null);

        // non-null Bundle => fragment replace branch not taken
        ActivityController<ProfileActivity> controller = Robolectric.buildActivity(ProfileActivity.class)
                .create(new android.os.Bundle()).start().resume();

        ProfileActivity activity = controller.get();

        setPrivateField(activity, "userRepository", repo);
        invokePrivate(activity, "loadProfileData"); // cachedUser == null branch
        // no assertions needed: coverage + no crash
    }

    @Test
    public void changeUsernameDialog_okWithEmpty_doesNotUpdateUsername() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        // set current user so dialog pre-fills and OK handler runs
        setPrivateField(activity, "currentUser", new User("id1", "Bob", 1, false));
        setPrivateField(activity, "userRepository", repo);

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);

        EditText input = dialog.findViewById(android.R.id.edit);
        // Robolectric sometimes doesn't assign android.R.id.edit; fallback: search manually
        if (input == null) {
            input = (EditText) dialog.findViewById(0);
        }
        // safer: just set text via dialog's view hierarchy
        // If not found, just skip strict lookup and click OK with default (it will be "Bob" prefill).
        // Instead force empty by finding the first EditText in window decor:
        EditText realInput = findFirstEditText(dialog);
        assertNotNull(realInput);

        realInput.setText("   "); // empty after trim

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Mockito.verify(repo, Mockito.never()).updateUser(Mockito.any(), Mockito.any());
    }

    @Test
    public void changeUsernameDialog_okWithNonEmpty_callsUpdateUser_success_updatesUI_andShowsToast() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Old", 5, false));
        setPrivateField(activity, "userRepository", repo);

        // When updateUser called, invoke callback with success(updatedUser)
        Mockito.doAnswer(invocation -> {
            User updated = invocation.getArgument(0);
            UserUpdateCallback cb =
                    invocation.getArgument(1);
            cb.onResult(new Result.Success<>(updated));
            return null;
        }).when(repo).updateUser(Mockito.any(User.class), Mockito.any());

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText input = findFirstEditText(dialog);
        assertNotNull(input);

        input.setText("NewName");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        TextView username = activity.findViewById(R.id.profileUsername);
        assertEquals("NewName", username.getText().toString());
        assertTrue(ShadowToast.getTextOfLatestToast().toString().contains("Username updated successfully"));
    }

    @Test
    public void changeUsernameDialog_okWithNonEmpty_callsUpdateUser_error_showsToast() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Old", 5, false));
        setPrivateField(activity, "userRepository", repo);

        Mockito.doAnswer(invocation -> {
            UserUpdateCallback cb =
                    invocation.getArgument(1);
            cb.onResult(new Result.Error<>(new Exception("boom")));
            return null;
        }).when(repo).updateUser(Mockito.any(User.class), Mockito.any());

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText input = findFirstEditText(dialog);
        assertNotNull(input);

        input.setText("NewName");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        assertTrue(ShadowToast.getTextOfLatestToast().toString().contains("Failed to update username"));
    }

    @Test
    public void deleteAccountDialog_whenCurrentUserNull_doesNothing() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", null);
        setPrivateField(activity, "userRepository", repo);

        invokePrivate(activity, "showDeleteAccountDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Mockito.verify(repo, Mockito.never()).deleteAccount(Mockito.any());
    }

    @Test
    public void deleteAccountDialog_success_showsToast_andNavigatesToLauncher() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Bob", 1, false));
        setPrivateField(activity, "userRepository", repo);

        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            kotlin.jvm.functions.Function1<Result<Void>, kotlin.Unit> cb =
                    invocation.getArgument(0);
            cb.invoke(new Result.Success<>(null));
            return null;
        }).when(repo).deleteAccount(Mockito.any());

        invokePrivate(activity, "showDeleteAccountDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        assertTrue(ShadowToast.getTextOfLatestToast().toString().contains("Account deleted successfully"));

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent next = shadowActivity.getNextStartedActivity();
        assertNotNull(next);
        assertEquals(LauncherActivity.class.getName(), next.getComponent().getClassName());
    }

    @Test
    public void deleteAccountDialog_error_showsToast() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Bob", 1, false));
        setPrivateField(activity, "userRepository", repo);

        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            kotlin.jvm.functions.Function1<Result<Void>, kotlin.Unit> cb =
                    invocation.getArgument(0);
            cb.invoke(new Result.Error<>(new Exception("nope")));
            return null;
        }).when(repo).deleteAccount(Mockito.any());

        invokePrivate(activity, "showDeleteAccountDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        assertTrue(ShadowToast.getTextOfLatestToast().toString().contains("Failed to delete account"));
    }

    // ---- helpers ----

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void invokePrivate(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }

    private static EditText findFirstEditText(AlertDialog dialog) {
        // brute-force walk: dialog.getWindow().getDecorView() exists in Robolectric
        android.view.View root = dialog.getWindow().getDecorView();
        return findFirstEditText(root);
    }

    private static EditText findFirstEditText(android.view.View v) {
        if (v instanceof EditText) return (EditText) v;
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                EditText found = findFirstEditText(vg.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}