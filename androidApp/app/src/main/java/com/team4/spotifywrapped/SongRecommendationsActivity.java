package com.team4.spotifywrapped;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SongRecommendationsActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private SongRecommendationsAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_song_recommendations);

    recyclerView = findViewById(R.id.song_recommendations_recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    ArrayList<SongRecommendation> songRecommendations =
        getIntent().getParcelableArrayListExtra("recommendations");
    display_and_save_recommendation(songRecommendations);
  }

  private void display_and_save_recommendation(ArrayList<SongRecommendation> recommendations) {
    if (recommendations != null && !recommendations.isEmpty()) {
      // Pass 'this' as the context to the adapter
      adapter = new SongRecommendationsAdapter(this, recommendations);
      recyclerView.setAdapter(adapter);
    } else {
      // Handle case where there are no recommendations
      Log.e("SongRecs", "No song recommendations were passed to the activity");
    }
  }
}
