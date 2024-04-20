package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

/**
 * The MainMenu class represents the main activity of the Spotify Wrapped application.
 * It provides functionality for managing user authentication, displaying user information,
 * navigating between different fragments, and handling user interactions.
 */
public class MainMenu extends AppCompatActivity {

    private String mAccessToken;
    private ArrayList<String> top5Songs = new ArrayList<>();

    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    /**
     * Called when the activity is first created. Initializes the activity,
     * sets up the bottom navigation view, and greets the user.
     *
     * @param savedInstanceState Bundle containing the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_menu);
        setupBottomNavigationView();

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(R.id.navigation_wrapped);

        Intent intent = MainMenu.this.getIntent();
        boolean justSignedIn = intent != null && intent.getBooleanExtra("justSignedIn", false);

        if (!justSignedIn) {
            greet_user();
        }
    }

    /**
     * Greets the user with a welcome message.
     */
    protected void greet_user() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        String userName = user.getDisplayName();

        if (userName != null && userName.isEmpty()) {
            userName = user.getEmail();
        }

        if (userName == null) {
            userName = "";
        }


        new AlertDialog.Builder(MainMenu.this)
                .setTitle("Welcome Back!")
                .setMessage("You are signed in as " + userName)
                .setPositiveButton("Continue", (dialog, which) -> {})
                .setNegativeButton("Change User", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(MainMenu.this, StartupScreen.class);
                    startActivity(intent);
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }


    /**
     * Sets up the bottom navigation view.
     */
    private void setupBottomNavigationView() {
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_wrapped) {
                selectedFragment = new WrappedFragment();
            } else if (item.getItemId() == R.id.navigation_games) {
                selectedFragment = new GamesFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
            return false;
        });

        navView.setSelectedItemId(R.id.navigation_wrapped);
    }

    /**
     * Handles the result of an external activity, such as obtaining an access token or code.
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the intent data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        if (WrappedFragment.AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            Log.d("WrappedFragment", "Access token received: " + response.getAccessToken());
            setTextAsync("You successfully logged in!", findViewById(R.id.token_text_view));
        } else if (WrappedFragment.AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessToken = response.getCode();
        }

    }

    /**
     * Retrieves the top 5 songs.
     *
     * @return ArrayList containing the top 5 songs
     */
    public ArrayList<String> getTop5Songs() {
        return new ArrayList<>(top5Songs);
    }

    /**
     * Sets the top 5 songs.
     *
     * @param songs ArrayList containing the top 5 songs
     */
    public void setTop5Songs(ArrayList<String> songs) {
        this.top5Songs = new ArrayList<>(songs); // Store a copy to prevent external changes
    }

    /**
     * Retrieves the OkHttpClient instance.
     *
     * @return OkHttpClient instance
     */
    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    /**
     * Retrieves the access token.
     *
     * @return String representing the access token
     */
    public String getTokenAccess() {
        return mAccessToken;
    }

    /**
     * Sets the access token.
     *
     * @param token String representing the access token
     */
    public void setAccessToken(String token) {
        this.mAccessToken = token;
    }

    /**
     * Called when the activity is about to become visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Called when the activity is about to be destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }

    /**
     * Creates a UI thread to update a TextView in the background Reduces UI latency and makes the
     * system perform more consistently
     *
     * @param text     the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }


}
