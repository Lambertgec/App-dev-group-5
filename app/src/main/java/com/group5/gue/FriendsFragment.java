package com.group5.gue;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.gue.data.friends.FriendsRepository;
import com.group5.gue.databinding.FragmentFriendsBinding;

import kotlin.Unit;
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

        FriendsRepository friendRepository = FriendsRepository.getInstance();

        refreshFriends(friendRepository);

        binding.addFriendEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String username = binding.addFriendEditText.getText().toString().trim();
                if (!username.isEmpty()) {
                    friendRepository.addFriendByDisplayName(username, (success, message) -> {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            binding.addFriendEditText.setText("");
                            refreshFriends(friendRepository);
                        }
                        return Unit.INSTANCE;
                    });
                }
                return true;
            }
            return false;
        });
    }

    /*
     * Refresh the list of friends of the user currently signed in.
     */
    private void refreshFriends(FriendsRepository repository) {
        repository.fetchFriends(friendsList -> {
            if (friendsList.isEmpty()) {
                showNoFriendsUI();
            } else {
                updateFriendsListUI(friendsList);
            }
            return Unit.INSTANCE;
        });
    }

    /*
     * Fill the RecyclerView with the list of friends.
     */
    private void updateFriendsListUI(List<String> friends) {
        if (binding == null) return;
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

    /*
     * Show a message stating no friends added yet.
     */
    private void showNoFriendsUI() {
        if (binding == null) return;
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
