package com.team4.spotifywrapped;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainMenu extends AppCompatActivity {

  public static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";
  public static final String REDIRECT_URI = "spotifysdk://auth";

  public static final int AUTH_TOKEN_REQUEST_CODE = 0;
  public static final int AUTH_CODE_REQUEST_CODE = 1;

  private final OkHttpClient mOkHttpClient = new OkHttpClient();
  private String mAccessToken, mAccessCode;
  private Call mCall;

  private TextView tokenTextView, codeTextView, profileTextView;

  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_menu);

    mAuth = FirebaseAuth.getInstance();

    // Default to false if the extra is not present
    boolean justSignedIn = getIntent().getBooleanExtra("justSignedIn", false);
    if (!justSignedIn) {
      greet_user();
      // Show your modal or Toast here
    }

    // Initialize the views
    tokenTextView = (TextView) findViewById(R.id.token_text_view);
    codeTextView = (TextView) findViewById(R.id.code_text_view);
    profileTextView = (TextView) findViewById(R.id.response_text_view);

    // Initialize the buttons
    Button tokenBtn = (Button) findViewById(R.id.token_btn);
    Button codeBtn = (Button) findViewById(R.id.code_btn);
    Button profileBtn = (Button) findViewById(R.id.profile_btn);

    // Set the click listeners for the buttons

    tokenBtn.setOnClickListener(
        (v) -> {
          getToken();
        });

    codeBtn.setOnClickListener(
        (v) -> {
          getCode();
        });

    profileBtn.setOnClickListener(
        (v) -> {
          onGetUserProfileClicked();
        });
  }

  @Override
  public void onStart() {
    super.onStart();

    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      Log.d(
          "MainMenuStart",
          "currentUser:" + currentUser.getDisplayName() + " email:" + currentUser.getEmail());
      // User already signed in
    }
  }

  protected void greet_user() {
    FirebaseUser user = mAuth.getCurrentUser();
    // The user has just signed in, show welcome back message
    if (user == null) {
      return;
    }
    String userName = "";
    userName = user.getDisplayName();
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

  /**
   * Get token from Spotify This method will open the Spotify login activity and get the token What
   * is token? https://developer.spotify.com/documentation/general/guides/authorization-guide/
   */
  public void getToken() {
    final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
    AuthorizationClient.openLoginActivity(MainMenu.this, AUTH_TOKEN_REQUEST_CODE, request);
  }

  /**
   * Get code from Spotify This method will open the Spotify login activity and get the code What is
   * code? https://developer.spotify.com/documentation/general/guides/authorization-guide/
   */
  public void getCode() {
    final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
    AuthorizationClient.openLoginActivity(MainMenu.this, AUTH_CODE_REQUEST_CODE, request);
  }

  public void signUpSpotifyWrappedAccount(String email, String id) {
    String TAG = "SpotifyWrapped Sign Up";
    mAuth
        .createUserWithEmailAndPassword(email, id)
        .addOnCompleteListener(
            this,
            new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  Log.d(
                      TAG, "Created user with " + email + " and password: " + id + " successfully");
                }
              }
            });
  }

  /**
   * When the app leaves this activity to momentarily get a token/code, this function fetches the
   * result of that external activity to get the response from Spotify
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    System.out.println("HELLO" + data);
    final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

    // Check which request code is present (if any)
    if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
      mAccessToken = response.getAccessToken();
      setTextAsync(mAccessToken, tokenTextView);

    } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
      mAccessCode = response.getCode();
      setTextAsync(mAccessCode, codeTextView);
    }
  }

  /** Get user profile This method will get the user profile using the token */
  public void onGetUserProfileClicked() {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
      return;
    }

    // Create a request to get the user profile
    final Request request =
        new Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer " + mAccessToken)
            .build();

    cancelCall();
    mCall = mOkHttpClient.newCall(request);

    mCall.enqueue(
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            Log.d("HTTP", "Failed to fetch data: " + e);
            Toast.makeText(
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              setTextAsync(jsonObject.toString(3), profileTextView);
              String email = (String) jsonObject.get("email");
              String id = (String) jsonObject.get("id");
              signUpSpotifyWrappedAccount(email, id);
            } catch (JSONException e) {
              Log.d("JSON", "Failed to parse data: " + e);
              Toast.makeText(
                      MainMenu.this,
                      "Failed to parse data, watch Logcat for more details",
                      Toast.LENGTH_SHORT)
                  .show();
            }
          }
        });
  }

  /**
   * Creates a UI thread to update a TextView in the background Reduces UI latency and makes the
   * system perform more consistently
   *
   * @param text the text to set
   * @param textView TextView object to update
   */
  private void setTextAsync(final String text, TextView textView) {
    runOnUiThread(() -> textView.setText(text));
  }

  /**
   * Get authentication request
   *
   * @param type the type of the request
   * @return the authentication request
   */
  private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
    return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
        .setShowDialog(false)
        .setScopes(
            new String[] {"user-read-email"}) // <--- Change the scope of your requested token here
        .setCampaign("your-campaign-token")
        .build();
  }

  /**
   * Gets the redirect Uri for Spotify
   *
   * @return redirect Uri object
   */
  private Uri getRedirectUri() {
    return Uri.parse(REDIRECT_URI);
  }

  private void cancelCall() {
    if (mCall != null) {
      mCall.cancel();
    }
  }

  @Override
  protected void onDestroy() {
    cancelCall();
    super.onDestroy();
  }
}
