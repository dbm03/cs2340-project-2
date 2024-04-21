package com.team4.spotifywrapped;

import android.os.Parcel;
import android.os.Parcelable;

public class SongRecommendation implements Parcelable {
  private String name;
  private String imageUrl;
  private String artistName;
  private String genre;

  // Updated constructor
  public SongRecommendation(String name, String imageUrl, String artistName, String genre) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.artistName = artistName;
    this.genre = genre;
  }

  // Read from parcel
  protected SongRecommendation(Parcel in) {
    name = in.readString();
    imageUrl = in.readString();
    artistName = in.readString();
    genre = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeString(imageUrl);
    dest.writeString(artistName);
    dest.writeString(genre);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<SongRecommendation> CREATOR =
      new Creator<SongRecommendation>() {
        @Override
        public SongRecommendation createFromParcel(Parcel in) {
          return new SongRecommendation(in);
        }

        @Override
        public SongRecommendation[] newArray(int size) {
          return new SongRecommendation[size];
        }
      };

  // Getters
  public String getName() {
    return name;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getArtistName() {
    return artistName;
  }

  public String getGenre() {
    return genre;
  }
}
