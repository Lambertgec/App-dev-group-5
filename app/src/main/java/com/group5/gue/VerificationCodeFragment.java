package com.group5.gue;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.group5.gue.blocking.AppBlockingManager;
import com.group5.gue.data.PermissionHandler;
import com.group5.gue.databinding.FragmentVerificationCodeBinding;
import com.group5.gue.data.friends.FriendsRepository;

import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.Annotation;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for the attendance verification process.
 * <p>
 * For Admins: Provides interface to generate a 6-digit verification code.
 * <p>
 * For Users: Provides an interface to enter the verification code, validates it,
 * and starts app blocking if successful.
 */
public class VerificationCodeFragment extends Fragment {

    /** Name of the SharedPreferences file used for storing lecture-related data. */
    private static final String PREFS_LECTURE = "lecture_prefs";
    /** Key for storing the lecture end time in SharedPreferences. */
    public static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    /** Key for storing the attendance verification status in SharedPreferences. */
    public static final String KEY_CODE_VERIFIED = "attendance_verified";
    /** Key for storing the lecture building in SharedPreferences. */
    public static final String KEY_LECTURE_BUILDING = "lecture_building";
    /** Key for storing the lecture room in SharedPreferences. */
    public static final String KEY_LECTURE_ROOM = "lecture_room";

