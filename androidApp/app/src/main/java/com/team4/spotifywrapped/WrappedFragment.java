package com.team4.spotifywrapped;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A fragment to display user-wrapped content.
 * This fragment handles user authentication, token management, and provides
 * functionality to retrieve and display song recommendations and artist recommendations.
 */
public class WrappedFragment extends Fragment {

    /**
     * Spotify client ID for authentication.
     */
    public static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";

    /**
     * Redirect URI for Spotify authentication.
     */
    public static final String REDIRECT_URI = "spotifysdk://auth";

    /**
     * Tag for Firebase logging.
     */
    public static final String FIREBASE_TAG = "Firebase";

    /**
     * Request code for authentication token.
     */
    public static final int AUTH_TOKEN_REQUEST_CODE = 0;

    /**
     * Request code for authentication code.
     */
    public static final int AUTH_CODE_REQUEST_CODE = 1;


    // Data structures to store top artists, songs, recommendations, and genres
    private ArrayList<String> top5Artists = new ArrayList<>();
    private ArrayList<String> top5Songs_id = new ArrayList<>();
    private ArrayList<String> top5Artists_id = new ArrayList<>();
    private Map<String, String> recommendations = new HashMap<>();
    private Map<String, Integer> genres = new HashMap<>();


    // UI elements
    public TextView tokenTextView, codeTextView, profileTextView;
    private Call mCall;
    private FirebaseAuth mAuth;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          If non-null, this is the parent view that the fragment's UI
     *                           should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wrapped, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been
     * restored in to the view. This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.
     *
     * @param view               The View returned by onCreateView()
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a
     *                           previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        LinearLayout tokenBtn = view.findViewById(R.id.token_btn);
        LinearLayout songRecommendationBtn = view.findViewById(R.id.song_recommendations);
        Button wrappedBtn = view.findViewById(R.id.wrapped_btn);
        LinearLayout recommendationsBtn = view.findViewById(R.id.artist_recom_btn);
        TextView previousWrappedBtn = view.findViewById(R.id.previous_wrapped_btn);

