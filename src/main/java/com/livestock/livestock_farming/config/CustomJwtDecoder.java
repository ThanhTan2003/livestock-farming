package com.livestock.livestock_farming.config;

import java.text.ParseException;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Override
    public Jwt decode(String token) throws JwtException { // Phuong thuc decode de giai ma token JWT
        try {
            SignedJWT signedJWT = SignedJWT.parse(token); // Parse token de lay SignedJWT

            return new Jwt(
                    token, // Tao doi tuong Jwt
                    signedJWT.getJWTClaimsSet().getIssueTime().toInstant(), // Lay thoi gian phat hanh token
                    signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(), // Lay thoi gian het han token
                    signedJWT.getHeader().toJSONObject(), // Lay header cua token
                    signedJWT.getJWTClaimsSet().getClaims() // Lay claims cua token
            );

        } catch (ParseException e) { // Xu ly truong hop parse token that bai
            throw new JwtException("Token không hợp lệ"); // Nen exception neu token khong hop le
        }
    }
}
