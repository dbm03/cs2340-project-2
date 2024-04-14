package com.team4.spotifywrapped;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GamesFragment extends Fragment {

  private Button game1Button;
  private Button game2Button;
  private GamesFragmentListener listener;

  public interface GamesFragmentListener {
    void playGame1() throws InterruptedException;

    void playGame2();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_games, container, false);

    game1Button = view.findViewById(R.id.game1_btn);
    game2Button = view.findViewById(R.id.game2_btn);

    game1Button.setOnClickListener(
        v -> {
          try {
            if (listener != null) {
              listener.playGame1();
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        });
    game2Button.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.playGame2();
          }
        });

    return view;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if (context instanceof GamesFragmentListener) {
      listener = (GamesFragmentListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement GamesFragmentListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }
}
