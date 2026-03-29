package com.group5.gue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group5.gue.data.model.Collectible;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView adapter for collectible grid tiles
 *
 * Binds collectible data to the items and returns click events via callback
 *
 */
public class CollectibleGridAdapter extends RecyclerView.Adapter<CollectibleGridAdapter.CollectibleViewHolder> {

    /**
     * Callback for when a collectible is clicked
     */
    public interface OnCollectibleClickListener {
        /**
         * Called when a collectible item has been clicked.
         * 
         * @param collectible The collectible object associated with the clicked item.
         */
        void onCollectibleClick(Collectible collectible);
    }

    // List of all collectible items to be displayed in the grid.
    private final List<Collectible> items = new ArrayList<>();
    // Set of IDs for collectibles that the current user has already acquired.
    private final Set<Integer> ownedCollectibleIds = new HashSet<>();
    // Listener for handling click events on individual grid items.
    private final OnCollectibleClickListener onCollectibleClickListener;

    /**
     * Constructs a new adapter with a specified click listener.
     * 
     * @param onCollectibleClickListener The listener to handle item clicks.
     */
    public CollectibleGridAdapter(OnCollectibleClickListener onCollectibleClickListener) {
        this.onCollectibleClickListener = onCollectibleClickListener;
    }

    /**
     * Replaces the current items in the adapter with a new list and refreshes the UI.
     * 
     * @param collectibles The new list of collectibles to display.
     */
    public void submitItems(List<Collectible> collectibles) {
        items.clear();
        items.addAll(collectibles);
        notifyDataSetChanged();
    }

    /**
     * Updates the set of owned collectibles and refreshes the UI to show unlocked items.
     * 
     * @param ownedIds The set of IDs belonging to the user's collection.
     */
    public void setOwnedCollectibleIds(Set<Integer> ownedIds) {
        ownedCollectibleIds.clear();
        ownedCollectibleIds.addAll(ownedIds);
        notifyDataSetChanged();
    }


    /**
     * Inflates the layout for a single collectible grid item.
     */
    @NonNull
    @Override
    public CollectibleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_collectible_grid, parent, false);
        return new CollectibleViewHolder(view);
    }

    /**
     * Binds the collectible data at a specific position to its ViewHolder.
     * 
     * @param holder The ViewHolder to update.
     * @param position The index of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull CollectibleViewHolder holder, int position) {
        Collectible collectible = items.get(position);
        // Determine if this item should be shown as 'unlocked'
        boolean isOwned = ownedCollectibleIds.contains(collectible.getId());
        holder.bind(collectible, isOwned, onCollectibleClickListener);
    }

    /**
     * Returns the total count of collectibles in the adapter.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder class that holds the views for a single collectible grid tile.
     */
    static class CollectibleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView costView;

        public CollectibleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.collectibleImageView);
            nameView = itemView.findViewById(R.id.collectibleNameView);
            costView = itemView.findViewById(R.id.collectibleCostView);
        }

        /**
         * Populates the views with collectible data and configures the appearance based on ownership.
         * 
         * @param collectible The collectible data.
         * @param isOwned Whether the user owns this collectible.
         * @param listener Callback for click events.
         */
        void bind(Collectible collectible, boolean isOwned, OnCollectibleClickListener listener) {
            nameView.setText(collectible.getName());
            costView.setText(itemView.getContext().getString(R.string.collectible_cost, collectible.getScore()));

            if (isOwned) {
                // Owned collectibles show their uploaded image.
                Glide.with(itemView)
                    .load(collectible.getImageUrl())
                    .placeholder(R.drawable.collecto_1)
                    .error(R.drawable.collecto_1)
                    .centerCrop()
                    .into(imageView);
            } else {
                // Unowned collectibles use the default image regardless of image_url.
                Glide.with(itemView)
                    .load(R.drawable.collecto_1)
                    .centerCrop()
                    .into(imageView);
            }

            // Allows parent object to decide how to handle clicks
            itemView.setOnClickListener(v -> listener.onCollectibleClick(collectible));
        }
    }
}