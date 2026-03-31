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
                RecyclerView rv = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertNotNull(adapter);
                assertEquals(1, adapter.getItemCount());

                // Manually trigger binding
                RecyclerView.ViewHolder holder = adapter.createViewHolder(rv, 0);
                adapter.bindViewHolder(holder, 0);
                
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Alice", nameTv.getText().toString());

                TextView rankTv = holder.itemView.findViewById(R.id.leaderboardRankTextView);
                assertEquals("1st", rankTv.getText().toString());
            });
            scenario.moveToState(Lifecycle.State.DESTROYED);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardMultipleRanksIncludingSpecialCases() {
        List<Profile> friends = new ArrayList<>();
        // Add 22 profiles to test the "22nd" case
        for (int i = 1; i <= 22; i++) {
            friends.add(new Profile(String.valueOf(i), "User " + i, 100L - i, false));
        }
        stubRepository.setFriendsWithScores(friends);

        try (FragmentScenario<LeaderboardFragment> scenario = FragmentScenario.launchInContainer(LeaderboardFragment.class)) {
            scenario.onFragment(fragment -> {
                RecyclerView rv = fragment.getView().findViewById(R.id.leaderboardRecyclerView);
                RecyclerView.Adapter adapter = rv.getAdapter();
                assertEquals(22, adapter.getItemCount());

                // Test a few specific ranks
                int[] positionsToTest = {0, 1, 2, 10, 11, 12, 20, 21}; // 1st, 2nd, 3rd, 11th, 12th, 13th, 21st, 22nd
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

    @Test
    @SuppressWarnings("unchecked")
    public void leaderboardGlobalFallbackAndUnknownName() {
        stubRepository.setFriendsWithScores(new ArrayList<>());
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
                TextView nameTv = holder.itemView.findViewById(R.id.leaderboardNameTextView);
                assertEquals("Unknown", nameTv.getText().toString());

                TextView rankTv = holder.itemView.findViewById(R.id.leaderboardRankTextView);
                assertEquals("1st", rankTv.getText().toString());
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
        public void fetchFriendsWithScores(@NonNull Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(friendsWithScores);
        }

        @Override
        public void fetchUsersWithScores(Function1<? super List<Profile>, Unit> callback) {
            callback.invoke(globalUsers);
        }
    }
}
