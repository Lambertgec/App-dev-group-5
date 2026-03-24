package com.group5.gue;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
    private static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    private static final String KEY_ATTENDANCE_VERIFIED = "attendance_verified";

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
            String enteredCode = codeInput.getText().toString();

            CalendarHandler calendarHandler = new CalendarHandler(requireActivity().getContentResolver());
            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
            
            ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
            if (ongoingEvents.isEmpty()) {
                Toast.makeText(getContext(), "No ongoing lecture found in calendar!", Toast.LENGTH_SHORT).show();
                return;
            }

//            timedit never has multiple at the same time so we can disregard extras
            Event currentEvent = ongoingEvents.get(0);
            String expectedCode = formatCode(generateCode(currentEvent.location, currentEvent.startTime));

            if (enteredCode.equals(expectedCode)) {
                Toast.makeText(getContext(), "Code Correct! Apps blocked for the duration of the lecture.", Toast.LENGTH_LONG).show();
                enableAppBlocking(currentEvent.endTime);
            } else {
                Toast.makeText(getContext(), "Invalid Code!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableAppBlocking(long endTime) {
        AppBlockingManager blockingManager = new AppBlockingManager(requireContext());

        requireContext().getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ATTENDANCE_VERIFIED, true)
                .putLong(KEY_LECTURE_END_TIME, endTime)
                .apply();

        blockingManager.startBlockingService();
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
