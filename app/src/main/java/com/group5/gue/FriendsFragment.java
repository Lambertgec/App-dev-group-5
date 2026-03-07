package com.group5.gue;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group5.gue.data.auth.AuthRepository;
import com.group5.gue.databinding.FragmentFriendsBinding;
import java.util.List;

public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        AuthRepository authRepository = AuthRepository.getInstance(requireContext());

        authRepository.fetchFriends(friendsList -> {
            if (friendsList == null || friendsList.isEmpty()) {
                showNoFriendsUI();
            } else {
                updateFriendsListUI(friendsList);
            }
        });
    }

    private void updateFriendsListUI(List<String> friends) {
        binding.emptyFriendsTextView.setVisibility(View.GONE);
        binding.friendsRecyclerView.setVisibility(View.VISIBLE);

        binding.friendsRecyclerView.setAdapter(new RecyclerView.Adapter<FriendViewHolder>() {
            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setTextSize(18);
                tv.setPadding(32, 32, 32, 32);
                tv.setTextColor(Color.WHITE);
                tv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                return new FriendViewHolder(tv);
            }

            @Override
            public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
                holder.textView.setText(friends.get(position));
            }

            @Override
            public int getItemCount() {
                return friends.size();
            }
        });
    }

    private void showNoFriendsUI() {
        binding.friendsRecyclerView.setVisibility(View.GONE);
        binding.emptyFriendsTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        FriendViewHolder(View v) {
            super(v);
            textView = (TextView) v;
        }
    }
}
