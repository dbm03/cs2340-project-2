package com.team4.spotifywrapped;

import static java.lang.Thread.sleep;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
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

public class MainMenu extends AppCompatActivity {

  public static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";
  public static final String REDIRECT_URI = "spotifysdk://auth";

  public static final int AUTH_TOKEN_REQUEST_CODE = 0;
  public static final int AUTH_CODE_REQUEST_CODE = 1;

  // Private array to store the top 5 genres
  private ArrayList<String> top5Songs = new ArrayList<>();
  private ArrayList<String> top5Artists = new ArrayList<>();
  private ArrayList<String> top5Songs_id = new ArrayList<>();
  private ArrayList<String> top5Artists_id = new ArrayList<>();
  private Map<String, String> recommendations = new HashMap<>();
  private Map<String, Integer> genres = new HashMap<>();
  private Map<String, Pair<String, String>> playlists = new HashMap<>();
  private Map<String, ArrayList<String>> playlist_songs = new HashMap<>();

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
    Button jsonBtn = (Button) findViewById(R.id.JSON_btn);
    Button jsonBtn2 = (Button) findViewById(R.id.JSON_btn2);
    Button genreBtn = (Button) findViewById(R.id.genre_btn);
    Button gameBtn = (Button) findViewById(R.id.game_btn);

    // Set the click listeners for the buttons

    tokenBtn.setOnClickListener(
        (v) -> {
          getToken();
          System.out.println(top5Songs);
        });

    codeBtn.setOnClickListener(
        (v) -> {
          getCode();
        });

    profileBtn.setOnClickListener(
        (v) -> {
          getRecommendations();
        });

    jsonBtn.setOnClickListener(
        (v) -> {
          onGetUserMostListenArtists("medium_term");
        });

    jsonBtn2.setOnClickListener(
        (v) -> {
          onGetUserMostListenSongs("medium_term");
        });

    genreBtn.setOnClickListener(
        (v) -> {
          onGetUserMostListenGenres("medium_term");
        });

