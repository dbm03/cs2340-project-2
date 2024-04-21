package com.team4.spotifywrapped;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WrappedScreen3Adapter extends RecyclerView.Adapter<WrappedScreen3Adapter.ViewHolder> {

  private List<WrappedScreen3> artistList;
  private Context context;

  public WrappedScreen3Adapter(Context context, List<WrappedScreen3> artistList) {
    this.context = context;
    this.artistList = artistList;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_artist_recommendation, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    WrappedScreen3 artist = artistList.get(position);
    holder.nameTextView.setText(artist.getName());
    holder.genreTextView.setText("Genre: " + artist.getGenre());
    holder.popularityTextView.setText("Popularity: " + artist.getPopularity());
    Picasso.get().load(artist.getImageUrl()).into(holder.imageView);
  }

  @Override
  public int getItemCount() {
    return artistList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView nameTextView, genreTextView, popularityTextView;
    ImageView imageView;

    public ViewHolder(View view) {
      super(view);
      nameTextView = itemView.findViewById(R.id.artist_name);
      genreTextView = itemView.findViewById(R.id.genre);
      popularityTextView = itemView.findViewById(R.id.popularity);
      imageView = itemView.findViewById(R.id.artist_image);
    }
  }
}
