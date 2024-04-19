package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class MainMenu extends AppCompatActivity {

  private String mAccessToken;

  private ArrayList<String> top5Songs = new ArrayList<>();

  public ArrayList<String> getTop5Songs() {
    return new ArrayList<>(top5Songs); // Return a copy to prevent direct modification
  }

  public void setTop5Songs(ArrayList<String> songs) {
    this.top5Songs = new ArrayList<>(songs); // Store a copy to prevent external changes
  }

  public void addSongToTop5(String song) {
    if (top5Songs.size() < 5) {
      top5Songs.add(song);
    } else {
      Log.e("MainMenu", "Already have 5 top songs");
    }
  }

  private OkHttpClient mOkHttpClient = new OkHttpClient();

  public OkHttpClient getOkHttpClient() {
    return mOkHttpClient;
  }

  public String getAccessToken() {
    return mAccessToken;
  }

  public void setAccessToken(String token) {
    this.mAccessToken = token;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_menu);
    setupBottomNavigationView();

    // Set the default selected item in BottomNavigationView
    BottomNavigationView navView = findViewById(R.id.bottom_navigation);
    navView.setSelectedItemId(R.id.navigation_wrapped); // Set the Wrapped screen as default

    Intent intent = MainMenu.this != null ? MainMenu.this.getIntent() : null;
    boolean justSignedIn = intent != null && intent.getBooleanExtra("justSignedIn", false);

    if (!justSignedIn) {
      greet_user();
    }
  }

  protected void greet_user() {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    // The user has just signed in, show welcome back message
    if (user == null) {
      return;
    }
    String userName = user.getDisplayName();
    if (userName != null && userName.isEmpty()) {
      userName = user.getEmail();
    }
    if (userName == null) userName = "";

    new AlertDialog.Builder(MainMenu.this)
        .setTitle("Welcome Back!")
        .setMessage("You are signed in as " + userName)
        .setPositiveButton("Continue", (dialog, which) -> {})
        .setNegativeButton(
            "Change User",
            (dialog, which) -> {
              // User chooses to change user, sign out and start the sign-in flow again
              mAuth.signOut();
              Intent intent = new Intent(MainMenu.this, StartupScreen.class);
              startActivity(intent);
            })
        .setIcon(android.R.drawable.ic_dialog_info)
        .show();
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
