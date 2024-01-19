package com.myhome.server.api.service;

public interface AuthService {
    void updateTokens(String accessToken, String refreshToken, String id);
    boolean checkPassword(String inputPassword, String id);
    String getAccessToken(String refreshToken);
    boolean validateAccessToken(String accessToken);
    boolean validateRefreshToken(String refreshToken);
    String validateAuth(String accessToken);
    String reissueAccessToken(String accessToken);
    String reissueRefreshToken(String refreshToken);
}
