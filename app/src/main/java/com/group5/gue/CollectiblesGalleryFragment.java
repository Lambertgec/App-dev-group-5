package com.group5.gue;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
 * Displays collectibles in a grid view and shows details for the selected item.
 */
public class CollectiblesGalleryFragment extends Fragment {

    private final CollectibleRepository repository = CollectibleRepository.Companion.getInstance();
    private final UserRepository userRepository = UserRepository.Companion.getInstance();

    private CollectibleGridAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView userScoreView;
    private View detailCard;
    private TextView detailNameView;
    private TextView detailCostView;
    private TextView detailDescriptionView;
    private Set<Integer> ownedCollectibleIds = new HashSet<>();

    public CollectiblesGalleryFragment() {
        super(R.layout.fragment_collectibles_gallery);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CollectibleGridAdapter(this::showDetails);

        progressBar = view.findViewById(R.id.collectiblesProgressBar);
        emptyView = view.findViewById(R.id.collectiblesEmptyView);
        userScoreView = view.findViewById(R.id.collectiblesUserScore);
        detailCard = view.findViewById(R.id.collectibleDetailsCard);
        detailNameView = view.findViewById(R.id.collectibleDetailName);
        detailCostView = view.findViewById(R.id.collectibleDetailCost);
        detailDescriptionView = view.findViewById(R.id.collectibleDetailDescription);

        RecyclerView recyclerView = view.findViewById(R.id.collectiblesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(adapter);

        Button backButton = view.findViewById(R.id.backHomeButton);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        Button uploadButton = view.findViewById(R.id.openUploadButton);
        uploadButton.setOnClickListener(v -> {
            int containerId = ((View) requireView().getParent()).getId();
            getParentFragmentManager().beginTransaction()
                .replace(containerId, new UploadCollectibleFragment())
                .addToBackStack(null)
                .commit();
        });

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

    private void refreshUserScore() {
        User cachedUser = userRepository.getCachedUser();
        int userScore = cachedUser != null ? cachedUser.getScore() : 0;
        userScoreView.setText(getString(R.string.collectibles_user_score_value, userScore));
    }

    /**
     * Loads collectibles from the database to populate the grid
     * 
     * Displays a toast if loading is triggered by a successful upload
     * 
     * @param showUploadToast whether to show a toast confirming a successful upload
     */

    private void loadCollectibles(boolean showUploadToast) {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        detailCard.setVisibility(View.GONE);

        repository.getAllCollectibles(collectibles -> {
            User cachedUser = userRepository.getCachedUser();
            String userId = cachedUser != null ? cachedUser.getId() : null;

            if (userId == null || userId.trim().isEmpty()) {
                applyCollectiblesState(collectibles, new HashSet<>(), showUploadToast);
                return Unit.INSTANCE;
            }

            userRepository.getOwnedCollectibleIds(userId, ownedIds -> {
                applyCollectiblesState(collectibles, ownedIds, showUploadToast);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
    }

    private void applyCollectiblesState(
        List<Collectible> collectibles,
        Set<Integer> ownedIds,
        boolean showUploadToast
    ) {
        progressBar.setVisibility(View.GONE);

        ownedCollectibleIds = new HashSet<>(ownedIds);
        List<Collectible> sortedCollectibles = new ArrayList<>(collectibles);
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
        detailNameView.setText(collectible.getName());
        detailCostView.setText(getString(R.string.collectible_cost, collectible.getScore()));


        String description = collectible.getDescription();
        if (description == null || description.trim().isEmpty()) {
            detailDescriptionView.setText(R.string.collectible_no_description);
        } else {
            detailDescriptionView.setText(description);
        }
    }
}