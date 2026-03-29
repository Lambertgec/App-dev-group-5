package com.group5.gue;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.group5.gue.data.friends.FriendsRepository;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.group5.gue.data.PermissionHandler;
import com.group5.gue.databinding.FragmentHomeBinding;
import kotlin.Unit;

/**
 * Fragment representing the primary home screen of the application.
 * This class serves as the main entry point for users after logging in. 
 * 
 * <p>Key functionalities include:
 * <ul>
 *   <li>Distinguishing between Student and Admin views via FriendsRepository.</li>
 *   <li>Handling calendar permission requests and embedding the CalendarFragment.</li>
 *   <li>Nesting the VerificationCodeFragment for attendance tracking.</li>
 *   <li>Navigating to the Collectibles gallery.</li>
 * </ul>
 * </p>
 *
 */
public class HomeFragment extends Fragment {

    /** View binding instance for safe access to the fragment_home layout. */
    private FragmentHomeBinding binding;

    /**
     * Required empty public constructor.
     */
    public HomeFragment() {
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Uses ViewBinding to inflate the layout.
     *
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout using ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the UI has been created. Handles role-based visibility 
     * and sets up fragment navigation.
     *
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup navigation to the Collectibles gallery
        binding.collectiblesButton.setOnClickListener(v -> {
            int containerId = ((View) requireView().getParent()).getId();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(containerId, new CollectiblesGalleryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Use the repository to determine if the logged-in user is an administrator
        FriendsRepository repository = FriendsRepository.getInstance();
        repository.isAdmin(isAdmin -> {
            if (binding == null) return Unit.INSTANCE;

            if (isAdmin) {
                // UI state for Admins
                binding.homeTitle.setText("Admin Dashboard");
                binding.frameLayout.setVisibility(View.GONE);
                binding.showCalendarButton.setVisibility(View.GONE);
            } else {
                // UI state for Students/Users
                binding.homeTitle.setText("Welcome back");
                binding.frameLayout.setVisibility(View.VISIBLE);
                // Execute logic to check permissions and load the calendar
                handleCalendarLogic();
            }

            // VerificationCodeFragment added for both roles
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.verificationContainer, new VerificationCodeFragment())
                    .commitAllowingStateLoss();

            return Unit.INSTANCE;
        });
    }

    /**
     * Manages the logic for displaying the calendar fragment.
     * Checks for calendar permissions and handles the "Show Calendar" button visibility.
     */
    private void handleCalendarLogic() {
        PermissionHandler permissionHandler = new PermissionHandler(requireActivity());

        // Check if READ_CALENDAR permission is already granted
        if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
            // If granted, embed the CalendarFragment immediately
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
            // Hide the prompt button
            binding.showCalendarButton.setVisibility(View.GONE);
        } else {
            // If not granted, show the button to prompt the user
            binding.showCalendarButton.setVisibility(View.VISIBLE);
            binding.showCalendarButton.setOnClickListener(v -> {
                // Trigger the system permission request dialog
                permissionHandler.requestPermission(Manifest.permission.READ_CALENDAR);

                if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
                    binding.showCalendarButton.setVisibility(View.GONE);
                }
            });
        }
    }

    /**
     * Cleans up the binding reference to prevent memory leaks when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
