package com.group5.gue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.gue.data.friends.FriendsRepository;
import com.group5.gue.data.friends.Profile;
import com.group5.gue.databinding.FragmentLeaderboardBinding;
import com.group5.gue.databinding.ItemLeaderboardBinding;

import java.util.List;

import kotlin.Unit;

/**
 * Fragment responsible for displaying the leaderboard.
 * The leaderboard shows friend users and their respective scores, ranking them accordingly.
 * It prioritizes showing the scores of the user's friends, falling back to a global
 * leaderboard if the user has no friends.
 */
public class LeaderboardFragment extends Fragment {
    
    // View binding for the leaderboard fragment layout.
    private FragmentLeaderboardBinding binding;

    /**
     * Default constructor for LeaderboardFragment.
     */
    public LeaderboardFragment() {
    }

    /**
     * Inflates the layout for this fragment and initializes the binding.
     *
     * @param inflater LayoutInflater to inflate views.
     * @param container Parent view group.
     * @param savedInstanceState Bundle with saved space.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes views after the view has been created.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState Bundle with saved data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the RecyclerView with a vertical linear layout manager
        binding.leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get the instance of the FriendsRepository to fetch score data
        FriendsRepository friendRepository = FriendsRepository.getInstance();

        // Fetch friends with scores for the leaderboard
        friendRepository.fetchFriendsWithScores(profiles -> {
            if (binding == null) {
                return Unit.INSTANCE;
            }
            if (!profiles.isEmpty()) {
                // If friends data is available, update the UI with it
                updateLeaderboardUI(profiles);
            } else {
                // If no friends exist or they have no scores, fall back to global leaderboard
                friendRepository.fetchUsersWithScores(globalProfiles -> {
                    if (binding != null) {
                        updateLeaderboardUI(globalProfiles);
                    }
                    return Unit.INSTANCE;
                });
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * Updates the RecyclerView adapter with the provided list of profiles.
     * 
     * @param profiles The list of Profile objects (name and score) to display in the leaderboard.
     */
    private void updateLeaderboardUI(List<Profile> profiles) {
        binding.leaderboardRecyclerView.setAdapter(new RecyclerView.Adapter<LeaderboardViewHolder>() {
            @NonNull
            @Override
            public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the item layout for a single leaderboard entry
                ItemLeaderboardBinding itemBinding = ItemLeaderboardBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new LeaderboardViewHolder(itemBinding);
            }

            @Override
            public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
                // Get the profile data for the current position
                Profile profile = profiles.get(position);
                
                // Set the display name, defaulting to "Unknown" if it's null
                holder.itemBinding.leaderboardNameTextView.setText(
                        profile.getDisplayName() != null ? profile.getDisplayName() : "Unknown");
                
                // Format and set the score text
                holder.itemBinding.leaderboardScoreTextView.setText(
                        String.valueOf(profile.getScore()) + " pts");
            }

            @Override
            public int getItemCount() {
                // Return the size of the profile list
                return profiles.size();
            }
        });
    }

    /**
     * Cleans up the view binding when the fragment's view is destroyed to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * ViewHolder class for individual leaderboard items.
     * Uses view binding to access the item's views.
     */
    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderboardBinding itemBinding;

        /**
         * Constructor for the ViewHolder.
         * 
         * @param itemBinding The binding object for the item's layout.
         */
        LeaderboardViewHolder(ItemLeaderboardBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
