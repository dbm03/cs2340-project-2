package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WrappedScreen3Activity extends AppCompatActivity {
  private RecyclerView recyclerView;
  private WrappedScreen3Adapter adapter;
  private List<WrappedScreen3> artistRecommendations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wrapped_screen3);

    recyclerView = findViewById(R.id.artist_recommendations_recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    artistRecommendations =
        new ArrayList<>(); // Initialize the list to avoid null pointer exceptions.

    adapter = new WrappedScreen3Adapter(this, artistRecommendations);
    recyclerView.setAdapter(adapter);
    Button nextButton = findViewById(R.id.button5);

    // Load artist recommendations if available
    loadArtistRecommendations();

    Intent intent = getIntent();
    String totalGenres = intent.getStringExtra("totalGenres");
    String top5Genres = intent.getStringExtra("top5Genres");

    nextButton.setOnClickListener((v) -> nextwrappedscreen(totalGenres, top5Genres));
  }

  private void loadArtistRecommendations() {
    Intent intent = getIntent();
    ArrayList<WrappedScreen3> recommendations =
        intent.getParcelableArrayListExtra("artists_wrapped");
    if (recommendations != null && !recommendations.isEmpty()) {
      artistRecommendations.addAll(recommendations);
      adapter.notifyDataSetChanged();
    } else {
      Log.e(
          "ArtistRecs", "No artist recommendations were passed to the activity or they were empty");
      // Optionally show a message or handle the empty case
    }
  }

  private void nextwrappedscreen(String totalGenres, String top5Genres) {
    Intent intent = new Intent(this, WrappedScreen4.class);
    intent.putExtra("totalGenres", totalGenres);
    intent.putExtra("top5Genres", top5Genres);
    startActivity(intent);
  }
}
