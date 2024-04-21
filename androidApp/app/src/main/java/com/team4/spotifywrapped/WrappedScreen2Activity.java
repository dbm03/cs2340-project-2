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

public class WrappedScreen2Activity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private WrappedScreen2Adapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wrapped_screen2);

    recyclerView = findViewById(R.id.song_recommendations_recyclerview);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    Button nextButton = findViewById(R.id.button3);

    ArrayList<WrappedScreen2> songs_wrapped = getIntent().getParcelableArrayListExtra("songs_wrapped");
    ArrayList<WrappedScreen3> artists_wrapped = getIntent().getParcelableArrayListExtra("artists_wrapped");
    String top5Genres = getIntent().getStringExtra("top5Genres");
    String totalGenres = getIntent().getStringExtra("totalGenres");

    display_and_save_recommendation(songs_wrapped);

    nextButton.setOnClickListener(
            (v) -> nextwrappedscreen(artists_wrapped, totalGenres, top5Genres));
  }

  private void display_and_save_recommendation(ArrayList<WrappedScreen2> recommendations) {
    if (recommendations != null && !recommendations.isEmpty()) {
      // Pass 'this' as the context to the adapter
      adapter = new WrappedScreen2Adapter(this, recommendations);
      recyclerView.setAdapter(adapter);
    } else {
      // Handle case where there are no recommendations
      Log.e("SongRecs", "No song recommendations were passed to the activity");
    }
  }

  private void nextwrappedscreen(ArrayList<WrappedScreen3> top5Artists, String totalGenres, String top5Genres) {
    Intent intent = new Intent(this, WrappedScreen3Activity.class);
    intent.putParcelableArrayListExtra("artists_wrapped", top5Artists);
    intent.putExtra("totalGenres", totalGenres);
    intent.putExtra("top5Genres", top5Genres);
    startActivity(intent);
  }
}
