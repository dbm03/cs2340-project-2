package com.team4.spotifywrapped;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class WrappedScreen1 extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.wrapped_screen1);

    ArrayList<WrappedScreen2> songs_wrapped =
        getIntent().getParcelableArrayListExtra("songs_wrapped");
    ArrayList<WrappedScreen3> artists_wrapped =
        getIntent().getParcelableArrayListExtra("artists_wrapped");
    String top5Genres = getIntent().getStringExtra("top5Genres");
    String totalGenres = getIntent().getStringExtra("totalGenres");

    Button nextButton = findViewById(R.id.button2);
    TextView textView = findViewById(R.id.textView);

    SpannableStringBuilder builder = new SpannableStringBuilder();

    // Bold and big for title These are your top 5 songs, artists, and genres from Spotify!
    SpannableString boldSpan = new SpannableString("This is your Spotify Wrapped!");
    boldSpan.setSpan(
        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, boldSpan.length(), 0);
    boldSpan.setSpan(new RelativeSizeSpan(3f), 0, boldSpan.length(), 0);
    // make Spotify light green
    boldSpan.setSpan(
        new android.text.style.ForegroundColorSpan(Color.GREEN),
        boldSpan.toString().indexOf("Spotify"),
        boldSpan.toString().indexOf("Spotify") + "Spotify".length(),
        0);

    System.out.println("boldSpan: " + boldSpan);

    // Set the text
    textView.setText(boldSpan);

    nextButton.setOnClickListener(
        (v) -> nextwrappedscreen(songs_wrapped, artists_wrapped, totalGenres, top5Genres));
  }

  private void nextwrappedscreen(
      ArrayList<WrappedScreen2> top5Songs,
      ArrayList<WrappedScreen3> top5Artists,
      String totalGenres,
      String top5Genres) {
    Intent intent = new Intent(this, WrappedScreen2Activity.class);
    intent.putParcelableArrayListExtra("songs_wrapped", top5Songs);
    intent.putParcelableArrayListExtra("artists_wrapped", top5Artists);
    intent.putExtra("totalGenres", totalGenres);
    intent.putExtra("top5Genres", top5Genres);

    startActivity(intent);
  }
}
