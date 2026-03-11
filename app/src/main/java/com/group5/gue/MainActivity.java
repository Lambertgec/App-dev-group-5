package com.group5.gue;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.group5.gue.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        switchFragmant(new HomeFragment());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        

        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
           if ((menuItem.getItemId()) == R.id.home) {
                switchFragmant(new HomeFragment());
           } else if ((menuItem.getItemId()) == R.id.map) {
               switchFragmant(new MapFragment());
           } else if ((menuItem.getItemId()) == R.id.friends) {
               switchFragmant(new FriendsFragment());
           }else {
               switchFragmant(new LeaderboardFragment());
           }
           return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.top_menu, menu);

        MenuItem setting = menu.findItem(R.id.setting);
        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    private void switchFragmant(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_one, fragment);
        fragmentTransaction.commit();
    }
}