        TextView greetingTextView = view.findViewById(R.id.greeting_text_view);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                String firstName = displayName.split(" ")[0];
                greetingTextView.setText(String.format("Welcome, %s", firstName));
            } else {
                greetingTextView.setText("Welcome!");
            }
        } else {
            greetingTextView.setText("Welcome!");
        }

        tokenBtn.setOnClickListener(
                (v) -> getToken());

        wrappedBtn.setOnClickListener(
                v -> showPopupMenu(v));

        songRecommendationBtn.setOnClickListener(
                (v) -> showSongRecommendations());

        recommendationsBtn.setOnClickListener(
                (v) -> showArtistRecommendations());

        previousWrappedBtn.setOnClickListener(
                (v) -> getPreviousWrappeds());
    }

    /**
     * Called when the fragment is visible to the user.
     * This is generally tied to {androidx.fragment.app.FragmentActivity#onStart()} of the
     * containing Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(
                    "MainMenuStart",
                    "currentUser:" + currentUser.getDisplayName() + " email:" + currentUser.getEmail());
        }
    }

    /**
     * Retrieves the locally stored access token.
     *
     * @return The locally stored access token
     */
    private String getLocalToken() {
        Activity activity = getActivity();
        assert activity != null;
        Log.d("HTTP", "Tried calling Token: " + ((MainMenu) activity).getTokenAccess());
        return ((MainMenu) activity).getTokenAccess();
    }

    /**
     * Redirects to the screen displaying previous wrapped content.
     */
    private void getPreviousWrappeds() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), PreviousWrappedSelectScreen.class);
        startActivity(intent);
    }

    /**
     * Redirects to the screen displaying recommended songs.
     */
    private void showSongRecommendations() {
        if (top5Songs_id.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get your top 5 songs first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (getActivity() == null) {
            return;
        }

        getSongRecommendations();
    }

    /**
     * Redirects to the screen displaying recommended artists.
     */
    public void showArtistRecommendations() {
        if (getActivity() == null) {
            return;
        }
        if (top5Artists_id.isEmpty()) {
            Toast.makeText(getContext(), "You need to get your top 5 artists first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String artist_id = top5Artists_id.get(0); // Or use all your artist IDs
        String url = "https://api.spotify.com/v1/artists/" + artist_id + "/related-artists";
        spotifyRequest_artist_recommendation(url);
    }


    /**
     * Generates the Wrapped content based on the specified time frame and displays it.
     * This method retrieves the user's most listened to artists, songs, and genres,
     * generates a Wrapped summary, saves it to Firebase Firestore, and then redirects
     * to the Wrapped screen.
     *
     * @param textView  The TextView to display the Wrapped content
     * @param timeFrame The time frame for which to generate the Wrapped content
     */
    private void generateWrapped(TextView textView, String timeFrame) {
        System.out.println("Generating Wrapped");
        onGetUserMostListenArtists(timeFrame);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Artists done");
        onGetUserMostListenSongs(timeFrame);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Songs done");
        onGetUserMostListenGenres(timeFrame);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Genres done");

        String top5SongsStr = String.join("\n", ((MainMenu) getActivity()).getTop5Songs());
        String top5ArtistsStr = String.join("\n", top5Artists);
        int total_genres = genres.keySet().size();

        // Sort the genres by the number of times they appear
        genres =
                genres.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // Get the top 5 genres
        int i = 0;
        String top5GenresStr = "";
        for (Map.Entry<String, Integer> entry : genres.entrySet()) {
            top5GenresStr += entry.getKey();
            if (i < 4) {
                top5GenresStr += "\n";
            } else {
                break;
            }
            i++;
        }

        String finalText_str =
                "Top 5 Songs: "
                        + top5SongsStr
                        + "\nTop 5 Artists: "
                        + top5ArtistsStr
                        + "\nTotal Genres: "
                        + total_genres
                        + "\nTop 5 Genres: "
                        + top5GenresStr;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        long now = Instant.now().toEpochMilli();

        Map<String, Object> wrappedData = new HashMap<>();
        wrappedData.put("top5Songs", top5SongsStr);
        wrappedData.put("top5Artists", top5ArtistsStr);
        wrappedData.put("totalGenres", (String.valueOf(total_genres)));
        wrappedData.put("top5Genres", top5GenresStr);
        wrappedData.put("epoch", now);

        String userUid = mAuth.getUid();

        if (userUid != null) {
            db.collection("users")
                    .document(userUid)
                    .collection("wrappeds")
                    .add(wrappedData)
                    .addOnSuccessListener(
                            new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(
                                            FIREBASE_TAG, "Document snapshot added with ID:" + documentReference.getId());
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(FIREBASE_TAG, "Error saving spotify wrapped data to database");
                                }
                            });
        }

        redirectToWrapped(top5SongsStr, top5ArtistsStr, (String.valueOf(total_genres)), top5GenresStr);
    }

    /**
     * Redirects to the Wrapped screen with the specified Wrapped content.
     *
     * @param top5SongsStr   Top 5 songs as a String
     * @param top5ArtistsStr Top 5 artists as a String
     * @param totalGenres    Total number of genres as a String
     * @param top5GenresStr  Top 5 genres as a String
     */
    private void redirectToWrapped(String top5SongsStr, String top5ArtistsStr, String totalGenres, String top5GenresStr) {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), WrappedScreen.class);
        intent.putExtra("top5Songs", top5SongsStr);
        intent.putExtra("top5Artists", top5ArtistsStr);
        intent.putExtra("totalGenres", totalGenres);
        intent.putExtra("top5Genres", top5GenresStr);

        getActivity().startActivity(intent);
    }

    /**
     * Shows a popup menu to select the time frame for generating Wrapped content.
     *
     * @param v The View to which the popup menu is anchored
     */
    private void showPopupMenu(View v) {
        if (getContext() == null) {
            return;
        }

        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            executeMethodBasedOnOption(item.getTitle().toString());
            return true;
        });

        popupMenu.show();
    }

    /**
     * Executes a method based on the selected option from the popup menu.
     *
     * @param option The selected option from the popup menu
     */
    private void executeMethodBasedOnOption(String option) {
        // Ensure that there is a valid context before proceeding
        if (getContext() == null) {
            return; // Context is not available, handle this case (e.g., return or show an error)
        }

        LoadingDialog loadingDialog = new LoadingDialog(getContext());
        switch (option) {
            case "Short":
                Toast.makeText(getContext(), "Short term selected, this may take a while", Toast.LENGTH_SHORT)
                        .show();
                loadingDialog.showDialog("Generating Wrapped...");
                generateWrapped(profileTextView, "short_term");
                loadingDialog.hideDialog();
                break;
            case "Medium":
                Toast.makeText(getContext(), "Medium term selected, this may take a while", Toast.LENGTH_SHORT)
                        .show();
                loadingDialog.showDialog("Generating Wrapped...");
                generateWrapped(profileTextView, "medium_term");
                loadingDialog.hideDialog();
                break;
            case "Long":
                Toast.makeText(getContext(), "Long term selected, this may take a while", Toast.LENGTH_SHORT)
                        .show();
                loadingDialog.showDialog("Generating Wrapped...");
                generateWrapped(profileTextView, "long_term");
                loadingDialog.hideDialog();
                break;
            default:
                break;
        }
    }

    /**
     * Get token from Spotify. This method will open the Spotify login activity and get the token.
     * What is token? <a href="https://developer.spotify.com/documentation/general/guides/authorization-guide/">...</a>
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        if (getActivity() != null) {
            AuthorizationClient.openLoginActivity(getActivity(), AUTH_TOKEN_REQUEST_CODE, request);
        }
    }

    /**
     * Get code from Spotify. This method will open the Spotify login activity and get the code.
     * What is code? <a href="https://developer.spotify.com/documentation/general/guides/authorization-guide/">...</a>
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        if (getActivity() != null) {
            AuthorizationClient.openLoginActivity(getActivity(), AUTH_CODE_REQUEST_CODE, request);
        }
    }

    /**
     * Parses JSON objects and extracts values associated with the specified key.
     *
     * @param json_value The JSON object to parse
     * @param key        The key to extract values from
     * @return An ArrayList containing the parsed values
     */
    public ArrayList<String> parseObjects(JSONObject json_value, String key) {
        ArrayList<String> hash_vals = new ArrayList<>();
        try {
            JSONArray items = (JSONArray) json_value.get("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                hash_vals.add(item.getString(key));
            }

        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data: " + e);
            if (getContext() != null) {
                Toast.makeText(
                        getContext(),
                        "Failed to parse data, watch Logcat for more details",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        return hash_vals;
    }

    /**
     * Parses JSON objects containing artist recommendations and extracts artist names.
     *
     * @param json_value The JSON object to parse
     * @return An ArrayList containing the names of recommended artists
     */
    public ArrayList<String> parseArtistRecommendations(JSONObject json_value) {
        ArrayList<String> hash_vals = new ArrayList<>();
        try {
            JSONArray items = (JSONArray) json_value.get("artists");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                hash_vals.add(item.getString("name"));
            }

        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data: " + e);
            Toast.makeText(
                            getContext(),
                            "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT)
                    .show();
        }

        return hash_vals;
    }

    /**
     * Parses JSON objects containing song recommendations and extracts song names and artists.
     *
     * @param json_value The JSON object to parse
     * @return A Map containing song names as keys and corresponding artist names as values
     */
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
                            getContext(),
                            "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT)
                    .show();
        }

        return hash_vals;
    }

    /**
     * Parses JSON objects containing genre information and counts the occurrence of each genre.
     *
     * @param json_value The JSON object to parse
     * @return A Map containing genres as keys and their occurrence counts as values
     */
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
                            getContext(),
                            "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT)
                    .show();
        }

        return hash_vals;
    }

    /**
     * Makes a Spotify request to retrieve song data.
     *
     * @param url_parameter The URL parameter for the Spotify request
     */
    public void spotifyRequest_song(String url_parameter) {
        if (getLocalToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final Request request = new Request.Builder()
                .url(url_parameter)
                .addHeader("Authorization", "Bearer " + getLocalToken())
                .build();

        cancelCall();

        OkHttpClient mOkHttpClient = ((MainMenu) getActivity()).getOkHttpClient();

        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("HTTP", "Server returned error: " + response);
                    return;
                }
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    if (getContext() != null) {
                        getActivity().runOnUiThread(() -> {
                            display_and_save_song(jsonObject, profileTextView);
                        });
                    }
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Makes a Spotify request to retrieve artist data.
     *
     * @param url_parameter The URL parameter for the Spotify request
     */
    public void spotifyRequest_artist(String url_parameter) {
        if (getLocalToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        OkHttpClient mOkHttpClient = ((MainMenu) getActivity()).getOkHttpClient();

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getLocalToken())
                        .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("HTTP", "Failed to fetch data: " + e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject jsonObject = new JSONObject(response.body().string());
                            display_and_save_artist(jsonObject, profileTextView);
                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Makes a Spotify request to retrieve recommendation data.
     *
     * @param url_parameter The URL parameter for the Spotify request
     */
    public void spotifyRequest_recommendation(String url_parameter) {
        if (getLocalToken() == null) {
            Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        final Request request = new Request.Builder()
                .url(url_parameter)
                .addHeader("Authorization", "Bearer " + getLocalToken())
                .build();

        OkHttpClient mOkHttpClient = ((MainMenu) getActivity()).getOkHttpClient();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Use getActivity() and runOnUiThread to show the Toast on the UI thread
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();  // Store response data for logging
                if (!response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Server returned an error", Toast.LENGTH_SHORT).show();
                    });
                    Log.e("HTTP", "Server Error: " + response.code() + " " + responseData);
                    return;
                }
                try {
                    final JSONObject jsonObject = new JSONObject(responseData);
                    getActivity().runOnUiThread(() -> {
                        try {
                            parseAndDisplayRecommendations(jsonObject);
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            Log.e("JSON", "Parsing error", e);
                        }
                    });
                } catch (JSONException e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                    });
                    Log.e("JSON", "Parsing error on initial parse", e);
                }
            }
        });
    }

    private void parseAndDisplayRecommendations(JSONObject json) throws JSONException {
        ArrayList<SongRecommendation> recommendations = new ArrayList<>();
        JSONArray tracks = json.getJSONArray("tracks");

        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);

            // Get song name
            String name = track.getString("name");

            // Get the image URL from the album object
            JSONObject album = track.getJSONObject("album");
            JSONArray images = album.getJSONArray("images");
            String imageUrl = images.length() > 0 ? images.getJSONObject(0).getString("url") : "";

            // Get artist name - assuming the first artist is the primary artist
            JSONArray artists = track.getJSONArray("artists");
            String artistName = artists.length() > 0 ? artists.getJSONObject(0).getString("name") : "Unknown Artist";

            // Optionally, get the genre if it's available
            String genre = "Unknown Genre"; // Default to "Unknown Genre" if not found
            if (album.has("genres") && album.getJSONArray("genres").length() > 0) {
                genre = album.getJSONArray("genres").getString(0);
            }

            // Create a new SongRecommendation object and add it to the list
            SongRecommendation songRecommendation = new SongRecommendation(name, imageUrl, artistName, genre);
            recommendations.add(songRecommendation);
        }

        // Intent to start SongRecommendationsActivity with the recommendations list
        Intent intent = new Intent(getActivity(), SongRecommendationsActivity.class);
        intent.putParcelableArrayListExtra("recommendations", recommendations);
        startActivity(intent);
    }


    /**
     * Makes a Spotify request to retrieve artist recommendations.
     *
     * @param url_parameter The URL parameter for the Spotify request
     */
    public void spotifyRequest_artist_recommendation(String url_parameter) {
        if (getLocalToken() == null) {
            Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url(url_parameter)
                .addHeader("Authorization", "Bearer " + getLocalToken())
                .build();
        OkHttpClient mOkHttpClient = ((MainMenu) getActivity()).getOkHttpClient();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("HTTP", "Failed to fetch data: " + e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Server returned an error", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    ArrayList<ArtistRecommendation> recommendations = parseArtists(jsonObject);
                    Intent intent = new Intent(getActivity(), ArtistRecommendationsActivity.class);
                    intent.putParcelableArrayListExtra("artistRecommendations", recommendations);
                    startActivity(intent);
                } catch (JSONException | IOException e) {
                    Log.e("JSON", "Parsing error on initial parse", e);
                }
            }
        });
    }

    private ArrayList<ArtistRecommendation> parseArtists(JSONObject json) throws JSONException {
        ArrayList<ArtistRecommendation> recommendations = new ArrayList<>();
        JSONArray artists = json.getJSONArray("artists");
        for (int i = 0; i < artists.length(); i++) {
            JSONObject artist = artists.getJSONObject(i);
            String name = artist.getString("name");
            String imageUrl = artist.getJSONArray("images").length() > 0 ? artist.getJSONArray("images").getJSONObject(0).getString("url") : "";
            String genre = artist.getJSONArray("genres").length() > 0 ? artist.getJSONArray("genres").getString(0) : "Unknown genre";
            int popularity = artist.optInt("popularity", 0);
            recommendations.add(new ArtistRecommendation(name, imageUrl, genre, popularity));
        }
        return recommendations;
    }


    /**
     * Makes a Spotify request to retrieve genre data.
     *
     * @param url_parameter The URL parameter for the Spotify request
     */
    public void spotifyRequest_genres(String url_parameter) {
        if (getLocalToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getLocalToken())
                        .build();

        cancelCall();
        OkHttpClient mOkHttpClient = ((MainMenu) getActivity()).getOkHttpClient();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("HTTP", "Failed to fetch data: " + e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            final JSONObject jsonObject = new JSONObject(response.body().string());
                            display_and_save_genres(jsonObject, profileTextView);
                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Retrieves the user's most listened to songs for the specified time frame.
     *
     * @param timeFrame The time frame for which to retrieve the most listened to songs
     */
    public void onGetUserMostListenSongs(String timeFrame) {
        String url =
                "https://api.spotify.com/v1/me/top/tracks?time_range=" + timeFrame + "&limit=5&offset=0";
        spotifyRequest_song(url);
    }

    /**
     * Retrieves the user's most listened to artists for the specified time frame.
     *
     * @param timeFrame The time frame for which to retrieve the most listened to artists
     */
    public void onGetUserMostListenArtists(String timeFrame) {

        String url =
                "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&limit=5&offset=0";
        spotifyRequest_artist(url);
    }

    /**
     * Retrieves the user's most listened to genres for the specified time frame.
     *
     * @param timeFrame The time frame for which to retrieve the most listened to genres
     */
    public void onGetUserMostListenGenres(String timeFrame) {
        String url = "https://api.spotify.com/v1/me/top/artists?time_range=" + timeFrame + "&offset=0";
        spotifyRequest_genres(url);
    }

    /**
     * Initiates the process of getting song recommendations based on the user's top 5 songs.
     */
    public void getSongRecommendations() {
        if (top5Songs_id.isEmpty()) {
            Toast.makeText(getContext(), "You need to get your top 5 songs first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String song_ids = String.join("%2C", top5Songs_id);
        String url = "https://api.spotify.com/v1/recommendations?&limit=5&seed_tracks=" + song_ids;
        spotifyRequest_recommendation(url);
    }

    /**
     * Initiates the process of getting artist recommendations based on the user's top 5 artists.
     */
    public void getArtistRecommendations() {
        if (top5Artists_id.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get your top 5 artists first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Take the first artist id
        String artist_id = top5Artists_id.get(0);
        String url = "https://api.spotify.com/v1/artists/" + artist_id + "/related-artists";
        spotifyRequest_artist_recommendation(url);
    }


    /**
     * Updates the UI and saves the user's top 5 songs.
     *
     * @param json     The JSON object containing song data
     * @param textView The TextView to display the song data
     */
    private void display_and_save_song(final JSONObject json, TextView textView) {
        // Update top5Songs_id
        top5Songs_id = parseObjects(json, "id");
        ArrayList<String> text = parseObjects(json, "name");
        // Update top5Songs
        ((MainMenu) getActivity()).setTop5Songs(text);

    /*String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));*/
    }


    /**
     * Updates the UI and saves the user's top 5 artists.
     *
     * @param json     The JSON object containing artist data
     * @param textView The TextView to display the artist data
     */
    private void display_and_save_artist(final JSONObject json, TextView textView) {
        // Update top5Artists_id
        top5Artists_id = parseObjects(json, "id");
        ArrayList<String> text = parseObjects(json, "name");
        // Update top5Artists
        top5Artists = text;

    /*String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));*/
    }

    /**
     * Updates the UI and saves the song recommendations.
     *
     * @param json     The JSON object containing recommendation data
     * @param textView The TextView to display the recommendation data
     */
    private void display_and_save_recommendation(final JSONObject json, TextView textView) {

        if (getActivity() == null || isDetached()) {
            return; // Fragment not attached, or its activity is null
        }

        // Update recommendations
        recommendations = parseRecommendations(json);

        SpannableStringBuilder builder = new SpannableStringBuilder();

        // We believe these 5 songs are of your liking
        SpannableString boldText =
                new SpannableString("We believe these 5 songs are of your liking\n\n");
        // make "We believe these 5 songs are of your liking"bigger
        boldText.setSpan(new RelativeSizeSpan(2f), 0, boldText.length(), 0);
        builder.append(boldText);

        ArrayList<String> text = new ArrayList<>();
        for (Map.Entry<String, String> entry : recommendations.entrySet()) {
            text.add("· " + entry.getKey() + " by " + entry.getValue() + "\n");
        }

        for (String s : text) {
            SpannableString str = new SpannableString(s + "\n");
            // make it a bit bigger
            str.setSpan(new RelativeSizeSpan(1.5f), 0, str.length(), 0);
            builder.append(str);
        }

        getActivity().runOnUiThread(() -> textView.setText(builder));
    }

    /**
     * Updates the UI and saves the artist recommendations.
     *
     * @param json     The JSON object containing artist recommendation data
     */
    private void display_and_save_artist_recommendation(final JSONObject json) throws JSONException {
        ArrayList<ArtistRecommendation> artistRecommendations = new ArrayList<>();
        JSONArray artists = json.getJSONArray("artists");

        for (int i = 0; i < artists.length(); i++) {
            JSONObject artist = artists.getJSONObject(i);
            String name = artist.getString("name");
            String imageUrl = "";
            if (artist.getJSONArray("images").length() > 0) {
                imageUrl = artist.getJSONArray("images").getJSONObject(0).getString("url");
            }

            String genre = ""; // Default empty if not found
            int popularity = artist.optInt("popularity", 0); // Using optInt to handle missing popularity

            // Check for genre if provided
            if (artist.has("genres") && artist.getJSONArray("genres").length() > 0) {
                genre = artist.getJSONArray("genres").getString(0); // Assuming first genre as primary
            }

            ArtistRecommendation artistRecommendation = new ArtistRecommendation(name, imageUrl, genre, popularity);
            artistRecommendations.add(artistRecommendation);
        }

        // Send data to the activity
        Intent intent = new Intent(getActivity(), ArtistRecommendationsActivity.class);
        intent.putParcelableArrayListExtra("artistRecommendations", artistRecommendations);
        startActivity(intent);
    }


    /**
     * Updates the UI and saves the user's top genres.
     *
     * @param json     The JSON object containing genre data
     * @param textView The TextView to display the genre data
     */
    private void display_and_save_genres(final JSONObject json, TextView textView) {
        genres = parseGenres(json);
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
                        new String[]{
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
}


