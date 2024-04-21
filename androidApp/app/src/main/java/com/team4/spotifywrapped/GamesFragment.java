package com.team4.spotifywrapped;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GamesFragment extends Fragment {

    private Map<String, Pair<String, String>> playlists = new HashMap<>();
    private Map<String, ArrayList<String>> playlist_songs = new HashMap<>();
    private Button previousWrappedBtn;

    private Call mCall;

    private FirebaseAuth mAuth;

    private TextView profileTextView;

    private ArrayList<String> bankOfSongs;
    private String currentSong;

    private String currentPlaylist;

    private int playlistorder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileTextView = view.findViewById(R.id.game_text_view);

        EditText guessInput = view.findViewById(R.id.guessInput);
        Button submitGuessBtn = view.findViewById(R.id.submitGuessBtn);

        submitGuessBtn.setOnClickListener(v -> {
            String userGuess = guessInput.getText().toString();
            checkGuess(userGuess);
            guessInput.setText(""); // Clear input after guess
        });

        mAuth = FirebaseAuth.getInstance();

        Button gameBtn = view.findViewById(R.id.game_btn);
        Button game2Btn = view.findViewById(R.id.game2_btn);

        gameBtn.setOnClickListener(v -> {
            try {
                if (getLocalToken() != null) {
                    Toast.makeText(getContext(), "Playing game, this may take a while", Toast.LENGTH_SHORT).show();
                    play_game();
                } else {
                    Toast.makeText(getContext(), "Access Token not available", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        game2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeGame2();  // This starts the game when the button is clicked
            }
        });
    }

    private String getLocalToken() {
        Activity activity = getActivity();
        return ((MainMenu) activity).getTokenAccess();
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

        if (playlists.isEmpty()) {
            return; // Exit if there are no playlists to avoid Random exception
        }

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
        currentPlaylist = randomPlaylist;
        currentSong = null;
        // print the song, the playlist and the other 2 playlists
        String txt =    randomSong +
                        "\n\nChoose the playlist: \n·1 - ";
        // Print 3 playlists, randomize in which order the original playlist is shown
        // It can either be the first, second or third
        int randomOrder = rand.nextInt(3);
        playlistorder = randomOrder+1;
        if (randomOrder == 0) {
            txt += randomPlaylist + "\n·2 - ";
            // Choose randomly another playlist from the ones that don't contain the song
            String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
            txt += playlist1 + "\n·3 - ";
            // Delete the playlist that was chosen from the list
            playlists_without_song.remove(playlist1);
            txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n";
        } else if (randomOrder == 1) {
            String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
            txt += playlist1 + "\n·2 - ";
            playlists_without_song.remove(playlist1);
            txt += randomPlaylist + "\n·3 - ";
            txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n";
        } else {
            String playlist1 = playlists_without_song.get(rand.nextInt(playlists_without_song.size()));
            txt += playlist1 + "\n·2 - ";
            playlists_without_song.remove(playlist1);
            txt += playlists_without_song.get(rand.nextInt(playlists_without_song.size())) + "\n·3 - ";
            txt += randomPlaylist + "\n";
        }

        String finalTxt = txt;
        getActivity().runOnUiThread(() -> profileTextView.setText(finalTxt));
    }

    public void getPlaylists() {
        String url = "https://api.spotify.com/v1/me/playlists";
        Log.d("GameFragment1", "Fetching for Playlists...: ");
        spotifyRequest_playlist(url);
    }

    public void spotifyRequest_playlist(String url_parameter) {

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

        Log.d("GameFragment1", "request: " + request);


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
                            Log.d("GameFragment1", "Your Playlist: " + jsonObject);
                            save_playlist(jsonObject, profileTextView);
                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to fetch data, watch Logcat for more details", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /** Get user profile This method will get the user profile using the token */
    public void onGetUserProfileClicked() {
        String url = "https://api.spotify.com/v1/me";
        spotifyRequest(url);
    }

    public void spotifyRequest(String url_parameter) {
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
                final String responseBody = response.body().string(); // Call string once
                try {
                    final JSONObject jsonObject = new JSONObject(responseBody);
                    setTextAsync(jsonObject.toString(3), profileTextView);
                    String email = jsonObject.optString("email", "");
                    String id = jsonObject.optString("id", "");
                    if (getContext() != null) {
                        getActivity().runOnUiThread(() -> {
                            signUpSpotifyWrappedAccount(email, id);
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

    private void initializeGame2() {
        bankOfSongs = new ArrayList<>(((MainMenu) getActivity()).getTop5Songs()); // Ensure this list is retrieved or initialized properly
        if (!bankOfSongs.isEmpty()) {
            playgame2();
        } else {
            Toast.makeText(getContext(), "Please generate your Wrapped first.", Toast.LENGTH_LONG).show();
        }
        playgame2();
    }

    private void playgame2() {
        if (bankOfSongs.isEmpty()) {
            Toast.makeText(getContext(), "All songs guessed! Restarting game or generate new Wrapped.", Toast.LENGTH_LONG).show();
            bankOfSongs = new ArrayList<>(((MainMenu) getActivity()).getTop5Songs());
            return;
        }
        // Select a random song from the top 5 songs and strip 4 random characters from the song name
        // The user has to guess the song name

        int randomIndex = new Random().nextInt(bankOfSongs.size());
        currentSong = bankOfSongs.get(randomIndex);
        currentPlaylist= null;
        bankOfSongs.remove(randomIndex);  // Remove the guessed song from the list

        String displayedSong = obscureSong(currentSong);
        generateGame2Text("Guess the song\n" + displayedSong, profileTextView);
    }

    private String obscureSong(String song) {
        String displayedSong = song;
        int count = 0; // Counter to track the number of replacements
        while (count < 4) {
            int randomCharIndex = (int) (Math.random() * displayedSong.length());
            // Ensure the selected character is not a whitespace
            if (displayedSong.charAt(randomCharIndex) != ' ') {
                displayedSong = displayedSong.substring(0, randomCharIndex) + "_" + displayedSong.substring(randomCharIndex + 1);
                count++;
            }
        }
        return displayedSong;
    }

    public void checkGuess(String guess) {
        if (currentPlaylist==null) {
            if (guess.equalsIgnoreCase(currentSong)) {
                Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
                playgame2();
            } else {
                Toast.makeText(getContext(), "Incorrect! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (guess.equalsIgnoreCase(currentPlaylist) || guess.equalsIgnoreCase(String.valueOf(playlistorder))) {
                Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
                try {
                    if (getLocalToken() != null) {
                        Toast.makeText(getContext(), "Playing game, this may take a while", Toast.LENGTH_SHORT).show();
                        play_game();
                    } else {
                        Toast.makeText(getContext(), "Access Token not available", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println(playlistorder);
                Toast.makeText(getContext(), "Incorrect! Try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void spotifyRequest_playlist_songs(String playlistId) {

        String url_tracks = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
        Log.d("GameFragment1", "url_tracks: " + url_tracks);
        Request request =
                new Request.Builder()
                        .url(url_tracks)
                        .addHeader("Authorization", "Bearer " + getLocalToken())
                        .build();
        Log.d("GameFragment1", "request: " + request);


        OkHttpClient mOkHttpClient = ((MainMenu)getActivity()).getOkHttpClient();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("HTTP", "Failed to fetch tracks: " + e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (!response.isSuccessful()) {
                            Log.e("HTTP", "Server returned an error: " + response.code());
                            return;
                        }

                        final String responseBody = response.body().string();
                        Log.d("GameFragment1", "responseBody: " + responseBody);
                        try {
                            final JSONObject jsonObject = new JSONObject(responseBody);
                            save_songs_from_playlist(jsonObject, playlistId);

                        } catch (JSONException e) {
                            Log.d("JSON", "Failed to parse data: " + e);
                            getActivity().runOnUiThread(
                                    () ->
                                            Toast.makeText(
                                                            getActivity(),
                                                            "Failed to parse data, watch Logcat for more details",
                                                            Toast.LENGTH_SHORT)
                                                    .show());
                        }
                    }
                });
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
                            getActivity(),
                            "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT)
                    .show();
        }
        playlist_songs.put(playlistId, songs);
    }

    public void signUpSpotifyWrappedAccount(String email, String id) {
        String TAG = "SpotifyWrapped Sign Up";
        // Ensure we have a valid Activity before attempting to sign up
        if (getActivity() == null) {
            // Optionally handle the case where the activity is not available
            return;
        }
        mAuth
                .createUserWithEmailAndPassword(email, id)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Created user with " + email + " and password: " + id + " successfully");
                        } else {
                            // Optionally handle the case where sign up fails
                            Log.d(TAG, "Sign up failed", task.getException());
                        }
                    }
                });
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
                            getContext(),
                            "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT)
                    .show();
        }

        return hash_vals;
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }

    private void generateGame2Text(final String text, TextView textView) {
        if (getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            SpannableString spannable = new SpannableString(text);
            int start = text.indexOf('\n') + 1;
            int end = text.length();

            if (start != -1 && start < end) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#1DB954")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannable);
        });
    }
}