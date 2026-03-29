package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.TextView;

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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class LeaderboardTest {

    private StubFriendsRepository stubRepository;

    @Before
    public void setUp() {
        stubRepository = new StubFriendsRepository();
        FriendsRepository.setInstance(stubRepository);
    }

    @Test
    public void testFragmentConstructor() {
        assertNotNull(new LeaderboardFragment());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardDisplaysFriendsScores() {
        List<Profile> friends = new ArrayList<>();
        friends.add(new Profile("1", "Alice", 100L, false));
        stubRepository.setFriendsWithScores(friends);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                
                recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                recyclerView.layout(0, 0, 1000, 1000);

                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                assertNotNull(adapter);
                assertEquals(1, adapter.getItemCount());

                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(0);
                assertNotNull(holder);
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Alice", nameTv.getText().toString());
            });
        }
    }

    @Test
    public void leaderboardGlobalWhenNoFriends() {
        stubRepository.setFriendsWithScores(new ArrayList<>());
        List<Profile> global = new ArrayList<>();
        global.add(new Profile("3", "Charlie", 200L, false));
        global.add(new Profile("4", null, 150L, false)); // Test null display name path
        stubRepository.setGlobalUsers(global);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                recyclerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                recyclerView.layout(0, 0, 1000, 1000);
                
                assertEquals(2, recyclerView.getAdapter().getItemCount());
                
                // Verify fallback to "Unknown"
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(1);
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Unknown", nameTv.getText().toString());
            });
        }
    }

    @Test
    public void testFragmentDestruction() {
        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.moveToState(Lifecycle.State.DESTROYED);
        }
    }

    private static class StubFriendsRepository extends FriendsRepository {
        private List<Profile> friendsWithScores = new ArrayList<>();
        private List<Profile> globalUsers = new ArrayList<>();

        public void setFriendsWithScores(List<Profile> friends) {
            this.friendsWithScores = friends;
        }

        public void setGlobalUsers(List<Profile> global) {
            this.globalUsers = global;
        }

        @Override
        public void fetchFriendsWithScores(Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(friendsWithScores);
        }

        @Override
        public void fetchUsersWithScores(Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(globalUsers);
        }
    }
}
