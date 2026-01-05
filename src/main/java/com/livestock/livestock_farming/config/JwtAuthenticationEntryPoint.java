package com.livestock.livestock_farming.config;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livestock.livestock_farming.dto.response.ApiResponse;
import com.livestock.livestock_farming.exception.ErrorCode; // Đảm bảo đã có enum này
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component; // Thêm Annotation này

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        // Giả sử ErrorCode.UNAUTHENTICATED trả về code 1001 và message "Unauthenticated"
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Trả về 401 chuẩn HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}