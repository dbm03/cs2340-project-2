package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInScreen extends AppCompatActivity {

  private static final String TAG = "EmailPassword";
  private EditText emailEditText;
  private EditText passwordEditText;
  private Button loginButton;
  private FirebaseAuth mAuth;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.log_in_screen);

    mAuth = FirebaseAuth.getInstance();

    emailEditText = findViewById(R.id.emailEditText);
    passwordEditText = findViewById(R.id.passwordEditText);
    loginButton = findViewById(R.id.loginButton);

    loginButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
              signIn(email, password);
            } else {
              Toast.makeText(
                      LogInScreen.this, "Please enter email and password", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }

  private void signIn(String email, String password) {
    mAuth
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(
            this,
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  Intent intent = new Intent(LogInScreen.this, MainMenu.class);
                  // If the user was already signed in, isUserAlreadySignedIn will be true
                  intent.putExtra("justSignedIn", true);
                  startActivity(intent);
                } else {
                  Toast.makeText(
                          LogInScreen.this, "Incorrect email or password.", Toast.LENGTH_SHORT)
                      .show();
                }
              }
            });
  }

  public void redirect(Class<?> cls) {
    Intent intent = new Intent(LogInScreen.this, cls);
    startActivity(intent);
  }
}
