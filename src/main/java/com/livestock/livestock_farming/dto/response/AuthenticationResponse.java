package com.livestock.livestock_farming.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    boolean authenticated;

    String accessToken;

    String refreshToken;

    Long accessTokenExpiresIn;

    Long refreshTokenExpiresIn;
}
