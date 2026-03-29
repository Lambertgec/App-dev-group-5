package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.gue.data.friends.FriendsRepository;
import com.group5.gue.data.friends.Profile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class FriendsTest {

    private StubFriendsRepository stubRepository;

    @Before
    public void setUp() {
        stubRepository = new StubFriendsRepository();
        FriendsRepository.setInstance(stubRepository);
    }

    @Test
    public void testConstructor() {
        assertNotNull(new FriendsFragment());
    }

    @Test
    public void adminUIAdjusted() {
        stubRepository.setIsAdmin(true);

        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText addFriendEditText = fragment.getView().findViewById(R.id.addFriendEditText);
                assertEquals(fragment.getString(R.string.hint_admin_search), addFriendEditText.getHint().toString());
                assertEquals(View.GONE, fragment.getView().findViewById(R.id.friendsRecyclerView).getVisibility());
            });
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void userUIWithAdapterLogic() {
        stubRepository.setIsAdmin(false);
        List<String> friends = new ArrayList<>();
        friends.add("Alice");
        stubRepository.setFriendsList(friends);

        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.friendsRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertNotNull(adapter);
                
                // Manually trigger ViewHolder creation and binding to hit inner adapter lines
                RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, 0);
                adapter.bindViewHolder(holder, 0);
                
                TextView friendNameTv = holder.itemView.findViewById(R.id.friendNameTextView);
                assertEquals("Alice", friendNameTv.getText().toString());

                // Test removal button inside the ViewHolder
                holder.itemView.findViewById(R.id.removeFriendButton).performClick();
                assertEquals("Alice", stubRepository.lastRemovedFriend);
            });
        }
    }

    @Test
    public void addFriendTriggersRepository() {
        stubRepository.setIsAdmin(false);
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText input = fragment.getView().findViewById(R.id.addFriendEditText);
                input.setText("Bob");
                fragment.getView().findViewById(R.id.addFriendButton).performClick();
                assertEquals("Bob", stubRepository.lastAddedFriend);
                assertEquals("", input.getText().toString()); // Cleared on success
            });
        }
    }

    @Test
    public void addFriendSearchAction() {
        stubRepository.setIsAdmin(false);
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText input = fragment.getView().findViewById(R.id.addFriendEditText);
                input.setText("Charlie");
                input.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
                assertEquals("Charlie", stubRepository.lastAddedFriend);
            });
        }
    }

    @Test
    public void addFriendWithEmptyInput() {
        stubRepository.setIsAdmin(false);
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                fragment.getView().findViewById(R.id.addFriendButton).performClick();
                assertNull(stubRepository.lastAddedFriend);
            });
        }
    }

    @Test
    public void fragmentCleanup() {
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.moveToState(Lifecycle.State.DESTROYED);
        }
    }

    private static class StubFriendsRepository extends FriendsRepository {
        private boolean isAdmin = false;
        private List<String> friendsList = new ArrayList<>();
        public String lastAddedFriend = null;
        public String lastRemovedFriend = null;

        public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
        public void setFriendsList(List<String> friends) { this.friendsList = friends; }

        @Override
        public void isAdmin(Function1<? super Boolean, Unit> callback) { callback.invoke(isAdmin); }

        @Override
        public void fetchFriends(Function1<? super List<String>, Unit> callback) { callback.invoke(friendsList); }

        @Override
        public void addFriendByDisplayName(String displayName, Function2<? super Boolean, ? super String, Unit> callback) {
            this.lastAddedFriend = displayName;
            callback.invoke(true, "Success");
        }

        @Override
        public void removeFriendByDisplayName(String displayName, Function1<? super Boolean, Unit> callback) {
            this.lastRemovedFriend = displayName;
            callback.invoke(true);
        }

        @Override
        public void fetchFriendsWithScores(Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(new ArrayList<>());
        }
    }
}
