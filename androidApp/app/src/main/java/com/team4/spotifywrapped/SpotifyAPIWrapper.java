package com.team4.spotifywrapped;

import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class SpotifyAPIWrapper {
  private static final String CLIENT_ID = "ab2d3ae0a0ee47a6990b4774ad98c805";
  public static final String REDIRECT_URI = "spotifysdk://auth";

  public AuthorizationRequest authRequest;
  public AuthorizationResponse authResponse;

  public SpotifyAPIWrapper(String[] scopes) {
    // Authorise application
    AuthorizationRequest.Builder builder =
        new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

    builder.setScopes(scopes);
    authRequest = builder.build();
  }
}