    gameBtn.setOnClickListener(
        (v) -> {
          try {
            play_game();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
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

  public ArrayList<String> parseObjects(JSONObject json_value, String key) {
    ArrayList<String> hash_vals = new ArrayList<>();
    try {
      JSONArray items = (JSONArray) json_value.get("items");

      for (int i = 0; i < items.length(); i++) {
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject item = (JSONObject) items.get(i);
        // Now you can safely call get(key) on the JSONObject
        hash_vals.add(item.getString(key)); // Use getString to directly get the String value
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
              MainMenu.this,
              "Failed to parse data, watch Logcat for more details",
              Toast.LENGTH_SHORT)
          .show();
    }

    return hash_vals;
  }

  public Map<String, String> parseRecommendations(JSONObject json_value) {
    Map<String, String> hash_vals = new HashMap<>();
    try {
      JSONArray items = (JSONArray) json_value.get("tracks");

      for (int i = 0; i < items.length(); i++) {
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject item = (JSONObject) items.get(i);
        // Get artists
        JSONArray artists = item.getJSONArray("artists");
        String artist = "";
        for (int j = 0; j < artists.length(); j++) {
          JSONObject artist_obj = artists.getJSONObject(j);
          artist += artist_obj.getString("name");
          if (j < artists.length() - 1) {
            artist += ", ";
          }
        }
        // Now you can safely call get(key) on the JSONObject
        hash_vals.put(
            item.getString("name"), artist); // Use getString to directly get the String value
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
              MainMenu.this,
              "Failed to parse data, watch Logcat for more details",
              Toast.LENGTH_SHORT)
          .show();
    }

    return hash_vals;
  }

  public Map<String, Integer> parseGenres(JSONObject json_value) {
    Map<String, Integer> hash_vals = new HashMap<>();
    try {
      JSONArray items = (JSONArray) json_value.get("items");

      for (int i = 0; i < items.length(); i++) {
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject item = (JSONObject) items.get(i);
        // Now you can safely call get(key) on the JSONObject
        JSONArray genres = item.getJSONArray("genres");
        for (int j = 0; j < genres.length(); j++) {
          String genre = genres.getString(j);
          if (hash_vals.containsKey(genre)) {
            hash_vals.put(genre, hash_vals.get(genre) + 1);
          } else {
            hash_vals.put(genre, 1);
          }
        }
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
              MainMenu.this,
              "Failed to parse data, watch Logcat for more details",
              Toast.LENGTH_SHORT)
          .show();
    }

    return hash_vals;
  }

  public Map<String, Pair<String, String>> parsePlaylist(JSONObject json_value) {
    Map<String, Pair<String, String>> hash_vals = new HashMap<>();
    try {
      JSONArray items = (JSONArray) json_value.get("items");

      for (int i = 0; i < items.length(); i++) {
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject item = (JSONObject) items.get(i);
        // Now you can safely call get(key) on the JSONObject
        // Get the image
        JSONArray images = item.getJSONArray("images");
        String image = "";
        if (images.length() > 0) {
          JSONObject image_obj = images.getJSONObject(0);
          image = image_obj.getString("url");
        }

        hash_vals.put(
            item.getString("name"),
            new Pair<>(
                item.getString("id"), image)); // Use getString to directly get the String value
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
              MainMenu.this,
              "Failed to parse data, watch Logcat for more details",
              Toast.LENGTH_SHORT)
          .show();
    }

    return hash_vals;
  }

  public void spotifyRequest(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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

  public void spotifyRequest_song(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              display_and_save_song(jsonObject, profileTextView);
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

  public void spotifyRequest_artist(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              display_and_save_artist(jsonObject, profileTextView);
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

  public void spotifyRequest_recommendation(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              display_and_save_recommendation(jsonObject, profileTextView);
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

  public void spotifyRequest_genres(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              display_and_save_genres(jsonObject, profileTextView);
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

  public void spotifyRequest_playlist(String url_parameter) {
    if (mAccessToken == null) {
      Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
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
                    MainMenu.this,
                    "Failed to fetch data, watch Logcat for more details",
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              save_playlist(jsonObject, profileTextView);
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

  /** Get user profile This method will get the user profile using the token */
  public void onGetUserProfileClicked() {
    String url = "https://api.spotify.com/v1/me";
    spotifyRequest(url);
  }

  public void onGetUserMostListenSongs(String timeFrame) {
    String url =
        "https://api.spotify.com/v1/me/top/tracks?time_range=" + timeFrame + "&limit=5&offset=0";
    spotifyRequest_song(url);
  }

  public void onGetUserMostListenArtists(String timeFrame) {

    String url =
        "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&limit=5&offset=0";
    spotifyRequest_artist(url);
  }

  public void onGetUserMostListenGenres(String timeFrame) {
    String url = "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&offset=0";
    spotifyRequest_genres(url);
  }

  public void getRecommendations() {
    if (top5Songs_id.isEmpty()) {
      Toast.makeText(this, "You need to get your top 5 songs first!", Toast.LENGTH_SHORT).show();
      return;
    }
    String song_ids = String.join("%2C", top5Songs_id);
    String url = "https://api.spotify.com/v1/recommendations?&limit=5&seed_tracks=" + song_ids;
    spotifyRequest_recommendation(url);
  }

  private void spotifyRequest_playlist_songs(String playlistId) {
    String url_tracks = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
    Request request =
        new Request.Builder()
            .url(url_tracks)
            .addHeader("Authorization", "Bearer " + mAccessToken)
            .build();

    mCall = mOkHttpClient.newCall(request);
    mCall.enqueue(
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            Log.d("HTTP", "Failed to fetch tracks: " + e);
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            try {
              final JSONObject jsonObject = new JSONObject(response.body().string());
              save_songs_from_playlist(jsonObject, playlistId);

            } catch (JSONException e) {
              Log.d("JSON", "Failed to parse data: " + e);
              runOnUiThread(
                  () ->
                      Toast.makeText(
                              MainMenu.this,
                              "Failed to parse data, watch Logcat for more details",
                              Toast.LENGTH_SHORT)
                          .show());
            }
          }
        });
  }

  private void play_game() throws InterruptedException {
    getPlaylists();
    sleep(2000);

    // Get all the songs from the playlists
    for (Map.Entry<String, Pair<String, String>> entry : playlists.entrySet()) {
      // Use id to get the songs
      spotifyRequest_playlist_songs(entry.getValue().first);
    }
    sleep(3000);
    // Choose a random playlist and a random song from that playlist
    Random rand = new Random();
    int randomPlaylistIndex = rand.nextInt(playlists.size());
    String randomPlaylist = (String) playlists.keySet().toArray()[randomPlaylistIndex];
    String randomPlaylistId = playlists.get(randomPlaylist).first;
    ArrayList<String> songs = playlist_songs.get(randomPlaylistId);
    int randomSongIndex = rand.nextInt(songs.size());
    String randomSong = songs.get(randomSongIndex);

    // Choose 2 more playlists that don't contain the random song that was chosen
    ArrayList<String> playlists_without_song = new ArrayList<>();
    for (Map.Entry<String, Pair<String, String>> entry : playlists.entrySet()) {
      if (!playlist_songs.get(entry.getValue().first).contains(randomSong)) {
        playlists_without_song.add(entry.getKey());
      }
    }
    // print the song, the playlist and the other 2 playlists
    String txt =
        "Song: "
            + randomSong
            + "(Original Playlist: "
            + randomPlaylist
            + ")\nChoose the playlist: ";
    // Print 3 playlists, randomize in which order the original playlist is shown
    // It can either be the first, second or third
    int randomOrder = rand.nextInt(3);
    if (randomOrder == 0) {
      txt += randomPlaylist + "\n";
      // Choose randomly another playlist from the ones that don't contain the song
      String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
      txt += playlist1 + "\n";
      // Delete the playlist that was chosen from the list
      playlists_without_song.remove(playlist1);
      txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n";
    } else if (randomOrder == 1) {
      String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
      txt += playlist1 + "\n";
      playlists_without_song.remove(playlist1);
      txt += randomPlaylist + "\n";
      txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n";
    } else {
      String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
      txt += playlist1 + "\n";
      playlists_without_song.remove(playlist1);
      txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n";
      txt += randomPlaylist + "\n";
    }

    String finalTxt = txt;
    runOnUiThread(() -> profileTextView.setText(finalTxt));
  }

  public void getPlaylists() {
    String url = "https://api.spotify.com/v1/me/playlists";
    spotifyRequest_playlist(url);
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

  private void display_and_save_song(final JSONObject json, TextView textView) {
    // Update top5Songs_id
    top5Songs_id = parseObjects(json, "id");
    ArrayList<String> text = parseObjects(json, "name");
    // Update top5Songs
    top5Songs = text;

    String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));
  }

  private void display_and_save_artist(final JSONObject json, TextView textView) {
    // Update top5Artists_id
    top5Artists_id = parseObjects(json, "id");
    ArrayList<String> text = parseObjects(json, "name");
    // Update top5Artists
    top5Artists = text;

    String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));
  }

  private void display_and_save_recommendation(final JSONObject json, TextView textView) {
    // Update recommendations
    recommendations = parseRecommendations(json);

    ArrayList<String> text = new ArrayList<>();
    for (Map.Entry<String, String> entry : recommendations.entrySet()) {
      text.add(entry.getKey() + " by " + entry.getValue());
    }

    String text_str = String.join("\n", text);

    runOnUiThread(() -> textView.setText(text_str));
  }

  private void display_and_save_genres(final JSONObject json, TextView textView) {
    genres = parseGenres(json);
    int total_genres = 0;
    for (Map.Entry<String, Integer> entry : genres.entrySet()) {
      total_genres += entry.getValue();
    }
    String text_str = "Total genres: " + total_genres + "\n" + "Top 5 genres:\n";
    // Add top 5 genres to text_str
    int i = 0;
    for (Map.Entry<String, Integer> entry : genres.entrySet()) {
      text_str += entry.getKey();
      if (i < 4) {
        text_str += "\n";
      } else {
        break;
      }
      i++;
    }

    String finalText_str = text_str;
    runOnUiThread(() -> textView.setText(finalText_str));
  }

  private void save_playlist(final JSONObject json, TextView textView) {
    playlists = parsePlaylist(json);
  }

  private void save_songs_from_playlist(final JSONObject json, String playlistId) {
    ArrayList<String> songs = new ArrayList<>();
    try {
      JSONArray items = (JSONArray) json.get("items");

      for (int i = 0; i < items.length(); i++) {
        // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

        // i es tu índice en el bucle o algún valor específico
        JSONObject item = (JSONObject) items.get(i);
        // Now you can safely call get(key) on the JSONObject
        JSONObject track = item.getJSONObject("track");
        songs.add(track.getString("name")); // Use getString to directly get the String value
      }

    } catch (JSONException e) {
      Log.d("JSON", "Failed to parse data: " + e);
      Toast.makeText(
              MainMenu.this,
              "Failed to parse data, watch Logcat for more details",
              Toast.LENGTH_SHORT)
          .show();
    }
    playlist_songs.put(playlistId, songs);
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
            new String[] {
              "user-read-email", "user-top-read"
            }) // <--- Change the scope of your requested token here
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
