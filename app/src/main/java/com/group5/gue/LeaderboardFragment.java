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

public class LeaderboardFragment extends Fragment {
    private FragmentLeaderboardBinding binding;

    public LeaderboardFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FriendsRepository friendRepository = FriendsRepository.getInstance();

        // Fetch friends with scores for the leaderboard
        friendRepository.fetchFriendsWithScores(profiles -> {
            if (binding == null) {
                return Unit.INSTANCE;
            }
            if (!profiles.isEmpty()) {
                updateLeaderboardUI(profiles);
            } else {
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

    private void updateLeaderboardUI(List<Profile> profiles) {
        binding.leaderboardRecyclerView.setAdapter(new RecyclerView.Adapter<LeaderboardViewHolder>() {
            @NonNull
            @Override
            public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ItemLeaderboardBinding itemBinding = ItemLeaderboardBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new LeaderboardViewHolder(itemBinding);
            }

            @Override
            public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
                Profile profile = profiles.get(position);
                holder.itemBinding.leaderboardNameTextView.setText(
                        profile.getDisplayName() != null ? profile.getDisplayName() : "Unknown");
                holder.itemBinding.leaderboardScoreTextView.setText(
                        String.valueOf(profile.getScore()) + " pts");
            }

            @Override
            public int getItemCount() {
                return profiles.size();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        ItemLeaderboardBinding itemBinding;

        LeaderboardViewHolder(ItemLeaderboardBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
