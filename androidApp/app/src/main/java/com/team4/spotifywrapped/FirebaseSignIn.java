package com.team4.spotifywrapped;

import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

// Assuming this is within your Activity class
public class FirebaseSignIn extends AppCompatActivity {

  private FirebaseAuth firebaseAuth;
  private FirebaseAuth.AuthStateListener listener;
  private final int AUTH_REQUEST_CODE =
      369420; // If needed, though not directly used with the new API
  private List<AuthUI.IdpConfig> providers; // Initialize this with your providers
  private ActivityResultLauncher<Intent> signInLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.firebase_sign_in);

    // Initialize FirebaseAuth
    firebaseAuth = FirebaseAuth.getInstance();

    // Setup providers
    providers =
        Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
            // Add more providers as needed
            );

    // Register the activity result callback
    signInLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
              @Override
              public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                  // Successfully signed in, changing to Main activity
                  Intent intent = new Intent(FirebaseSignIn.this, MainMenu.class);
                  startActivity(intent);
                } else {
                  // TODO: Sign in failed. If response is null the user canceled the sign-in flow
                  // using the back button.
                  // Handle the error
                }
              }
            });

    listener =
        new FirebaseAuth.AuthStateListener() {
          @Override
          public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
              Intent intent = new Intent(FirebaseSignIn.this, MainMenu.class);
              // If the user was already signed in, isUserAlreadySignedIn will be true
              intent.putExtra("justSignedIn", true);
              startActivity(intent);
              finish(); // Close the current activity
            } else {
              // Use the launcher to start the sign-in flow instead of startActivityForResult
              Intent signInIntent =
                  AuthUI.getInstance()
                      .createSignInIntentBuilder()
                      .setAvailableProviders(providers)
                      .setTheme(R.style.Theme_SpotifyWrapped)
                      .setLogo(R.drawable.logo)
                      .build();
              signInLauncher.launch(signInIntent);
            }
          }
        };
  }

  @Override
  protected void onStart() {
    super.onStart();
    firebaseAuth.addAuthStateListener(listener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (listener != null) {
      firebaseAuth.removeAuthStateListener(listener);
    }
  }
}
