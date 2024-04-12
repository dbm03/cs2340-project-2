package com.team4.spotifywrapped;

import java.util.Date;

public class Utils {
  public static String formatDateDifference(Date date) {
    Date currentDate = new Date();
    long difference = currentDate.getTime() - date.getTime();
    long seconds = difference / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    long weeks = days / 7;
    long months = days / 30;
    long years = days / 365;

    if (years > 0) {
      return years + " year" + (years > 1 ? "s" : "") + " ago";
    } else if (months > 0) {
      return months + " month" + (months > 1 ? "s" : "") + " ago";
    } else if (weeks > 0) {
      return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
    } else if (days > 0) {
      return days + " day" + (days > 1 ? "s" : "") + " ago";
    } else if (hours > 0) {
      return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
    } else if (minutes > 0) {
      return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
    } else {
      return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
    }
  }
}
