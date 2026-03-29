package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.group5.gue.data.model.Collectible;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class CollectibleGridAdapterTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void submitItems_updatesItemCount() {
        CollectibleGridAdapter adapter = new CollectibleGridAdapter(collectible -> {
        });

        assertEquals(0, adapter.getItemCount());

        adapter.submitItems(Arrays.asList(
            collectible(1, "Alpha", 10, "https://example.com/alpha.png"),
            collectible(2, "Beta", 20, "https://example.com/beta.png")
        ));

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void onCreateViewHolder_inflatesExpectedViews() {
        CollectibleGridAdapter adapter = new CollectibleGridAdapter(collectible -> {
        });
        FrameLayout parent = new FrameLayout(context);

        CollectibleGridAdapter.CollectibleViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        assertNotNull(holder.itemView.findViewById(R.id.collectibleImageView));
        assertNotNull(holder.itemView.findViewById(R.id.collectibleNameView));
        assertNotNull(holder.itemView.findViewById(R.id.collectibleCostView));
    }

    @Test
    public void onBindViewHolder_ownedCollectible_bindsTextAndClickCallback() {
        AtomicReference<Collectible> clicked = new AtomicReference<>();
        CollectibleGridAdapter adapter = new CollectibleGridAdapter(clicked::set);
        Collectible owned = collectible(7, "Legend", 30, "https://example.com/legend.png");

        adapter.submitItems(Collections.singletonList(owned));
        adapter.setOwnedCollectibleIds(new HashSet<>(Collections.singletonList(7)));

        CollectibleGridAdapter.CollectibleViewHolder holder =
            adapter.onCreateViewHolder(new FrameLayout(context), 0);

        adapter.onBindViewHolder(holder, 0);

        TextView nameView = holder.itemView.findViewById(R.id.collectibleNameView);
        TextView costView = holder.itemView.findViewById(R.id.collectibleCostView);
        ImageView imageView = holder.itemView.findViewById(R.id.collectibleImageView);

        assertEquals("Legend", nameView.getText().toString());
        assertEquals("Cost: 30 points", costView.getText().toString());
        assertNotNull(imageView);

        holder.itemView.performClick();
        assertSame(owned, clicked.get());
    }

    @Test
    public void onBindViewHolder_unownedCollectible_bindsDefaultBranchAndClickCallback() {
        AtomicReference<Collectible> clicked = new AtomicReference<>();
        CollectibleGridAdapter adapter = new CollectibleGridAdapter(clicked::set);
        Collectible unowned = collectible(8, "Mystery", 5, null);

        adapter.submitItems(Collections.singletonList(unowned));
        adapter.setOwnedCollectibleIds(Collections.emptySet());

        CollectibleGridAdapter.CollectibleViewHolder holder =
            adapter.onCreateViewHolder(new FrameLayout(context), 0);

        adapter.onBindViewHolder(holder, 0);

        TextView nameView = holder.itemView.findViewById(R.id.collectibleNameView);
        TextView costView = holder.itemView.findViewById(R.id.collectibleCostView);
        ImageView imageView = holder.itemView.findViewById(R.id.collectibleImageView);

        assertEquals("Mystery", nameView.getText().toString());
        assertEquals("Cost: 5 points", costView.getText().toString());
        assertNotNull(imageView);

        holder.itemView.performClick();
        assertSame(unowned, clicked.get());
    }

    @Test
    public void setOwnedCollectibleIds_acceptsReplacementSetsWithoutChangingItemCount() {
        CollectibleGridAdapter adapter = new CollectibleGridAdapter(collectible -> {
        });
        adapter.submitItems(Arrays.asList(
            collectible(1, "A", 1, null),
            collectible(2, "B", 2, null),
            collectible(3, "C", 3, null)
        ));

        adapter.setOwnedCollectibleIds(new HashSet<>(Arrays.asList(1, 3)));
        assertEquals(3, adapter.getItemCount());

        adapter.setOwnedCollectibleIds(new HashSet<>(Collections.singletonList(2)));
        assertEquals(3, adapter.getItemCount());
    }

    private static Collectible collectible(int id, String name, int score, String imageUrl) {
        return new Collectible(
            id,
            "creator",
            name,
            score,
            "description",
            "2026-01-01T00:00:00Z",
            imageUrl
        );
    }
}
