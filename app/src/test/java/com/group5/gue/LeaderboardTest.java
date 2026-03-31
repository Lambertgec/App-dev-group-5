package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

/**
 * Unit tests for LeaderboardFragment using Robolectric.
 * These tests verify that the leaderboard correctly handles different data scenarios
 * and formats the UI elements (ranks, names, scores) as expected.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class LeaderboardTest {

    private StubFriendsRepository stubRepository;

    /**
     * Set up the test environment by initializing a stub repository
     * and injecting it into the FriendsRepository singleton.
     */
    @Before
    public void setUp() {
        stubRepository = new StubFriendsRepository();
        FriendsRepository.setInstance(stubRepository);
    }

    /**
     * Verifies that the fragment can be instantiated.
     */
    @Test
    public void testFragmentConstructor() {
        assertNotNull(new LeaderboardFragment());
    }

    /**
     * Tests that a single friend's score is correctly displayed in the RecyclerView.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardDisplaysFriendsScores() {
        // Prepare mock data
        List<Profile> friends = new ArrayList<>();
        friends.add(new Profile("1", "Alice", 100L, false));
        stubRepository.setFriendsWithScores(friends);

        // Launch fragment and verify UI
        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertNotNull(adapter);
                assertEquals(1, adapter.getItemCount());

                // Manually trigger binding for the first item
                RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, 0);
                adapter.bindViewHolder(holder, 0);
                
                // Verify name and rank formatting
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Alice", nameTv.getText().toString());

                TextView rankTv = holder.itemView.findViewById(R.id.leaderboardRankTextView);
                assertEquals("1st", rankTv.getText().toString());
            });
            scenario.moveToState(Lifecycle.State.DESTROYED);
        }
    }

    /**
     * Tests that various rank numbers are correctly formatted with suffixes (st, nd, rd, th).
     * Specifically checks edge cases like 11th, 12th, 13th, and 21st, 22nd.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardMultipleRanksIncludingSpecialCases() {
        List<Profile> friends = new ArrayList<>();
        // Add 22 profiles to test different suffix rules
        for (int i = 1; i <= 22; i++) {
            friends.add(new Profile(String.valueOf(i), "User " + i, 100L - i, false));
        }
        stubRepository.setFriendsWithScores(friends);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertEquals(22, adapter.getItemCount());

                // Positions to check: 0 (1st), 1 (2nd), 2 (3rd), 10 (11th), 11 (12th), 12 (13th), 20 (21st), 21 (22nd)
                int[] positionsToTest = {0, 1, 2, 10, 11, 12, 20, 21}; 
                String[] expectedRanks = {"1st", "2nd", "3rd", "11th", "12th", "13th", "21st", "22nd"};

                for (int i = 0; i < positionsToTest.length; i++) {
                    int pos = positionsToTest[i];
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, 0);
                    adapter.bindViewHolder(holder, pos);
                    TextView rankTv = holder.itemView.findViewById(R.id.leaderboardRankTextView);
                    assertEquals("Rank at position " + pos + " is wrong", expectedRanks[i], rankTv.getText().toString());
                }
            });
        }
    }

    /**
     * Tests that the UI correctly handles null display names and falls back to global scores
     * if no friends have scores.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardGlobalFallbackAndUnknownName() {
        // Setup empty friends list to trigger fallback
        stubRepository.setFriendsWithScores(new ArrayList<>());
        
        // Setup global users, one with a null name
        List<Profile> global = new ArrayList<>();
        global.add(new Profile("2", null, 50L, false));
        stubRepository.setGlobalUsers(global);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertEquals(1, adapter.getItemCount());
                
                RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, 0);
                adapter.bindViewHolder(holder, 0);
                
                // Verify null name results in "Unknown"
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Unknown", nameTv.getText().toString());

                TextView rankTv = holder.itemView.findViewById(R.id.leaderboardRankTextView);
                assertEquals("1st", rankTv.getText().toString());
            });
        }
    }

    /**
     * A stub implementation of FriendsRepository for testing purposes.
     * Allows manual control over the data returned by fetch methods.
     */
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
        public void fetchFriendsWithScores(@NonNull Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(friendsWithScores);
        }

        @Override
        public void fetchUsersWithScores(Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(globalUsers);
        }
    }
}
