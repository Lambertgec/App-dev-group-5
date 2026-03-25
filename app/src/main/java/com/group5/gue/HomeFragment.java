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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.collectiblesButton.setOnClickListener(v -> {
            int containerId = ((View) requireView().getParent()).getId();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(containerId, new CollectiblesGalleryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        FriendsRepository repository = FriendsRepository.getInstance();

        repository.isAdmin(isAdmin -> {
            if (binding == null) return Unit.INSTANCE;
            Button collectiblesButton = view.findViewById(R.id.collectiblesButton);
            FragmentTransaction tr = getChildFragmentManager().beginTransaction();

            Fragment collectFragment = new CollectFragment();
            tr.add(R.id.full_view, collectFragment).hide(collectFragment);


            if (isAdmin) {
                binding.homeTitle.setVisibility(View.VISIBLE);
                binding.homeTitle.setText("Admin Dashboard");

                Fragment verificationCode = new VerificationCodeFragment();
                tr.add(R.id.full_view, verificationCode).show(verificationCode).commit();

                binding.frameLayout.setVisibility(View.GONE);
                binding.showCalendarButton.setVisibility(View.GONE);
            } else {
                binding.homeTitle.setVisibility(View.VISIBLE);
                binding.homeTitle.setText("Welcome back");

                binding.frameLayout.setVisibility(View.VISIBLE);

                handleCalendarLogic();
            }

            return Unit.INSTANCE;
        });
    }

    private void handleCalendarLogic() {
        PermissionHandler permissionHandler = new PermissionHandler(requireActivity());

        if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
            binding.showCalendarButton.setVisibility(View.GONE);
        } else {
            binding.showCalendarButton.setVisibility(View.VISIBLE);
            binding.showCalendarButton.setOnClickListener(v -> {
                permissionHandler.requestPermission(Manifest.permission.READ_CALENDAR);

                if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
                    binding.showCalendarButton.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}