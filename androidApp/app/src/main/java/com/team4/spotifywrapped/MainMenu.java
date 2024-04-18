package com.team4.spotifywrapped;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainMenu extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_menu);
    setupBottomNavigationView();

    // Set the default selected item in BottomNavigationView
    BottomNavigationView navView = findViewById(R.id.bottom_navigation);
    navView.setSelectedItemId(R.id.navigation_wrapped); // Set the Wrapped screen as default
  }

  private void setupBottomNavigationView() {
    BottomNavigationView navView = findViewById(R.id.bottom_navigation);
    navView.setOnNavigationItemSelectedListener(
        item -> {
          Fragment selectedFragment = null;

          // Using if-else to handle fragment switching
          if (item.getItemId() == R.id.navigation_wrapped) {
            selectedFragment = new WrappedFragment();
          } else if (item.getItemId() == R.id.navigation_games) {
            selectedFragment = new GamesFragment(); // Assume you have a GamesFragment
          } else if (item.getItemId() == R.id.navigation_profile) {
            selectedFragment = new ProfileFragment(); // Assume you have a ProfileFragment
          }

          if (selectedFragment != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();
            return true; // return true to display the item as the selected item
          }
          return false; // return false to not select the item
        });

    // Manually trigger the first selection
    navView.setSelectedItemId(
        R.id.navigation_wrapped); // This will load the WrappedFragment by default
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Optional: Authentication check logic here if needed
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Clean up resources if needed
  }
}
