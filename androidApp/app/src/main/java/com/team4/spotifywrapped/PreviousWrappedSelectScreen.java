package com.team4.spotifywrapped;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.team4.spotifywrapped.data.PreviousWrappedSelectItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PreviousWrappedSelectScreen extends AppCompatActivity
        implements PreviousWrappedAdapter.ItemClickListener {

    private FirebaseAuth mAuth;
    private List<String> selectItems;
    private List<Map<String, Object>> firebaseItems;

    private PreviousWrappedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.previouswrapped_select_screen);

        mAuth = FirebaseAuth.getInstance();

        firebaseItems = new ArrayList<>();

        // RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // recyclerView.setAdapter(customAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userUid = mAuth.getUid();

        if (userUid == null) {
            Log.d("Firebase", "There was an error when fetching UID");
            return;
        }

        db.collection("users")
                .document(userUid)
                .collection("wrappeds")
                .orderBy("epoch", Query.Direction.DESCENDING) // Order by epoch in descending order
                .get()
                .addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    selectItems = new ArrayList<>();
                                    int i = 0;
                                    for (Iterator<QueryDocumentSnapshot> it = task.getResult().iterator();
                                         it.hasNext();
                                         i++) {
                                        Map<String, Object> data = it.next().getData();
                                        long epoch;
                                        String title;
                                        try {
                                            epoch = (long) data.get("epoch");
                                            Date date = new Date(epoch);
                                            title = Utils.formatDateDifference(date);
                                        } catch (NullPointerException e) {
                                            Log.d("Firebase", "Couldn't parse epoch time");
                                            title = "NULL";
                                        }
                                        ArrayList<HashMap> top5Songs = (ArrayList<HashMap>) data.get("top5Songs");
                                        ArrayList<HashMap> top5Artists = (ArrayList<HashMap>) data.get("top5Artists");
                                        String totalGenres = (String) data.get("totalGenres");
                                        String top5Genres = (String) data.get("top5Genres");

                                        Map<String, Object> newWrapped = new HashMap<>();

                                        newWrapped.put("top5Songs", top5Songs);
                                        newWrapped.put("top5Artists", top5Artists);
                                        newWrapped.put("totalGenres", totalGenres);
                                        newWrapped.put("top5Genres", top5Genres);

                                        firebaseItems.add(newWrapped);

                                        PreviousWrappedSelectItem newItem = new PreviousWrappedSelectItem(title, i);
                                        selectItems.add(newItem.getText());
                                    }

                                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                                    recyclerView.setLayoutManager(
                                            new LinearLayoutManager(PreviousWrappedSelectScreen.this));
                                    adapter =
                                            new PreviousWrappedAdapter(PreviousWrappedSelectScreen.this, selectItems);
                                    adapter.setClickListener(PreviousWrappedSelectScreen.this);
                                    recyclerView.setAdapter(adapter);
                                } else {
                                    Log.d("Firebase", "Error fetching from firestore");
                                }
                            }
                        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(
                        this,
                        "You clicked " + adapter.getItem(position) + " on row number " + position,
                        Toast.LENGTH_SHORT)
                .show();
        Map<String, Object> selectedWrapped = firebaseItems.get(position);
        ArrayList<HashMap> top5Songs = (ArrayList<HashMap>) selectedWrapped.get("top5Songs");
        ArrayList<HashMap> top5Artists = (ArrayList<HashMap>) selectedWrapped.get("top5Artists");
        String totalGenres = (String) selectedWrapped.get("totalGenres");
        String top5Genres = (String) selectedWrapped.get("top5Genres");

        redirectToWrapped(top5Songs, top5Artists, totalGenres, top5Genres);
    }

    private void redirectToWrapped(
            ArrayList<HashMap> top5Songs,
            ArrayList<HashMap> top5Artists,
            String totalGenres,
            String top5Genres) {
        Intent intent = new Intent(PreviousWrappedSelectScreen.this, WrappedScreen1.class);
        // put the final text string in the intent

        //top5songs is an array that contains the fields of the object WrappedScreen2
        //top5artists is an array that contains the fields of the object WrappedScreen3
        //We need to recreate these objects now, and pass them to the next activity

        ArrayList<WrappedScreen3> artists_wrapped = new ArrayList<>();
        ArrayList<WrappedScreen2> songs_wrapped = new ArrayList<>();

        //Recreating the WrappedScreen2 objects
        for (HashMap song : top5Songs) {
            //Each song looks like {imageUrl=https://i.scdn.co/image/ab67616d0000b273f74a26b9b62a6a98e56a84e3, genre=Unknown Genre, name=Personal, artistName=HRVY}
            String name = (String) song.get("name");
            String imageUrl = (String) song.get("imageUrl");
            String artistName = (String) song.get("artistName");
            String genre = (String) song.get("genre");
            songs_wrapped.add(new WrappedScreen2(name, imageUrl, artistName, genre));
        }

        //Recreating the WrappedScreen3 objects
        for (HashMap artist : top5Artists) {
            //Each artist looks like {imageUrl=https://i.scdn.co/image/ab6761610000e5ebe672b5f553298dcdccb0e676, popularity=100, genre=pop, name=Taylor Swift}
            String name = (String) artist.get("name");
            String imageUrl = (String) artist.get("imageUrl");
            String genre = (String) artist.get("genre");
            Long popularity = (Long) artist.get("popularity");
            artists_wrapped.add(new WrappedScreen3(name, imageUrl, genre, (int) (long) popularity));
        }


        System.out.println("artists_wrapped: " + artists_wrapped);
        System.out.println("songs_wrapped: " + songs_wrapped);
        intent.putParcelableArrayListExtra("songs_wrapped", songs_wrapped);
        intent.putParcelableArrayListExtra("artists_wrapped", artists_wrapped);
        intent.putExtra("totalGenres", totalGenres);
        intent.putExtra("top5Genres", top5Genres);

        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(
                    "MainMenuStart",
                    "currentUser:" + currentUser.getDisplayName() + " email:" + currentUser.getEmail());
            // User already signed in
        }
    }
}
