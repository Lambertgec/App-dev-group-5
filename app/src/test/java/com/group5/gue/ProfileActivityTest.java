package com.group5.gue;

import static org.junit.Assert.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

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
        ActivityController<ProfileActivity> controller = Robolectric.buildActivity(ProfileActivity.class)
                .create(new android.os.Bundle()).start().resume();

        ProfileActivity activity = controller.get();

        setPrivateField(activity, "userRepository", repo);
        invokePrivate(activity, "loadProfileData");
    }

    @Test
    public void onCreate_withActionBarTheme_setsHomeAsUpEnabled() {
        ActivityController<ProfileActivity> controller = Robolectric.buildActivity(ProfileActivity.class);
        ProfileActivity activity = controller.get();

        activity.setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light);
        controller.create(null).start().resume();

        assertNotNull(activity.getSupportActionBar());
        int displayOptions = activity.getSupportActionBar().getDisplayOptions();
        assertTrue((displayOptions & androidx.appcompat.app.ActionBar.DISPLAY_HOME_AS_UP) != 0);
    }

    @Test
    public void changeUsernameDialog_okWithEmpty_doesNotUpdateUsername() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();
        setPrivateField(activity, "currentUser", new User("id1", "Bob", 1, false));
        setPrivateField(activity, "userRepository", repo);

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);

        EditText input = dialog.findViewById(android.R.id.edit);
        if (input == null) {
            input = (EditText) dialog.findViewById(0);
        }
        EditText realInput = findFirstEditText(dialog);
        assertNotNull(realInput);

        realInput.setText("   ");

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Mockito.verify(repo, Mockito.never()).updateUser(Mockito.any(), Mockito.any());
    }

    @Test
    public void changeUsernameDialog_whenCurrentUserNull_nonEmptyInput_doesNotUpdate() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", null);
        setPrivateField(activity, "userRepository", repo);

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);

        EditText input = findFirstEditText(dialog);
        assertNotNull(input);
        assertEquals("", input.getText().toString());

        input.setText("NameShouldNotApply");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Mockito.verify(repo, Mockito.never()).updateUser(Mockito.any(User.class), Mockito.any());
    }

    @Test
    public void changeUsernameDialog_okWithNonEmpty_callsUpdateUser_success_updatesUI_andShowsToast() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Old", 5, false));
        setPrivateField(activity, "userRepository", repo);
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
    public void changeUsernameDialog_okWithNonEmpty_whenUpdateReturnsNullResult_noToastShown() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Old", 5, false));
        setPrivateField(activity, "userRepository", repo);

        ShadowToast.reset();
        Mockito.doAnswer(invocation -> {
            UserUpdateCallback cb = invocation.getArgument(1);
            cb.onResult(null);
            return null;
        }).when(repo).updateUser(Mockito.any(User.class), Mockito.any());

        invokePrivate(activity, "showChangeUsernameDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        EditText input = findFirstEditText(dialog);
        assertNotNull(input);

        input.setText("NoToastName");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        assertNull(ShadowToast.getTextOfLatestToast());
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
    public void updateUsername_whenCurrentUserNull_doesNotCallRepository() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", null);
        setPrivateField(activity, "userRepository", repo);

        invokePrivateWithArg(activity, "updateUsername", String.class, "NewName");

        Mockito.verify(repo, Mockito.never()).updateUser(Mockito.any(User.class), Mockito.any());
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
    public void deleteAccountDialog_whenDeleteReturnsNullResult_noToastAndNoNavigation() throws Exception {
        UserRepository repo = Mockito.mock(UserRepository.class);

        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        setPrivateField(activity, "currentUser", new User("id1", "Bob", 1, false));
        setPrivateField(activity, "userRepository", repo);

        ShadowToast.reset();
        Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            kotlin.jvm.functions.Function1<Result<Void>, kotlin.Unit> cb = invocation.getArgument(0);
            cb.invoke(null);
            return null;
        }).when(repo).deleteAccount(Mockito.any());

        invokePrivate(activity, "showDeleteAccountDialog");

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();

        assertNull(ShadowToast.getTextOfLatestToast());
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        assertNull(shadowActivity.getNextStartedActivity());
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

    @Test
    public void displayUserProfile_nullName_setsNotSet_and_handlesMissingViews() throws Exception {
        ProfileActivity activity = Robolectric.buildActivity(ProfileActivity.class)
                .create().start().resume().get();

        invokePrivateWithArg(activity, "displayUserProfile", User.class, new User("id9", null, 77, false));

        TextView username = activity.findViewById(R.id.profileUsername);
        assertEquals("Not set", username.getText().toString());

        activity.setContentView(android.R.layout.simple_list_item_1);
        invokePrivate(activity, "setupButtons");
        invokePrivateWithArg(activity, "displayUserProfile", User.class, new User("id8", null, 11, true));
    }

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

    private static void invokePrivateWithArg(Object target, String methodName, Class<?> argType, Object arg) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, argType);
        m.setAccessible(true);
        m.invoke(target, arg);
    }

    private static EditText findFirstEditText(AlertDialog dialog) {
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