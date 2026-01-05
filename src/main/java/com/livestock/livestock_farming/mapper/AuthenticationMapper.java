package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationMapper {
    @Value("${jwt.valid-duration}")
    private long validDuration;

    @Value("${jwt.refreshable-duration}")
    private long refreshableDuration;

    public AuthenticationResponse toAuthenticationResponse(
            boolean authenticated, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .authenticated(authenticated)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(validDuration)
                .refreshTokenExpiresIn(refreshableDuration)
                .build();
    }
}
