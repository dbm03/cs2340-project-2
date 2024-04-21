package com.team4.spotifywrapped;

import android.os.Parcel;
import android.os.Parcelable;

public class WrappedScreen2 implements Parcelable {
  private String name;
  private String imageUrl;
  private String artistName;
  private String genre;

  // Updated constructor
  public WrappedScreen2(String name, String imageUrl, String artistName, String genre) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.artistName = artistName;
    this.genre = genre;
  }

  // Read from parcel
  protected WrappedScreen2(Parcel in) {
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

  public static final Creator<WrappedScreen2> CREATOR =
      new Creator<WrappedScreen2>() {
        @Override
        public WrappedScreen2 createFromParcel(Parcel in) {
          return new WrappedScreen2(in);
        }

        @Override
        public WrappedScreen2[] newArray(int size) {
          return new WrappedScreen2[size];
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
