package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.testing.FragmentScenario;
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
    public void userUIAdjusted() {
        stubRepository.setIsAdmin(false);
        List<String> friends = new ArrayList<>();
        friends.add("Friend1");
        stubRepository.setFriendsList(friends);

        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText addFriendEditText = fragment.getView().findViewById(R.id.addFriendEditText);
                assertEquals(fragment.getString(R.string.hint_user_search), addFriendEditText.getHint().toString());
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.friendsRecyclerView);
                assertEquals(View.VISIBLE, recyclerView.getVisibility());
                assertEquals(1, recyclerView.getAdapter().getItemCount());
            });
        }
    }

    @Test
    public void addFriendClearsInputOnSuccess() {
        stubRepository.setIsAdmin(false);
        
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText addFriendEditText = fragment.getView().findViewById(R.id.addFriendEditText);
                addFriendEditText.setText("NewFriend");
                fragment.getView().findViewById(R.id.addFriendButton).performClick();
                
                assertEquals("", addFriendEditText.getText().toString());
                assertEquals("NewFriend", stubRepository.lastAddedFriend);
            });
        }
    }

    @Test
    public void addFriendWithEmptyInputDoesNothing() {
        stubRepository.setIsAdmin(false);
        
        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                EditText addFriendEditText = fragment.getView().findViewById(R.id.addFriendEditText);
                addFriendEditText.setText("");
                fragment.getView().findViewById(R.id.addFriendButton).performClick();
                
                assertNull(stubRepository.lastAddedFriend);
            });
        }
    }

    @Test
    public void showEmptyMessageWhenNoFriends() {
        stubRepository.setIsAdmin(false);
        stubRepository.setFriendsList(new ArrayList<>());

        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                assertEquals(View.VISIBLE, fragment.getView().findViewById(R.id.emptyFriendsTextView).getVisibility());
                assertEquals(View.GONE, fragment.getView().findViewById(R.id.friendsRecyclerView).getVisibility());
            });
        }
    }

    @Test
    public void removeFriendTriggersRepository() {
        stubRepository.setIsAdmin(false);
        List<String> friends = new ArrayList<>();
        friends.add("FriendToDelete");
        stubRepository.setFriendsList(friends);

        try (FragmentScenario<FriendsFragment> scenario = FragmentScenario.launchInContainer(FriendsFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.friendsRecyclerView);
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(0);
                if (viewHolder != null) {
                    viewHolder.itemView.findViewById(R.id.removeFriendButton).performClick();
                    assertEquals("FriendToDelete", stubRepository.lastRemovedFriend);
                }
            });
        }
    }

    private static class StubFriendsRepository extends FriendsRepository {
        private boolean isAdmin = false;
        private List<String> friendsList = new ArrayList<>();
        public String lastAddedFriend = null;
        public String lastRemovedFriend = null;

        public void setIsAdmin(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        public void setFriendsList(List<String> friends) {
            this.friendsList = friends;
        }

        @Override
        public void isAdmin(Function1<? super Boolean, Unit> callback) {
            callback.invoke(isAdmin);
        }

        @Override
        public void fetchFriends(Function1<? super List<String>, Unit> callback) {
            callback.invoke(friendsList);
        }

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
