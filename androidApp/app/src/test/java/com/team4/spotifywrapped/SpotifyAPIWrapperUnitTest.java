package com.team4.spotifywrapped;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class SpotifyAPIWrapperUnitTest {
  @Test
  public void appAuthCorrect() {
    SpotifyAPIWrapper testWrapper = new SpotifyAPIWrapper(new String[] {"Streaming"});
  }

  @Test
  public void dummyTest() {
    assertEquals(1, 0);
  }
}
