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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kotlin.Unit;

/**
 * Displays collectibles in a grid view and shows details for the selected item.
 */
public class CollectiblesGalleryFragment extends Fragment {

    private final CollectibleRepository repository = CollectibleRepository.Companion.getInstance();

    private CollectibleGridAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private View detailCard;
    private TextView detailNameView;
    private TextView detailDescriptionView;

    public CollectiblesGalleryFragment() {
        super(R.layout.fragment_collectibles_gallery);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CollectibleGridAdapter(this::showDetails);

        progressBar = view.findViewById(R.id.collectiblesProgressBar);
        emptyView = view.findViewById(R.id.collectiblesEmptyView);
        detailCard = view.findViewById(R.id.collectibleDetailsCard);
        detailNameView = view.findViewById(R.id.collectibleDetailName);
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
        loadCollectibles(false);
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

        repository.getAllCollectibles(collectibles -> {
            progressBar.setVisibility(View.GONE);

            List<Collectible> sortedCollectibles = new ArrayList<>(collectibles);
            sortedCollectibles.sort(Comparator.comparingLong(Collectible::getId).reversed());

            adapter.submitItems(sortedCollectibles);
            emptyView.setText(R.string.collectibles_empty);
            emptyView.setVisibility(sortedCollectibles.isEmpty() ? View.VISIBLE : View.GONE);

            if (showUploadToast) {
                Toast.makeText(requireContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
            }

            return Unit.INSTANCE;
        });
    }

    /**
     * Displays Collectible specific data
     * 
     * @param collectible the collectible to show details for
     */
    private void showDetails(Collectible collectible) {
        detailCard.setVisibility(View.VISIBLE);
        detailNameView.setText(collectible.getName());


        String description = collectible.getDescription();
        if (description == null || description.trim().isEmpty()) {
            detailDescriptionView.setText(R.string.collectible_no_description);
        } else {
            detailDescriptionView.setText(description);
        }
    }
}