package com.group5.gue;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.group5.gue.data.auth.AuthRepository;
import com.group5.gue.data.model.Role;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VerificationCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerificationCodeFragment extends Fragment {

    public VerificationCodeFragment() {
        // Required empty public constructor
    }

    public static VerificationCodeFragment newInstance() {
        VerificationCodeFragment fragment = new VerificationCodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_verification_code, container, false);

        Role role = AuthRepository.getInstance(getActivity()).getCachedUser().getRole();
        if (role == Role.ADMIN) {
//            allow to pick location, then show code
        } else {
//            user has to enter code, then add pts and block apps
        }

        final String[] location = {"location"};
        final long[] time = {System.currentTimeMillis()};


        TextView setLocation = v.findViewById(R.id.setLocation);
        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TODO: fetch real list of options
                final String[] locations = {
                        "Atlas", "Auditorium", "Metaforum"
                };

//                show popup to let admin chose location
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setItems(locations, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        location[0] = locations[choice];
                        setLocation.setText(location[0]);
                    }
                });
                builder.show();
            }
        });

        TextView codeDisplay = v.findViewById(R.id.codeDisplay);
        codeDisplay.setText("Click to generate attendance code");

        codeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                codeDisplay.setText(formatCode(generateCode(location[0], time[0])));
            }
        });

        TextView setTime = v.findViewById(R.id.setTime);
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        setTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        time[0] = calendar.getTimeInMillis();
                    }
                }, hour, minute, true); // 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        return v;
    }

    public static int generateCode(String location, Long time) {
        int a = time.intValue();
        int b = location.hashCode();

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
