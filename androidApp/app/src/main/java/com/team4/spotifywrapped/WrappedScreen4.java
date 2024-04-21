package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class WrappedScreen4 extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.wrapped_screen4);

    String top5Genres = getIntent().getStringExtra("top5Genres");
    String totalGenres = getIntent().getStringExtra("totalGenres");

    Button nextButton = findViewById(R.id.button2);
    TextView textView_top_5_genres = findViewById(R.id.textView_top_5_genres);
    TextView textView_total_genres = findViewById(R.id.textView_total_genres);

    SpannableStringBuilder builder = new SpannableStringBuilder();

    // Make total genres big
    // Total Genres
    SpannableString number = new SpannableString(totalGenres);
    number.setSpan(
        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, number.length(), 0);
    number.setSpan(new RelativeSizeSpan(5f), 0, number.length(), 0);
    builder.append(number);

    textView_total_genres.setText(builder, TextView.BufferType.SPANNABLE);

    // List the top 5 genres
    SpannableStringBuilder builder2 = new SpannableStringBuilder();
    // Top 5 Genres
    SpannableString number2 = new SpannableString(top5Genres);
    number2.setSpan(
        new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, number2.length(), 0);
    number2.setSpan(new RelativeSizeSpan(2f), 0, number2.length(), 0);
    builder2.append(number2);

    textView_top_5_genres.setText(builder2, TextView.BufferType.SPANNABLE);

    nextButton.setOnClickListener((v) -> goBackToHomeScreen());
  }

  private void goBackToHomeScreen() {
    // go back 4 activities, simulate pressing back button 4 times
    Intent intent = new Intent(this, MainMenu.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);

    // Finish this activity to remove it from the stack
    finish();
  }
}
