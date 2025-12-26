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

@Configuration
@EnableWebSocket
class WebSocketConfig(private val customNotificationHandler: CustomNotificationHandler) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(customNotificationHandler, "/ws")
            .addInterceptors(object : HandshakeInterceptor {
                override fun beforeHandshake(
                    request: ServerHttpRequest,
                    response: ServerHttpResponse,
                    wsHandler: WebSocketHandler,
                    attributes: MutableMap<String, Any>
                ): Boolean {
                    val uri = request.uri
                    val queryParams = uri.query.split("&").associate {
                        it.split("=").let { pair -> pair[0] to (pair.getOrNull(1) ?: "") }
                    }
                    val userId = queryParams["userId"]
                    if (userId != null) {
                        attributes["userId"] = userId
                    }
                    return true
                }

                override fun afterHandshake(
                    request: ServerHttpRequest, response: ServerHttpResponse,
                    wsHandler: WebSocketHandler, exception: Exception?
                ) {}
            })
            .setAllowedOrigins("*")
    }
}