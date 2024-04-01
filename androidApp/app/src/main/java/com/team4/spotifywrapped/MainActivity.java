package com.team4.spotifywrapped;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

  public static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";
  public static final String REDIRECT_URI = "spotifysdk://auth";

  public static final int AUTH_TOKEN_REQUEST_CODE = 0;
  public static final int AUTH_CODE_REQUEST_CODE = 1;

  private final OkHttpClient mOkHttpClient = new OkHttpClient();
  private String mAccessToken, mAccessCode;
  private Call mCall;

  private TextView tokenTextView, codeTextView, profileTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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

  /**
   * Get token from Spotify This method will open the Spotify login activity and get the token What
   * is token? https://developer.spotify.com/documentation/general/guides/authorization-guide/
   */
  public void getToken() {
    final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
    AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
  }

  /**
   * Get code from Spotify This method will open the Spotify login activity and get the code What is
   * code? https://developer.spotify.com/documentation/general/guides/authorization-guide/
   */
  public void getCode() {
    final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
    AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_CODE_REQUEST_CODE, request);
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

  public ArrayList<String> parseObjects(JSONObject json_value, String key){
    ArrayList<String> hash_vals = new ArrayList<>();
    try {
      JSONArray items = (JSONArray) json_value.get("items");

      for (int i = 0; i < items.length(); i++){
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        hash_vals.add(items.get(i).get(key));

      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
                      MainActivity.this,
                      "Failed to parse data, watch Logcat for more details",
                      Toast.LENGTH_SHORT)
              .show();
    }

    return hash_vals;

  }
  public JSONObject spotifyRequest(String url_parameter){
    final JSONObject jsonObjectReturnNull = new JSONObject();
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
      return jsonObjectReturnNull;
    }

    // Create a request to get the user profile
    final Request request =
            new Request.Builder()
                    .url(url_parameter)
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
                                MainActivity.this,
                                "Failed to fetch data, watch Logcat for more details",
                                Toast.LENGTH_SHORT)
                        .show();
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                try {
                  final JSONObject jsonObject = new JSONObject(response.body().string());
                  setTextAsync(jsonObject.toString(3), profileTextView);
                } catch (JSONException e) {
                  Log.d("JSON", "Failed to parse data: " + e);
                  Toast.makeText(
                                  MainActivity.this,
                                  "Failed to parse data, watch Logcat for more details",
                                  Toast.LENGTH_SHORT)
                          .show();
                }
              }
            });
    return jsonObjectReturnNull;
  }
  /** Get user profile This method will get the user profile using the token */
  public JSONObject onGetUserProfileClicked() {
    String url = "https://api.spotify.com/v1/me";
    return spotifyRequest(url);
  }


  public JSONObject onGetUserMostListenSongs(String timeFrame) {
    String url = "https://api.spotify.com/v1/me/top/tracks?time_range=" + timeFrame + "&limit=5&offset=0";
    return spotifyRequest(url);
  }

  public JSONObject onGetUserMostListenArtists(String timeFrame) {
    String url = "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&limit=5&offset=0";
    return spotifyRequest(url);
  }

  public LinkedHashMap<String, Integer> onGetUserMostListenGenres(String timeFrame) {
    String url = "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&limit=5&offset=0";
    JSONObject top_artists = spotifyRequest(url);
    HashMap<String, Integer> genres = new HashMap<>();
    try {
      JSONArray items = top_artists.getJSONArray("items");

      for (int i = 0; i < items.length(); i++){
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject artist = items.getJSONObject(i);
        JSONArray genre_aux = artist.getJSONArray("genres");

        // Convertir JSONArray a ArrayList<String>
        ArrayList<String> artist_genres = new ArrayList<>();
        for (int j = 0; j < genre_aux.length(); j++) {
          artist_genres.add(genre_aux.getString(j));
        }
        for (int j = 0; j < artist_genres.size(); j++){
          if (genres.containsKey(artist_genres.get(j))){
            int num_rep = genres.get(artist_genres.get(j));
            genres.put(artist_genres.get(j), num_rep + 1);
          }
          else{
            genres.put(artist_genres.get(j), 1);
          }
        }
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
                      MainActivity.this,
                      "Failed to parse data, watch Logcat for more details",
                      Toast.LENGTH_SHORT)
              .show();
    }
    List<Map.Entry<String, Integer>> list = new ArrayList<>(genres.entrySet());

    list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

    int limit = Math.min(list.size(), 5);

    LinkedHashMap<String, Integer> top5 = new LinkedHashMap<>();

    for (int i = 0; i < limit; i++) {
      Map.Entry<String, Integer> entry = list.get(i);
      top5.put(entry.getKey(), entry.getValue());
    }

    return top5;

  }

  public JSONObject getRecommendations(){
    JSONObject songs_recommendation = onGetUserMostListenSongs("long_term");
    JSONObject artist_recommendation = onGetUserMostListenArtists("long_term");
    ArrayList<String> songs_recom = parseObjects(songs_recommendation, "id");
    ArrayList<String> artist_recom = parseObjects(artist_recommendation, "id");
    String res_song = String.join("%2C", songs_recom);
    String res_artist = String.join("%2C", artist_recom);
    String url = "https://api.spotify.com/v1/recommendations?market=US&seed_artists="+res_artist +"&seed_tracks="+res_song;
    return spotifyRequest(url);
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
                new String[] {"user-read-email", "user-top-read"}) // <--- Change the scope of your requested token here
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
