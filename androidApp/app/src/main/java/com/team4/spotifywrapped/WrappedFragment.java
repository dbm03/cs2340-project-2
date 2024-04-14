package com.team4.spotifywrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WrappedFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_wrapped, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Button wrappedBtn = view.findViewById(R.id.wrapped_btn);
    Button songRecommendationsBtn = view.findViewById(R.id.profile_btn);
    Button artistRecommendationsBtn = view.findViewById(R.id.artist_recom_btn);
    Button previousWrappedBtn = view.findViewById(R.id.previous_wrapped_btn);

    wrappedBtn.setOnClickListener(v -> generateWrapped());
    songRecommendationsBtn.setOnClickListener(v -> getSongRecommendations());
    artistRecommendationsBtn.setOnClickListener(v -> getArtistRecommendations());
    previousWrappedBtn.setOnClickListener(v -> showPreviousWrappeds());
  }

  private void generateWrapped() {
    // Implement the functionality to generate wrapped
  }

  private void getSongRecommendations() {
    // Implement functionality to get song recommendations
  }

  private void getArtistRecommendations() {
    // Implement functionality to get artist recommendations
  }

  private void showPreviousWrappeds() {
    // Implement functionality to show previous wrappeds
  }
}
