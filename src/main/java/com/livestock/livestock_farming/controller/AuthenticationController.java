package com.livestock.livestock_farming.controller;

import java.util.HashMap;
import java.util.Map;

import com.livestock.livestock_farming.dto.request.AuthenticationRequest;
import com.livestock.livestock_farming.dto.request.IntrospectRequest;
import com.livestock.livestock_farming.dto.request.LogOutRequest;
import com.livestock.livestock_farming.dto.request.RefreshRequest;
import com.livestock.livestock_farming.dto.response.AuthenticationResponse;
import com.livestock.livestock_farming.dto.response.IntrospectResponse;
import com.livestock.livestock_farming.service.SeedDataService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.livestock.livestock_farming.service.AuthenticationService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final SeedDataService seedDataService;

    @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    @Value("${cookie.domain}")
    private String COOKIE_DOMAIN;

    @Value("${cookie.path}")
    private String COOKIE_PATH;

    @Value("${cookie.secure}")
    private boolean COOKIE_SECURE;

    @Value("${cookie.same-site}")
    private String COOKIE_SAME_SITE;

    /**
     * URL: GET /api/v1/identity/auth/public/init-data
     * Chạy trực tiếp trên trình duyệt để nạp dữ liệu mẫu
     */
    @GetMapping("/public/init-data")
    public ResponseEntity<Map<String, String>> initData() {
        log.info("[initData] Đang khởi tạo dữ liệu mẫu...");

        // Gọi service đã viết logic chuẩn ở bước trước
        String message = seedDataService.initData();

        Map<String, String> response = new HashMap<>();
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    // ========== ADMIN LOGIN ENDPOINTS ==========

    /**
     * Đăng nhập Admin - trả token qua body
     * Dành cho: Mobile, Desktop, API client
     */
    @PostMapping("/public/log-in")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        log.info("[login] Admin đăng nhập qua body: {}", request.getUserName());
        return ResponseEntity.ok(authenticationService.login(request));
    }

    /**
     * Đăng nhập Admin - lưu RT vào cookie
     * Dành cho: Web browser
     */
    @PostMapping("/public/cookie/log-in")
    public ResponseEntity<AuthenticationResponse> loginWithCookie(
            @Valid @RequestBody AuthenticationRequest request, HttpServletResponse response) {
        log.info("[loginWithCookie] Admin đăng nhập qua cookie: {}", request.getUserName());

        AuthenticationResponse authResponse = authenticationService.login(request);

        if (authResponse.isAuthenticated()) {
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            log.debug("[loginWithCookie] Đã lưu RefreshToken vào cookie");
        }

        // Trả về response không có RT (đã lưu trong cookie)
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .authenticated(authResponse.isAuthenticated())
                .accessToken(authResponse.getAccessToken())
                .accessTokenExpiresIn(authResponse.getAccessTokenExpiresIn())
                .refreshToken(null)
                .refreshTokenExpiresIn(null)
                .build());
    }

    /**
     * Làm mới token - nhận RT qua body
     */
    @PostMapping("/public/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshRequest request) {
        log.info("[refreshToken] Admin làm mới token qua body");
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    /**
     * Làm mới token - lấy RT từ cookie
     */
    @PostMapping("/public/cookie/refresh")
    public ResponseEntity<AuthenticationResponse> refreshTokenFromCookie(
            HttpServletRequest request, HttpServletResponse response) {
        log.info("[refreshTokenFromCookie] Admin làm mới token qua cookie");

        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            log.error("[refreshTokenFromCookie] Không tìm thấy RefreshToken trong cookie");
            throw new IllegalArgumentException("Không tìm thấy phiên đăng nhập. Vui lòng đăng nhập lại");
        }

        AuthenticationResponse authResponse = authenticationService.refreshToken(new RefreshRequest(refreshToken));

        if (authResponse.isAuthenticated()) {
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            log.debug("[refreshTokenFromCookie] Đã cập nhật RefreshToken trong cookie");
        }

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .authenticated(authResponse.isAuthenticated())
                .accessToken(authResponse.getAccessToken())
                .accessTokenExpiresIn(authResponse.getAccessTokenExpiresIn())
                .refreshToken(null)
                .refreshTokenExpiresIn(null)
                .build());
    }

    /**
     * Đăng xuất - nhận AT qua body
     */
    @PostMapping("/public/log-out")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogOutRequest request) {
        log.info("[logout] Admin đăng xuất qua body");
        authenticationService.logOut(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    /**
     * Đăng xuất - lấy RT từ cookie và xóa cookie
     */
    @PostMapping("/public/cookie/log-out")
    public ResponseEntity<Map<String, String>> logoutWithCookie(
            HttpServletRequest request, HttpServletResponse response) {
        log.info("[logoutWithCookie] Admin đăng xuất qua cookie");

        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            // Tạo LogOutRequest với RT để xóa khỏi whitelist
            authenticationService.logOut(new LogOutRequest(refreshToken));
            log.debug("[logoutWithCookie] Đã xóa RefreshToken khỏi whitelist");
        }

        clearRefreshTokenCookie(response);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(result);
    }

    /**
     * Kiểm tra token có hợp lệ không
     */
    @PostMapping("/public/introspect")
    public ResponseEntity<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        log.debug("[introspect] Kiểm tra token");
        return ResponseEntity.ok(authenticationService.introspect(request));
    }

    // ========== HELPER METHODS ==========

    /**
     * Lưu Refresh Token vào cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(String.format("%s=%s", COOKIE_NAME, token));
        cookieBuilder.append(String.format("; Path=%s", COOKIE_PATH));
        cookieBuilder.append("; HttpOnly");
        cookieBuilder.append(String.format("; SameSite=%s", COOKIE_SAME_SITE));
        cookieBuilder.append(String.format("; Max-Age=%d", REFRESHABLE_DURATION));

        if (COOKIE_SECURE) {
            cookieBuilder.append("; Secure");
        }

        if (COOKIE_DOMAIN != null && !COOKIE_DOMAIN.isEmpty()) {
            cookieBuilder.append(String.format("; Domain=%s", COOKIE_DOMAIN));
        }

        response.addHeader("Set-Cookie", cookieBuilder.toString());
        log.debug("[setRefreshTokenCookie] Đã set cookie");
    }

    /**
     * Lấy Refresh Token từ cookie
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Xóa Refresh Token cookie
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(String.format("%s=", COOKIE_NAME));
        cookieBuilder.append(String.format("; Path=%s", COOKIE_PATH));
        cookieBuilder.append("; HttpOnly");
        cookieBuilder.append(String.format("; SameSite=%s", COOKIE_SAME_SITE));
        cookieBuilder.append("; Max-Age=0");

        if (COOKIE_SECURE) {
            cookieBuilder.append("; Secure");
        }

        if (COOKIE_DOMAIN != null && !COOKIE_DOMAIN.isEmpty()) {
            cookieBuilder.append(String.format("; Domain=%s", COOKIE_DOMAIN));
        }

        response.addHeader("Set-Cookie", cookieBuilder.toString());
        log.debug("[clearRefreshTokenCookie] Đã xóa cookie");
    }
}
