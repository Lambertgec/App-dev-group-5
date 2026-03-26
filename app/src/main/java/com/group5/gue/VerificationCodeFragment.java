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
 * A simple {@link Fragment} subclass.
 * Use the {@link VerificationCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationCodeFragment extends Fragment {

    private static final String PREFS_LECTURE = "lecture_prefs";
    public static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    public static final String KEY_CODE_VERIFIED = "attendance_verified";
    public static final String KEY_LECTURE_BUILDING = "lecture_building";
    public static final String KEY_LECTURE_ROOM = "lecture_room";

    public VerificationCodeFragment() {
        // Required empty public constructor
    }

    public static VerificationCodeFragment newInstance() {
        return new VerificationCodeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_verification_code, container, false);

        User currentUser = UserRepository.Companion.getInstance().getCachedUser();
        if (currentUser == null) {
            return v;
        }

        LinearLayout adminLayout = v.findViewById(R.id.adminLayout);
        LinearLayout userLayout = v.findViewById(R.id.userLayout);

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

    private void setupAdminUI(View v) {
        final String[] location = {"location"};
        final long[] time = {System.currentTimeMillis()};

        TextView setLocation = v.findViewById(R.id.setLocation);
        setLocation.setOnClickListener(v1 -> {
            AnnotationRepository.Companion.getInstance().getAll(annotations -> {
                List<String> locationNames = new ArrayList<>();
                for (Annotation a : annotations) {
                    if (a.getBuilding() != null) locationNames.add(a.getBuilding());
                }
                
                if (locationNames.isEmpty()) {
                    Toast.makeText(getContext(), "No locations found in repository", Toast.LENGTH_SHORT).show();
                    return kotlin.Unit.INSTANCE;
                }

                String[] locationsArray = locationNames.toArray(new String[0]);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(locationsArray, (dialog, choice) -> {
                    location[0] = locationsArray[choice];
                    setLocation.setText(location[0]);
                });
                builder.show();
                return kotlin.Unit.INSTANCE;
            });
        });

        TextView codeDisplay = v.findViewById(R.id.codeDisplay);
        codeDisplay.setText("Click to generate attendance code");
        codeDisplay.setOnClickListener(v1 -> codeDisplay.setText(formatCode(generateCode(location[0], time[0]))));

        TextView setTime = v.findViewById(R.id.setTime);
        setTime.setOnClickListener(v1 -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker = new TimePickerDialog(requireContext(), (timePicker, selectedHour, selectedMinute) -> {
                setTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
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

    private void setupUserUI(View v) {
        EditText codeInput = v.findViewById(R.id.codeInput);
        Button verifyButton = v.findViewById(R.id.verifyButton);

        verifyButton.setOnClickListener(v1 -> {
            String enteredCode = "";
            enteredCode = codeInput.getText().toString();

            CalendarHandler calendarHandler = new CalendarHandler(requireActivity().getContentResolver());
            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
            
            ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
            if (ongoingEvents.isEmpty()) {
                Toast.makeText(getContext(), "No ongoing lecture found in calendar!", Toast.LENGTH_SHORT).show();
                return;
            }

            Event currentEvent = ongoingEvents.get(0);
            
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new PermissionHandler(requireActivity()).requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }

            ProximityChecker proximityChecker = new ProximityChecker(requireContext());
            String[] location = currentEvent.location.split(" ");
            String building = location[0];
            String room = location.length > 1 ? location[1] : "";

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

                        AppBlockingManager blockingManager = new AppBlockingManager(requireContext());
                        requireContext().getSharedPreferences(AppBlockingManager.PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(AppBlockingManager.KEY_BLOCKING_ENABLED, true)
                                .apply();
                        
                        PermissionHandler permissionHandler = new PermissionHandler(requireActivity());
                        permissionHandler.requestAppBlocking();
                        blockingManager.startBlockingService();

                        AttendanceCheckWorker.oneTimeWork(requireContext());
                    } else {
                        Toast.makeText(getContext(), "Invalid Code!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    public static int generateCode(String location, Long time) {
        int a = time.intValue();
        int b = (location != null) ? location.hashCode() : 0;

        int res = (a * b);
        res = res % 1000000;

        if (res > 0) {
            return res;
        } else {
            return -res;
        }
    }

    private String formatCode(int code) {
        String codeString = String.valueOf(code);
        while (codeString.length() < 6) {
            codeString = "0" + codeString;
        }
        return codeString;
    }
}
