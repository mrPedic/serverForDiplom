package com.example.com.venom.config;

import com.example.com.venom.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && !token.isEmpty()) {
            try {
                // Для простоты считаем, что токен содержит userId
                // В реальном приложении нужно декодировать JWT
                Long userId = parseUserIdFromToken(token);

                if (userId != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Токен невалидный, продолжаем без аутентификации
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Извлекаем токен из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Или из query параметра (для совместимости)
        return request.getParameter("token");
    }

    private Long parseUserIdFromToken(String token) {
        // Для простоты: если токен вида "android_token_{userId}", извлекаем userId
        if (token != null && token.startsWith("android_token_")) {
            try {
                String userIdStr = token.substring("android_token_".length());
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Для других токенов можно добавить логику декодирования JWT
        return null;
    }
}
