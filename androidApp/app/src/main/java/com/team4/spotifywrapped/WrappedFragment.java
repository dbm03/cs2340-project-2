package com.team4.spotifywrapped;

import static java.lang.Thread.sleep;
import java.time.Instant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WrappedFragment extends Fragment {

    // public TextView profileTextView;
    public static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";
    public static final String REDIRECT_URI = "spotifysdk://auth";

    public static final String FIREBASE_TAG = "Firebase";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    // Variables for Wrapped
    private ArrayList<String> top5Artists = new ArrayList<>();
    private ArrayList<String> top5Songs_id = new ArrayList<>();
    private ArrayList<String> top5Artists_id = new ArrayList<>();
    private Map<String, String> recommendations = new HashMap<>();
    private Map<String, Integer> genres = new HashMap<>();

    // private String mAccessToken, mAccessCode;
    public TextView tokenTextView, codeTextView, profileTextView;

    private Call mCall;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wrapped, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        TextView tokenTextView = view.findViewById(R.id.token_text_view);
        TextView profileTextView = view.findViewById(R.id.response_text_view);

        Button tokenBtn = view.findViewById(R.id.token_btn);
        Button profileBtn = view.findViewById(R.id.profile_btn);
        Button wrappedBtn = view.findViewById(R.id.wrapped_btn);
        Button recommendationsBtn = view.findViewById(R.id.artist_recom_btn);
        Button previousWrappedBtn = view.findViewById(R.id.previous_wrapped_btn);


        tokenBtn.setOnClickListener(
                (v) -> {
                    getToken();
                });

        profileBtn.setOnClickListener(
                (v) -> {
                    getRecommendations();
                });

        recommendationsBtn.setOnClickListener(
                (v) -> {
                    getArtistRecommendations();
                });

        wrappedBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupMenu(v);
                    }
                });

        previousWrappedBtn.setOnClickListener(
                (v) -> {
                    getPreviousWrappeds();
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
        }
    }

    private void updateToken(String newToken) {
        Activity activity = getActivity();
        if (activity instanceof MainMenu) {
            ((MainMenu) activity).setAccessToken(newToken);
        } else {
            // Handle error or log a warning that the activity is not the expected type
        }
    }

    private String getAccessToken() {
        if (getActivity() instanceof MainMenu) {
            return ((MainMenu) getActivity()).getAccessToken();
        }
        return null;
    }

    private void getPreviousWrappeds() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), PreviousWrappedSelectScreen.class);
        startActivity(intent);
    }

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
     * What is token? https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        if (getActivity() != null) {
            AuthorizationClient.openLoginActivity(getActivity(), AUTH_TOKEN_REQUEST_CODE, request);
        }
    }

    /**
     * Get code from Spotify. This method will open the Spotify login activity and get the code.
     * What is code? https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        if (getActivity() != null) {
            AuthorizationClient.openLoginActivity(getActivity(), AUTH_CODE_REQUEST_CODE, request);
        }
    }

    /**
     * When the app leaves this activity to momentarily get a token/code, this function fetches the
     * result of that external activity to get the response from Spotify
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            updateToken(response.getAccessToken());
            System.out.println("Access token: " + getAccessToken());
            setTextAsync("You successfully logged in!", tokenTextView);
        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            updateToken(response.getCode());
            setTextAsync("You successfully retrieved the token!", codeTextView);
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
            if (getContext() != null) {
                Toast.makeText(
                        getContext(),
                        "Failed to parse data, watch Logcat for more details",
                        Toast.LENGTH_SHORT
                ).show();
            }        }

        return hash_vals;
    }

    public ArrayList<String> parseArtistRecommendations(JSONObject json_value) {
        ArrayList<String> hash_vals = new ArrayList<>();
        try {
            JSONArray items = (JSONArray) json_value.get("artists");

            for (int i = 0; i < items.length(); i++) {
                // Suponiendo que top_artists es un JSONObject y 'items' es un JSONArray dentro de él

                // i es tu índice en el bucle o algún valor específico
                JSONObject item = (JSONObject) items.get(i);
                // Now you can safely call get(key) on the JSONObject
                hash_vals.add(item.getString("name")); // Use getString to directly get the String value
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

    public void spotifyRequest_song(String url_parameter) {
        if (getAccessToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .build();

        cancelCall();
        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();
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

    public void spotifyRequest_artist(String url_parameter) {
        if (getAccessToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
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

    public void spotifyRequest_recommendation(String url_parameter) {
        if (getAccessToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .build();

        cancelCall();
        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();
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
                            display_and_save_recommendation(jsonObject, profileTextView);
                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void spotifyRequest_artist_recommendation(String url_parameter) {
        if (getAccessToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .build();

        cancelCall();
        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();

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
                            display_and_save_artist_recommendation(jsonObject, profileTextView);
                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void spotifyRequest_genres(String url_parameter) {
        if (getAccessToken() == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a request to get the user profile
        final Request request =
                new Request.Builder()
                        .url(url_parameter)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .build();

        cancelCall();
        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();
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
            if (getContext() != null) {
                Toast.makeText(getContext(), "You need to get your top 5 songs first!", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String song_ids = String.join("%2C", top5Songs_id);
        String url = "https://api.spotify.com/v1/recommendations?&limit=5&seed_tracks=" + song_ids;
        spotifyRequest_recommendation(url);
    }

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
     * Creates a UI thread to update a TextView in the background Reduces UI latency and makes the
     * system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }

    private void display_and_save_song(final JSONObject json, TextView textView) {
        // Update top5Songs_id
        top5Songs_id = parseObjects(json, "id");
        ArrayList<String> text = parseObjects(json, "name");
        // Update top5Songs
        ((MainMenu) getActivity()).setTop5Songs(text);

    /*String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));*/
    }

    private void display_and_save_artist(final JSONObject json, TextView textView) {
        // Update top5Artists_id
        top5Artists_id = parseObjects(json, "id");
        ArrayList<String> text = parseObjects(json, "name");
        // Update top5Artists
        top5Artists = text;

    /*String text_str = String.join("\n", text);
    runOnUiThread(() -> textView.setText(text_str));*/
    }

    private void display_and_save_recommendation(final JSONObject json, TextView textView) {
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

    private void display_and_save_artist_recommendation(final JSONObject json, TextView textView) {
        // Update recommendations
        ArrayList<String> text = parseArtistRecommendations(json);

        SpannableStringBuilder builder = new SpannableStringBuilder();

        // We believe these 5 songs are of your liking
        SpannableString boldText =
                new SpannableString("We believe these 5 artists are of your liking\n\n");
        // make "We believe these 5 songs are of your liking"bigger
        boldText.setSpan(new RelativeSizeSpan(2f), 0, boldText.length(), 0);
        builder.append(boldText);

        // Get the first 5 artists
        int i = 0;
        for (String s : text) {
            if (i >= 5) {
                break;
            }
            SpannableString str = new SpannableString("· " + s + "\n");
            // make it a bit bigger
            str.setSpan(new RelativeSizeSpan(1.5f), 0, str.length(), 0);
            builder.append(str);
            i++;
        }

        getActivity().runOnUiThread(() -> textView.setText(builder));
    }

    private void display_and_save_genres(final JSONObject json, TextView textView) {
        genres = parseGenres(json);
    /*int total_genres = 0;
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
    runOnUiThread(() -> textView.setText(finalText_str));*/
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
}


