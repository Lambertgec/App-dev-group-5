package com.group5.gue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.gue.data.friends.FriendsRepository;
import com.group5.gue.databinding.FragmentFriendsBinding;
import com.group5.gue.databinding.ItemFriendBinding;

import kotlin.Unit;
import java.util.List;

/**
 * Fragment responsible for displaying and managing the user's friends list.
 * It allows users to search for and add new friends, view existing ones,
 * and remove them from their list.
 * 
 * For admin users, the functionality is adjusted to allow for elevating user.
 * privileges instead of creating relations.
 */
public class FriendsFragment extends Fragment {
    
    // View binding for the fragment layout.
    private FragmentFriendsBinding binding;

    // Flag to track if the currently logged-in user has admin privileges.
    private boolean isAdminUser = false;

    /**
     * Default constructor for the FriendsFragment.
     */
    public FriendsFragment() {
    }

    /**
     * Inflates the fragment's view using view binding.
     *
     * @param inflater The LayoutInflater to inflate views.
     * @param container Parent view.
     * @param savedInstanceState Bundle with saved space.
     * @return The root view of the inflated layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Initializes the fragment's UI after the view has been created.
     * Sets up the RecyclerView, handles role-based UI adjustments.
     *
     * @param view The View returned by onCreateView.
     * @param savedInstanceState Bundle with saved space.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the singleton instance of the friends repository
        FriendsRepository repository = FriendsRepository.getInstance();

        // Configure the friends RecyclerView with a linear layout manager
        binding.friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Hide the message of user having no friends.
        binding.emptyFriendsTextView.setVisibility(View.GONE);
        // Show the friend list of the current user.
        binding.friendsRecyclerView.setVisibility(View.VISIBLE);

        // Check user role to customize functionality
        repository.isAdmin(isAdmin -> {
            if (binding == null) return Unit.INSTANCE;

            this.isAdminUser = isAdmin;
            if (isAdmin) {
                // Admins see a search-only interface
                binding.addFriendEditText.setHint(R.string.hint_admin_search);
                binding.emptyFriendsTextView.setVisibility(View.GONE);
                binding.friendsRecyclerView.setVisibility(View.GONE);
            } else {
                // Regular users see their friends list
                binding.addFriendEditText.setHint(R.string.hint_user_search);
                binding.friendsRecyclerView.setVisibility(View.VISIBLE);
                refreshFriends(repository);
            }
            return Unit.INSTANCE;
        });

        // Set up listener for the 'Add Friend' button
        binding.addFriendButton.setOnClickListener(v -> performAddFriend(repository));

        // Set up keyboard action listener for adding friends via the search field
        binding.addFriendEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                performAddFriend(repository);
                return true;
            }
            return false;
        });
    }

    /**
     * Loads the friends list and their associated scores from the repository.
     * Updates the UI to show an empty state message if no friends are found.
     */
    private void loadFriendsList() {
        FriendsRepository.getInstance().fetchFriendsWithScores(profiles -> {
            if (binding == null) return Unit.INSTANCE;

            if (profiles.isEmpty()) {
                // Display the "no friends" placeholder
                binding.emptyFriendsTextView.setVisibility(View.VISIBLE);
            } else {
                // Hide the "no friends" placeholder
                binding.emptyFriendsTextView.setVisibility(View.GONE);
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * Attempts to add a friend based on the username entered in the search field.
     * Validates the input and shows feedback via Toast messages.
     *
     * @param repository The repository to perform the addition through.
     */
    private void performAddFriend(FriendsRepository repository) {
        String username = binding.addFriendEditText.getText().toString().trim();
        if (!username.isEmpty()) {
            // Call repository to add friend by their display name
            repository.addFriendByDisplayName(username, (success, message) -> {
                if (getContext() != null) {
                    // Show success or error message to the user
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
                if (success) {
                    // Clear the input field upon successful addition
                    if (binding != null) {
                        binding.addFriendEditText.setText("");
                    }
                    // Refresh the list if the user is not an admin
                    if (!isAdminUser) {
                        refreshFriends(repository);
                    }
                }
                return Unit.INSTANCE;
            });
        } else {
            // Handle empty search field
            Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Refresh the list of friends of the user currently signed in.
     * Fetches the latest data from the backend.
     * 
     * @param repository Repository instance to fetch data from.
     */
    private void refreshFriends(FriendsRepository repository) {
        repository.fetchFriends(friendsList -> {
            if (friendsList.isEmpty()) {
                // User has no friends yet :(
                showNoFriendsUI();
            } else {
                // Populate the UI with the fetched friends
                updateFriendsListUI(friendsList);
            }
            return Unit.INSTANCE;
        });
    }

    /**
     * Fill the RecyclerView with the list of friends.
     * Handles friend removal logic.
     * 
     * @param friends List of usernames to display.
     */
    private void updateFriendsListUI(List<String> friends) {
        if (binding == null) return;
        binding.emptyFriendsTextView.setVisibility(View.GONE);
        binding.friendsRecyclerView.setVisibility(View.VISIBLE);

        // Configure and set the adapter for the friends list
        binding.friendsRecyclerView.setAdapter(new RecyclerView.Adapter<FriendViewHolder>() {
            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the item layout for each friend
                ItemFriendBinding itemBinding = ItemFriendBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
                return new FriendViewHolder(itemBinding);
            }

            @Override
            public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
                String friendName = friends.get(position);
                // Set the friend's name in the UI
                holder.itemBinding.friendNameTextView.setText(friendName);
                // Handle the 'Remove Friend' click event
                holder.itemBinding.removeFriendButton.setOnClickListener(v -> {
                    FriendsRepository.getInstance().removeFriendByDisplayName(friendName, success -> {
                        if (success) {
                            // Update the list immediately after removal
                            refreshFriends(FriendsRepository.getInstance());
                        }
                        return Unit.INSTANCE;
                    });
                });
            }

            /**
             * Get the total number of friends in the list.
             * @return The number of friends.
             */
            @Override
            public int getItemCount() {
                return friends.size();
            }
        });
    }

    /**
     * Show a message stating no friends added yet.
     * Hides the list view and displays the empty state placeholder.
     */
    private void showNoFriendsUI() {
        if (binding == null) return;
        binding.friendsRecyclerView.setVisibility(View.GONE);
        if (!isAdminUser) {
            // Show message for regular users
            binding.emptyFriendsTextView.setVisibility(View.VISIBLE);
        } else {
            // Admins don't see an empty friends list message
            binding.emptyFriendsTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Cleanup resources when the fragment's view is destroyed.
     * Prevents memory leaks by setting the binding object to null.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * ViewHolder class for individual friend items in the RecyclerView.
     */
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        // View binding for the friend item layout.
        ItemFriendBinding itemBinding;

        /**
         * Constructor for the ViewHolder.
         * 
         * @param itemBinding The binding object for the item's layout.
         */
        FriendViewHolder(ItemFriendBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