    /**
     * Required empty public constructor.
     */
    public VerificationCodeFragment() {
    }

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @return A new instance of VerificationCodeFragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_verification_code, container, false);

        // Retrieve current user from the repository to check permissions
        User currentUser = UserRepository.Companion.getInstance().getCachedUser();
        if (currentUser == null) {
            return v;
        }

        // Locate role-specific layouts
        LinearLayout adminLayout = v.findViewById(R.id.adminLayout);
        LinearLayout userLayout = v.findViewById(R.id.userLayout);

        // Toggle visibility based on admin status
        if (currentUser.isAdmin()) {
            adminLayout.setVisibility(View.VISIBLE);
            userLayout.setVisibility(View.GONE);
            setupAdminUI(v);
        } else {
            adminLayout.setVisibility(View.GONE);
            userLayout.setVisibility(View.VISIBLE);
            setupUserUI(v);
        }

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentVerificationCodeBinding binding = FragmentVerificationCodeBinding.bind(view);
        FriendsRepository repository = FriendsRepository.getInstance();

        repository.isAdmin(isAdmin -> {
            if (binding == null) return kotlin.Unit.INSTANCE;

            if (isAdmin) {
                binding.adminLayout.setVisibility(View.VISIBLE);
                binding.userLayout.setVisibility(View.GONE);
                setupAdminUI(view);
            } else {
                binding.userLayout.setVisibility(View.VISIBLE);
                binding.adminLayout.setVisibility(View.GONE);
                setupUserUI(view);
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /**
     * Sets up the UI components for an admin user.
     */
    private void setupAdminUI(View v) {
        final String[] location = {"location"};
        final long[] time = {System.currentTimeMillis()};

        // UI trigger for selecting a building location
        TextView setLocation = v.findViewById(R.id.setLocation);
        setLocation.setOnClickListener(v1 -> {
            // Fetch all annotations (buildings) from the repository
            AnnotationRepository.Companion.getInstance().getAll(annotations -> {
                List<String> locationNames = new ArrayList<>();
                for (Annotation a : annotations) {
                    if (a.getBuilding() != null) locationNames.add(a.getBuilding());
                }
                
                // Show toast if no locations are available for selection
                if (locationNames.isEmpty()) {
                    Toast.makeText(getContext(), "No locations found in repository", Toast.LENGTH_SHORT).show();
                    return kotlin.Unit.INSTANCE;
                }

                // Prepare and show a selection dialog
                String[] locationsArray = locationNames.toArray(new String[0]);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(locationsArray, (dialog, choice) -> {
                    // Update state and UI with chosen location
                    location[0] = locationsArray[choice];
                    setLocation.setText(location[0]);
                });
                builder.show();
                return kotlin.Unit.INSTANCE;
            });
        });

        // Trigger for code generation and display
        TextView codeDisplay = v.findViewById(R.id.codeDisplay);
        codeDisplay.setText("Click to generate attendance code");
        codeDisplay.setOnClickListener(v1 -> codeDisplay.setText(formatCode(generateCode(location[0], time[0]))));

        // UI trigger for selecting the lecture start time
        TextView setTime = v.findViewById(R.id.setTime);
        setTime.setOnClickListener(v1 -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            // Open standard Android TimePicker
            TimePickerDialog mTimePicker = new TimePickerDialog(requireContext(), (timePicker, selectedHour, selectedMinute) -> {
                // Display formatted time back to the user
                setTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                
                // Construct a timestamp for the selected time today
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                time[0] = calendar.getTimeInMillis();
            }, hour, minute, true);
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });
    }

    /**
     * Sets up the UI components for a student/user.
     */
    private void setupUserUI(View v) {
        EditText codeInput = v.findViewById(R.id.codeInput);
        Button verifyButton = v.findViewById(R.id.verifyButton);

        // Verification button click logic
        verifyButton.setOnClickListener(v1 -> {
            final String enteredCode = codeInput.getText().toString();

            // Initialize calendar handler to check for ongoing lectures
            CalendarHandler calendarHandler = new CalendarHandler(requireActivity().getContentResolver());
            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
            
            // Fetch events happening right now
            ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
            if (ongoingEvents.isEmpty()) {
                // If no event is scheduled, verification cannot proceed
                Toast.makeText(getContext(), "No ongoing lecture found in calendar!", Toast.LENGTH_SHORT).show();
                return;
            }

            final Event currentEvent = ongoingEvents.get(0);

            // Ensure location permissions are granted for proximity check
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new PermissionHandler(requireActivity()).requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }

            ProximityChecker proximityChecker = new ProximityChecker(requireContext());
            String[] location = currentEvent.location.split(" ");
            final String building = location[0];
            final String room = location.length > 1 ? location[1] : "";

            proximityChecker.check(building, room, 100.0, null, isNearby -> {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (!isNearby) {
                        Toast.makeText(getContext(), "You are not near the lecture building (" + building + ")", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String expectedCode = formatCode(generateCode(building, currentEvent.startTime));

                    if (enteredCode.equals(expectedCode)) {
                        Toast.makeText(getContext(), "Code verified, app blocking started!", Toast.LENGTH_SHORT).show();

                        requireContext().getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_CODE_VERIFIED, true)
                            .putLong(KEY_LECTURE_END_TIME, currentEvent.endTime)
                            .putString(KEY_LECTURE_BUILDING, building)
                            .putString(KEY_LECTURE_ROOM, room)
                            .apply();

                        // Initialize app blocking logic
                        AppBlockingManager blockingManager = new AppBlockingManager(requireContext());
                        requireContext().getSharedPreferences(AppBlockingManager.PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(AppBlockingManager.KEY_BLOCKING_ENABLED, true)
                                .apply();
                        
                        // Ensure necessary permissions and start the blocking service
                        PermissionHandler permissionHandler = new PermissionHandler(requireActivity());
                        permissionHandler.requestAppBlocking();
                        blockingManager.startBlockingService();

                        // Start background work for attendance checks
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AttendanceCheckWorker.class).build();
                        WorkManager.getInstance(requireContext()).enqueue(workRequest);

                    } else {
                        // Notify user of incorrect code
                        Toast.makeText(getContext(), "Invalid Code!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    /**
     * Generates a 6-digit verification code based on a location and timestamp.
     *
     * @param location Name of building of lecture location
     * @param time Lecture start epoch timestamp.
     * @return An integer representing the generated code.
     */
    public static int generateCode(String location, Long time) {
        // Use part of the timestamp and the hash of the location string
        int a = time.intValue();
        int b = (location != null) ? location.hashCode() : 0;

        // Multiply and take modulo to get a 6-digit range
        int res = (a * b);
        res = res % 1000000;

        // Ensure result is positive
        if (res > 0) {
            return res;
        } else {
            return -res;
        }
    }

    /**
     * Formats the integer code into a 6-character string with leading zeros if necessary.
     *
     * @param code The integer code to format.
     * @return A 6-character string representation of the code.
     */
    private String formatCode(int code) {
        String codeString = String.valueOf(code);
        // Prepend zeros until length is exactly 6
        while (codeString.length() < 6) {
            codeString = "0" + codeString;
        }
        return codeString;
    }
}
