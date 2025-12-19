package com.example.websocket.config

import com.example.websocket.handler.CustomNotificationHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(private val customNotificationHandler: CustomNotificationHandler) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(customNotificationHandler, "/ws")
            .setAllowedOrigins("*") // В продакшене укажите конкретные origins
    }
}