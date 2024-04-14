package com.team4.spotifywrapped;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WrappedFragment extends Fragment {

  private Button generateWrappedButton;
  private Button showPreviousWrappedButton;
  private Button signInSpotifyButton;
  private WrappedFragmentListener listener;

  public interface WrappedFragmentListener {
    void generateWrapped(String timeFrame);

    void getPreviousWrappeds();

    void signInSpotify();

    boolean isSpotifyAuthenticated();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_wrapped, container, false);

    generateWrappedButton = view.findViewById(R.id.wrapped_btn);
    showPreviousWrappedButton = view.findViewById(R.id.previous_wrapped_btn);
    signInSpotifyButton = view.findViewById(R.id.spotify_sign_in_btn);

    generateWrappedButton.setOnClickListener(
        v -> {
          if (listener != null && listener.isSpotifyAuthenticated()) {
            listener.generateWrapped("medium_term"); // Or handle timeframe selection differently
          } else {
            Toast.makeText(getContext(), "Please sign in with Spotify first.", Toast.LENGTH_SHORT)
                .show();
          }
        });

    showPreviousWrappedButton.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.getPreviousWrappeds();
          }
        });

    signInSpotifyButton.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.signInSpotify();
          }
        });

    return view;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if (context instanceof WrappedFragmentListener) {
      listener = (WrappedFragmentListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement WrappedFragmentListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }
}
