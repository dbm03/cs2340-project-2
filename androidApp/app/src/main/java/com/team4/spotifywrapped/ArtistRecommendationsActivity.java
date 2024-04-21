package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ArtistRecommendationsActivity extends AppCompatActivity {
  private RecyclerView recyclerView;
  private ArtistRecommendationsAdapter adapter;
  private List<ArtistRecommendation> artistRecommendations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_artist_recommendation);

    recyclerView = findViewById(R.id.artist_recommendations_recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    artistRecommendations =
        new ArrayList<>(); // Initialize the list to avoid null pointer exceptions.

    adapter = new ArtistRecommendationsAdapter(this, artistRecommendations);
    recyclerView.setAdapter(adapter);

    // Load artist recommendations if available
    loadArtistRecommendations();
  }

  private void loadArtistRecommendations() {
    Intent intent = getIntent();
    ArrayList<ArtistRecommendation> recommendations =
        intent.getParcelableArrayListExtra("artistRecommendations");
    if (recommendations != null && !recommendations.isEmpty()) {
      artistRecommendations.addAll(recommendations);
      adapter.notifyDataSetChanged();
    } else {
      Log.e(
          "ArtistRecs", "No artist recommendations were passed to the activity or they were empty");
      // Optionally show a message or handle the empty case
    }
  }
}
