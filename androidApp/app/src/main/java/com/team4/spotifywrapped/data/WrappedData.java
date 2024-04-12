package com.team4.spotifywrapped.data;

public class WrappedData {

  private String top5Songs;
  private String top5Artists;
  private int totalGenres;
  private String top5Genres;

  public WrappedData(String top5Songs, String top5Artists, int totalGenres, String top5Genres) {
    this.top5Songs = top5Songs;
    this.top5Artists = top5Artists;
    this.totalGenres = totalGenres;
    this.top5Genres = top5Genres;
  }

  public String getTop5Songs() {
    return top5Songs;
  }

  public void setTop5Songs(String top5Songs) {
    this.top5Songs = top5Songs;
  }

  public String getTop5Artists() {
    return top5Artists;
  }

  public void setTop5Artists(String top5Artists) {
    this.top5Artists = top5Artists;
  }

  public int getTotalGenres() {
    return totalGenres;
  }

  public void setTotalGenres(int totalGenres) {
    this.totalGenres = totalGenres;
  }

  public String getTop5Genres() {
    return top5Genres;
  }

  public void setTop5Genres(String top5Genres) {
    this.top5Genres = top5Genres;
  }
}
