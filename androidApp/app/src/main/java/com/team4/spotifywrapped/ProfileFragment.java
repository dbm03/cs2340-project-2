package com.team4.spotifywrapped;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

  private Button signOutButton;
  private Button fetchRecommendationsButton;
  private TextView userInfoTextView;
  private ProfileFragmentListener listener;

  public interface ProfileFragmentListener {
    void logoutUser();

    void getRecommendations();

    void redirectToLoginScreen();
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_profile, container, false);

    signOutButton = view.findViewById(R.id.logout_btn);
    fetchRecommendationsButton = view.findViewById(R.id.get_recommendations_btn);
    userInfoTextView = view.findViewById(R.id.user_info_text_view);

    signOutButton.setOnClickListener(v -> logoutUser());
    fetchRecommendationsButton.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.getRecommendations();
          }
        });

    displayUserInfo();

    return view;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if (context instanceof ProfileFragmentListener) {
      listener = (ProfileFragmentListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement ProfileFragmentListener");
    }
  }

  private void displayUserInfo() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
      String userDetails =
          "Logged in as: "
              + (user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
      userInfoTextView.setText(userDetails);
    } else {
      userInfoTextView.setText("No user logged in.");
    }
  }

  private void logoutUser() {
    if (listener != null) {
      FirebaseAuth.getInstance().signOut();
      listener.logoutUser();
      listener.redirectToLoginScreen();
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }
}
