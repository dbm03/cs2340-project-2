package com.team4.spotifywrapped;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtistRecommendation implements Parcelable {
    private String name;
    private String imageUrl;
    private String genre; // optional
    private int popularity; // optional

    public ArtistRecommendation(String name, String imageUrl) {
        this(name, imageUrl, "", 0); // Call the main constructor with default values
    }

    public ArtistRecommendation(String name, String imageUrl, String genre, int popularity) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.genre = genre;
        this.popularity = popularity;
    }

    protected ArtistRecommendation(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        genre = in.readString();
        popularity = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(genre);
        dest.writeInt(popularity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ArtistRecommendation> CREATOR = new Creator<ArtistRecommendation>() {
        @Override
        public ArtistRecommendation createFromParcel(Parcel in) {
            return new ArtistRecommendation(in);
        }

        @Override
        public ArtistRecommendation[] newArray(int size) {
            return new ArtistRecommendation[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getGenre() {
        return genre;
    }

    public int getPopularity() {
        return popularity;
    }
}
