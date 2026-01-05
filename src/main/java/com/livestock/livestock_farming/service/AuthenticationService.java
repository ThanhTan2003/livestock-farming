package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.request.AuthenticationRequest;
import com.livestock.livestock_farming.dto.request.IntrospectRequest;
import com.livestock.livestock_farming.dto.request.LogOutRequest;
import com.livestock.livestock_farming.dto.request.RefreshRequest;
import com.livestock.livestock_farming.dto.response.AuthenticationResponse;
import com.livestock.livestock_farming.dto.response.IntrospectResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    private Long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    private Long REFRESHABLE_DURATION;

    // --- [QUAN TRỌNG] BỘ NHỚ TẠM ĐỂ LƯU TOKEN ĐÃ ĐĂNG XUẤT ---
    // Dùng cái này thay vì Database/Redis cho đồ án môn học
    private final Set<String> invalidatedTokenIds = ConcurrentHashMap.newKeySet();

    // ========== LOGIN ==========
    public AuthenticationResponse login(AuthenticationRequest request) {
        log.info("[login] Checking user: {}", request.getUserName());

        // FIX LỖI 1: Logic check password đơn giản cho đồ án
        boolean isValidUser = "admin".equals(request.getUserName()) && "admin".equals(request.getPassword());

        if (!isValidUser) {
            // FIX LỖI 2: Phải throw exception, không được return exception
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }

        // Tạo token
        String jwtID = UUID.randomUUID().toString();
        String accessToken = generateToken(jwtID, false);
        String refreshToken = generateToken(jwtID, true);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(VALID_DURATION)
                .refreshTokenExpiresIn(REFRESHABLE_DURATION)
                .build();
    }

    // ========== REFRESH TOKEN ==========
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        try {
            // 1. Verify token cũ
            SignedJWT signedJWT = verifyToken(request.getToken(), true);
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

            // 2. Check xem token này đã bị đăng xuất chưa (Blacklist check)
            if (invalidatedTokenIds.contains(jwtId)) {
                throw new RuntimeException("Token này đã bị hủy (Đăng xuất)");
            }

            // 3. Tạo cặp token mới
            String newJwtId = UUID.randomUUID().toString();
            String accessToken = generateToken(newJwtId, false);
            String refreshToken = generateToken(newJwtId, true);

            // (Option) Đồ án thì có thể giữ token cũ vẫn sống, hoặc cho vào blacklist luôn tùy logic
            // invalidatedTokenIds.add(jwtId); // Nếu muốn hủy token cũ ngay lập tức

            return AuthenticationResponse.builder()
                    .authenticated(true)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresIn(VALID_DURATION)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi làm mới token: " + e.getMessage());
        }
    }

    // ========== LOGOUT ==========
    public void logOut(LogOutRequest request) {
        try {
            // Verify để lấy ID của token
            SignedJWT signedJWT = SignedJWT.parse(request.getToken()); // Không cần verify kỹ chữ ký lúc logout cũng được để cho nhanh
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

            // [QUAN TRỌNG] Đưa ID vào danh sách đen
            invalidatedTokenIds.add(jwtId);

            log.info("Đã đăng xuất (blacklist) token ID: {}", jwtId);
        } catch (ParseException e) {
            log.warn("Token không đúng định dạng khi logout");
        }
    }

    // ========== INTROSPECT ==========
    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean isValid = true;
        try {
            verifyToken(request.getToken(), false);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    // ========== HELPER: GENERATE & VERIFY ==========

    // Gộp hàm generate lại cho gọn
    private String generateToken(String jwtID, boolean isRefresh) {
        long duration = isRefresh ? REFRESHABLE_DURATION : VALID_DURATION;
        Date expiryDate = new Date(System.currentTimeMillis() + duration * 1000);

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject("admin") // Hardcode subject cho đồ án
                .issuer("LivestockSystem")
                .issueTime(new Date())
                .expirationTime(expiryDate)
                .jwtID(jwtID)
                .claim("token_type", isRefresh ? "refresh" : "access");

        if (!isRefresh) {
            claimsBuilder.claim("scope", "Admin");
        }

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Payload payload = new Payload(claimsBuilder.build().toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 1. Check chữ ký
        if (!signedJWT.verify(verifier)) {
            throw new RuntimeException("Chữ ký không hợp lệ");
        }

        // 2. Check hạn sử dụng
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime.before(new Date())) {
            throw new RuntimeException("Token đã hết hạn");
        }

        // 3. [QUAN TRỌNG] Check Blacklist (Đã đăng xuất chưa)
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if (invalidatedTokenIds.contains(jwtId)) {
            throw new RuntimeException("Token đã bị đăng xuất");
        }

        return signedJWT;
    }
}