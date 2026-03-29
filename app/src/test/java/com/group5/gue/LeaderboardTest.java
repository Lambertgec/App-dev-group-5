package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void leaderboardDisplaysFriendsScores() {
        List<Profile> friends = new ArrayList<>();
        friends.add(new Profile("1", "Alice", 100L, false));
        friends.add(new Profile("2", "Bob", 50L, false));
        stubRepository.setFriendsWithScores(friends);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                assertNotNull("RecyclerView should not be null", recyclerView);
                assertNotNull("Adapter should not be null", recyclerView.getAdapter());
                assertEquals("Should display 2 friends", 2, recyclerView.getAdapter().getItemCount());
            });
        }
    }

    @Test
    public void leaderboardGlobalWhenNoFriends() {
        stubRepository.setFriendsWithScores(new ArrayList<>());
        List<Profile> global = new ArrayList<>();
        global.add(new Profile("3", "Charlie", 200L, false));
        stubRepository.setGlobalUsers(global);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView recyclerView = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                assertEquals("Should display 1 global user", 1, recyclerView.getAdapter().getItemCount());
            });
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
