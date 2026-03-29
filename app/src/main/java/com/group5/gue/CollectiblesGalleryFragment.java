package com.group5.gue;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.gue.data.Result;
import com.group5.gue.data.collectible.CollectibleRepository;
import com.group5.gue.data.model.Collectible;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Unit;

/**
 * Fragment that displays a gallery of collectibles in a grid layout.
 * Users can view their owned items, see details for each item, and admins can upload new ones.
 */
public class CollectiblesGalleryFragment extends Fragment {

    // Repository for managing collectible data fetching and deletion.
    private final CollectibleRepository repository = CollectibleRepository.Companion.getInstance();
    // Repository for accessing user profile and score information.
    private final UserRepository userRepository = UserRepository.Companion.getInstance();

    private User currentUser;
    private CollectibleGridAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView userScoreView;
    private View detailCard;
    private TextView detailNameView;
    private TextView detailCostView;
    private TextView detailDescriptionView;
    // Tracks which collectibles the user has already acquired.
    private Set<Integer> ownedCollectibleIds = new HashSet<>();


    /**
     * Initializes the fragment with the gallery layout.
     */
    public CollectiblesGalleryFragment() {
        super(R.layout.fragment_collectibles_gallery);
    }

    /**
     * Sets up UI components, adapters, and result listeners for the gallery view.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = userRepository.getCachedUser();
        adapter = new CollectibleGridAdapter(this::showDetails);


        progressBar = view.findViewById(R.id.collectiblesProgressBar);
        emptyView = view.findViewById(R.id.collectiblesEmptyView);
        userScoreView = view.findViewById(R.id.collectiblesUserScore);
        detailCard = view.findViewById(R.id.collectibleDetailsCard);
        detailNameView = view.findViewById(R.id.collectibleDetailName);
        detailCostView = view.findViewById(R.id.collectibleDetailCost);
        detailDescriptionView = view.findViewById(R.id.collectibleDetailDescription);

        // Configure RecyclerView with a 2-column grid layout
        RecyclerView recyclerView = view.findViewById(R.id.collectiblesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);

        // Navigation button to return to the previous screen
        Button backButton = view.findViewById(R.id.backHomeButton);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Admin-only upload button configuration
        Button uploadButton = view.findViewById(R.id.openUploadButton);
        boolean isAdmin = currentUser != null && currentUser.isAdmin();
        uploadButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        if (isAdmin) {
            uploadButton.setOnClickListener(v -> {
                int containerId = ((View) requireView().getParent()).getId();
                getParentFragmentManager().beginTransaction()
                    .replace(containerId, new UploadCollectibleFragment())
                    .addToBackStack(null)
                    .commit();
            });
        }

        // Upload returns via fragment result so the gallery can refresh without a direct fragment reference.
        getParentFragmentManager().setFragmentResultListener(
            UploadCollectibleFragment.RESULT_KEY,
            getViewLifecycleOwner(),
            (requestKey, result) -> loadCollectibles(true)
        );

        detailCard.setVisibility(View.GONE);
        refreshUserScore();
        loadCollectibles(false);
    }

    /**
     * Updates the UI to display the current user's total score.
     */
    private void refreshUserScore() {
        int userScore = currentUser != null ? currentUser.getScore() : 0;
        userScoreView.setText(getString(R.string.collectibles_user_score_value, userScore));
    }

    /**
     * Loads collectibles from the database to populate the grid
     * Displays a toast if loading is triggered by a successful upload
     * 
     * @param showUploadToast whether to show a toast confirming a successful upload
     */

    private void loadCollectibles(boolean showUploadToast) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        detailCard.setVisibility(View.GONE);

        repository.getAllCollectibles(collectibles -> {
            String userId = currentUser != null ? currentUser.getId() : null;

            if (userId == null || userId.trim().isEmpty()) {
                applyCollectiblesState(collectibles, new HashSet<>(), showUploadToast);
                return Unit.INSTANCE;
            }

            // Sync with owned items to update the UI visuals (locked vs unlocked)
            userRepository.getOwnedCollectibleIds(userId, ownedIds -> {
                applyCollectiblesState(collectibles, ownedIds, showUploadToast);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
    }

    /**
     * Sorts and displays the collectible items in the adapter.
     */
    private void applyCollectiblesState(
        List<Collectible> collectibles,
        Set<Integer> ownedIds,
        boolean showUploadToast
    ) {
        progressBar.setVisibility(View.GONE);

        ownedCollectibleIds = new HashSet<>(ownedIds);
        List<Collectible> sortedCollectibles = new ArrayList<>(collectibles);
        // Sort items by ID in descending order to show newest additions first
        sortedCollectibles.sort(Comparator.comparingLong(Collectible::getId).reversed());

        adapter.setOwnedCollectibleIds(ownedCollectibleIds);
        adapter.submitItems(sortedCollectibles);

        emptyView.setText(R.string.collectibles_empty);
        emptyView.setVisibility(sortedCollectibles.isEmpty() ? View.VISIBLE : View.GONE);

        if (showUploadToast) {
            Toast.makeText(requireContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays Collectible specific data
     * 
     * @param collectible the collectible to show details for
     */
    private void showDetails(Collectible collectible) {
        detailCard.setVisibility(View.VISIBLE);
        setupDeleteButton(collectible);
        detailNameView.setText(collectible.getName());
        detailCostView.setText(getString(R.string.collectible_cost, collectible.getScore()));


        String description = collectible.getDescription();
        if (description == null || description.trim().isEmpty()) {
            detailDescriptionView.setText(R.string.collectible_no_description);
        } else {
            detailDescriptionView.setText(description);
        }
    }

    /**
     * Configures the delete button within the detail view (restricted to admins).
     * 
     * @param collectible The collectible associated with the delete action.
     */
    private void setupDeleteButton(Collectible collectible) {
        Button deletebutton = detailCard.findViewById(R.id.collectibleDetailDeleteButton);
        if (currentUser != null && currentUser.isAdmin()) {
            deletebutton.setVisibility(View.VISIBLE);
            deletebutton.setOnClickListener(v -> repository.deleteCollectible(collectible.getId(), result -> {
                if (result instanceof Result.Success) {
                    Toast.makeText(requireContext(), "Collectible deleted", Toast.LENGTH_SHORT).show();
                    loadCollectibles(false);
                } else if (result instanceof Result.Error) {
                    Exception error = ((Result.Error<Unit>) result).getError();
                    Toast.makeText(requireContext(), "Failed to delete: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return Unit.INSTANCE;
            }));
        } else {
            deletebutton.setVisibility(View.GONE);
        }
    }
}