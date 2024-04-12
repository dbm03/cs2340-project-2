package com.team4.spotifywrapped.data;

public class PreviousWrappedSelectItem {
  private String text;
  private int pos;

  public PreviousWrappedSelectItem(String text, int pos) {
    this.text = text;
    this.pos = pos;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getPos() {
    return pos;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }
}
