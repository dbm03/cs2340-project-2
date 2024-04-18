package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button modifyBtn = view.findViewById(R.id.modify_btn);
        Button logOutBtn = view.findViewById(R.id.logout_btn);

        modifyBtn.setOnClickListener(v -> {
            // Navigate to modify user settings
            startActivity(new Intent(getActivity(), ModifyUser.class));
        });

        logOutBtn.setOnClickListener(v -> {
            // Handle logout
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), StartupScreen.class));
        });
    }
}