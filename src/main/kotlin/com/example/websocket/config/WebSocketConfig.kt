package com.example.websocket.config

import com.example.websocket.handler.CustomNotificationHandler
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val customNotificationHandler: CustomNotificationHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(customNotificationHandler, "/ws")
            .addInterceptors(AuthHandshakeInterceptor())
            .setAllowedOriginPatterns("*") // Более безопасно, чем setAllowedOrigins
            .withSockJS() // Добавьте, если нужна поддержка SockJS
    }

    private class AuthHandshakeInterceptor : HandshakeInterceptor {

        override fun beforeHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            attributes: MutableMap<String, Any>
        ): Boolean {
            val queryParams = UriComponentsBuilder.fromUri(request.uri).build()
                .queryParams

            val userId = queryParams.getFirst("userId")
            val token = queryParams.getFirst("token")

            // Добавьте проверку токена/аутентификации
            if (userId.isNullOrEmpty()) {
                return false // Отклоняем соединение без userId
            }

            // Пример проверки токена (реализуйте свою логику)
            if (!validateToken(token)) {
                return false
            }

            attributes["userId"] = userId
            attributes["token"] = token.toString()
            return true
        }

        override fun afterHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            exception: Exception?
        ) {
            // Логирование успешного handshake
        }

        private fun validateToken(token: String?): Boolean {
            // Реализуйте проверку токена
            return !token.isNullOrEmpty()
        }
    }
}