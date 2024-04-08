package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartupScreen extends AppCompatActivity {

  private FirebaseAuth mAuth;

  // Redirect to another Activity
  public void redirect(Class<?> cls) {
    Intent intent = new Intent(StartupScreen.this, cls);
    startActivity(intent);
  }

  @Override
  public void onStart() {
    super.onStart();

    mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      // User already signed in
      redirect(MainMenu.class);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.startup_screen);
    // Initialize the buttons
    Button signUpBtn = (Button) findViewById(R.id.sign_up_btn);
    Button logInBtn = (Button) findViewById(R.id.log_in_btn);

    // Set the click listeners for the buttons

    signUpBtn.setOnClickListener(
        (v) -> {
          redirect(FirebaseSignIn.class);
        });

    logInBtn.setOnClickListener(
        (v) -> {
          redirect(LogInScreen.class);
        });
  }
}
