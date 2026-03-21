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
        void onCollectibleClick(Collectible collectible);
    }

    private final List<Collectible> items = new ArrayList<>();
    private final Set<Integer> ownedCollectibleIds = new HashSet<>();
    private final OnCollectibleClickListener onCollectibleClickListener;

    public CollectibleGridAdapter(OnCollectibleClickListener onCollectibleClickListener) {
        this.onCollectibleClickListener = onCollectibleClickListener;
    }

    // Refreshing UI when new collectible is added
    public void submitItems(List<Collectible> collectibles) {
        items.clear();
        items.addAll(collectibles);
        notifyDataSetChanged();
    }

    public void setOwnedCollectibleIds(Set<Integer> ownedIds) {
        ownedCollectibleIds.clear();
        ownedCollectibleIds.addAll(ownedIds);
        notifyDataSetChanged();
    }


    // Creates blank tile
    @NonNull
    @Override
    public CollectibleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_collectible_grid, parent, false);
        return new CollectibleViewHolder(view);
    }

    // Binds the tile to a listener and collectible data
    @Override
    public void onBindViewHolder(@NonNull CollectibleViewHolder holder, int position) {
        Collectible collectible = items.get(position);
        boolean isOwned = ownedCollectibleIds.contains(collectible.getId());
        holder.bind(collectible, isOwned, onCollectibleClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // The tile view holder
    static class CollectibleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;
        private final TextView costView;

        CollectibleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.collectibleImageView);
            nameView = itemView.findViewById(R.id.collectibleNameView);
            costView = itemView.findViewById(R.id.collectibleCostView);
        }

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