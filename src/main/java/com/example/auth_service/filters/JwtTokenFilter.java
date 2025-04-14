package com.example.auth_service.filters;



import com.example.auth_service.components.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final HttpServletResponse httpServletResponse;
    @Value("${api.prefix}") // Lấy giá trị từ file cấu hình
    private String apiPrefix;

    private final UserDetailsService userDetailsService; // Dịch vụ tải thông tin người dùng
    private final JwtTokenUtil jwtTokenUtil; // Tiện ích xử lý JWT token

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Bỏ qua xác thực nếu yêu cầu thuộc các URL bypass
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;  // Đảm bảo không tiếp tục sau khi xử lý xong
            }

            // Lấy header Authorization
            final String authorizationHeader = request.getHeader("Authorization");

            // Kiểm tra header có hợp lệ không
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }
                return;
            }

            // Trích xuất token từ header
            final String token = authorizationHeader.substring(7);
            // Lấy số điện thoại (username) từ token
            final String email = jwtTokenUtil.extractUsername(token);
            // Nếu số điện thoại tồn tại và chưa có xác thực trong SecurityContext
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Lấy thông tin người dùng từ cơ sở dữ liệu
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Kiểm tra token có hợp lệ không
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    // Tạo đối tượng xác thực
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    // Gắn chi tiết của request vào đối tượng xác thực
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Đặt xác thực trong SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            // Tiếp tục chuỗi filter
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Kiểm tra nếu phản hồi chưa được gửi, thì gửi lỗi
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            } else {
                // Log lỗi nếu phản hồi đã bị commit
                logger.error("Response was already committed. Cannot send error.", e);
            }
        }
    }

    // Kiểm tra URL có được bỏ qua xác thực hay không
    private boolean isBypassToken(@NotNull HttpServletRequest request) {
        // Danh sách các API không yêu cầu xác thực
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/auth/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/otp/send", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/otp/verify", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/login-google", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/reset-password", apiPrefix), "POST"),
                Pair.of(String.format("%s/auth/login", apiPrefix), "POST")


        );

        // Duyệt danh sách và kiểm tra đường dẫn và phương thức HTTP
        for (Pair<String, String> bypassToken : bypassTokens) {
            if (request.getServletPath().contains(bypassToken.getFirst()) &&
                    request.getMethod().equalsIgnoreCase(bypassToken.getSecond())) {
                return true; // Bypass xác thực
            }
        }
        return false; // Không bypass, yêu cầu xác thực
    }
}
