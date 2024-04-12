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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ModifyUser extends AppCompatActivity {

  private EditText nameEditText;
  private EditText passwordText;
  private Button applyNameButton;
  private Button applyPasswordButton;
  private Button cancelButton;
  private Button deleteButton;
  private FirebaseAuth mAuth;
  FirebaseUser user;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.modify_user_data);

    mAuth = FirebaseAuth.getInstance();
    user = mAuth.getCurrentUser();

    nameEditText = findViewById(R.id.nameEditText);
    passwordText = findViewById(R.id.passwordModifyText);
    applyNameButton = findViewById(R.id.applyNameButton);
    applyPasswordButton = findViewById(R.id.applyPasswordButton);
    cancelButton = findViewById(R.id.cancelButton);
    deleteButton = findViewById(R.id.deleteAccButton);

    applyNameButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String name = nameEditText.getText().toString().trim();
            if (!name.isEmpty()) {
              if (user != null) {
                UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                              Toast.makeText(
                                      ModifyUser.this,
                                      "Could not update your name please try again",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            } else {
                              Toast.makeText(ModifyUser.this, "Name updated", Toast.LENGTH_SHORT)
                                  .show();
                            }
                          }
                        });
              }
            } else {
              Toast.makeText(ModifyUser.this, "Please choose a new name", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });

    applyPasswordButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String password = passwordText.getText().toString();
            if (!password.isEmpty()) {
              if (user != null) {
                user.updatePassword(password)
                    .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                              Toast.makeText(
                                      ModifyUser.this,
                                      "Could not update your password please try again",
                                      Toast.LENGTH_SHORT)
                                  .show();
                            } else {
                              Toast.makeText(
                                      ModifyUser.this, "Password updated", Toast.LENGTH_SHORT)
                                  .show();
                            }
                          }
                        });
              }
            } else {
              Toast.makeText(ModifyUser.this, "Please choose a new password", Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });

    cancelButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(ModifyUser.this, MainMenu.class);
            // If the user was already signed in, isUserAlreadySignedIn will be true
            intent.putExtra("justSignedIn", true);
            startActivity(intent);
          }
        });

    deleteButton.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mAuth.signOut();
            user.delete()
                .addOnCompleteListener(
                    new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                          Toast.makeText(
                                  ModifyUser.this,
                                  "Could not delete your account",
                                  Toast.LENGTH_SHORT)
                              .show();
                          System.out.println(task.getException().toString());
                        } else {
                          redirect(StartupScreen.class);
                        }
                      }
                    });
          }
        });
  }

  public void redirect(Class<?> cls) {
    Intent intent = new Intent(ModifyUser.this, cls);
    startActivity(intent);
  }
}
