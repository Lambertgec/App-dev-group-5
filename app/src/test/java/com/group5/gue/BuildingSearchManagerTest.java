package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.widget.AutoCompleteTextView;

import androidx.test.core.app.ApplicationProvider;

import com.group5.gue.data.model.Annotation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class BuildingSearchManagerTest {

    private BuildingSearchManager searchManager;
    private AutoCompleteTextView searchView;
    private List<Annotation> annotations;
    private boolean buildingSelectedCalled = false;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        searchView = new AutoCompleteTextView(context);
        searchManager = new BuildingSearchManager(searchView, () -> buildingSelectedCalled = true);

        annotations = new ArrayList<>();
        annotations.add(new Annotation(1L, "now", "Atlas", null, null, 51.447, 5.484, "admin"));
        annotations.add(new Annotation(2L, "now", "Atlas", "1.100", "1", 51.447, 5.484, "admin"));
        annotations.add(new Annotation(3L, "now", "Flux", null, null, 51.448, 5.485, "admin"));
        annotations.add(new Annotation(4L, "now", "Flux", null, null, 51.448, 5.485, "admin")); // Duplicate building name
    }

    @Test
    public void testSetAnnotationsFiltersBuildings() {
        searchManager.setAnnotations(annotations);
        
        assertEquals(2, searchView.getAdapter().getCount());
        assertEquals("Atlas", searchView.getAdapter().getItem(0));
        assertEquals("Flux", searchView.getAdapter().getItem(1));
    }

    @Test
    public void testSetAnnotationsEmptyList() {
        searchManager.setAnnotations(new ArrayList<>());
        assertTrue(searchView.getAdapter() == null || searchView.getAdapter().getCount() == 0);
    }
}
