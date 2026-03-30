package com.group5.gue.data.attendance;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.group5.gue.data.model.AttendanceRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AttendanceRepositoryTest {

    private AttendanceRepository repository;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        repository = AttendanceRepository.getInstance(context);
    }

    @Test
    public void testInstance() {
        assertNotNull(repository);
        AttendanceRepository second = AttendanceRepository.getInstance(context);
        assertSame(repository, second);
    }

    @Test
    public void testSaveAndRetrieve() {
        AttendanceRecord record = new AttendanceRecord("Test Event", "Building 1", "Room 101", System.currentTimeMillis());
        boolean success = repository.saveIfNotDuplicate(record);
        assertTrue(success);

        List<AttendanceRecord> records = repository.getAll();
        boolean found = false;
        for (AttendanceRecord r : records) {
            if (r.getEventName().equals("Test Event")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testDuplicate() {
        long time = System.currentTimeMillis();
        AttendanceRecord record1 = new AttendanceRecord("Unique", "B", "R", time);
        AttendanceRecord record2 = new AttendanceRecord("Unique", "B", "R", time);

        repository.saveIfNotDuplicate(record1);
        boolean secondSave = repository.saveIfNotDuplicate(record2);
        assertFalse(secondSave);
    }
}
