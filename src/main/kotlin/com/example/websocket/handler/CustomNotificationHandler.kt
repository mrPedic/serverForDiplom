package com.example.websocket.handler

import com.example.websocket.service.SubscriptionService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class CustomNotificationHandler(
    private val subscriptionService: SubscriptionService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        // Получаем userId из атрибутов (можно настроить через HandshakeInterceptor)
        val userId = session.attributes["userId"] as? String
        userId?.let {
            scope.launch {
                subscriptionService.registerConnection(it, session.id)
            }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        scope.launch {
            try {
                val request = objectMapper.readTree(message.payload)
                val type = request.get("type").asText()

                when (type) {
                    "subscribe" -> {
                        val channel = request.get("channel").asText()
                        subscriptionService.subscribe(session.id, channel)
                        sendMessage(session, """{"type": "subscribed", "channel": "$channel"}""")
                    }
                    "unsubscribe" -> {
                        val channel = request.get("channel").asText()
                        subscriptionService.unsubscribe(session.id, channel)
                        sendMessage(session, """{"type": "unsubscribed", "channel": "$channel"}""")
                    }
                    else -> {
                        sendMessage(session, """{"type": "error", "message": "Unknown message type: $type"}""")
                    }
                }
            } catch (e: Exception) {
                sendMessage(session, """{"type": "error", "message": "${e.message}"}""")
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        scope.launch {
            subscriptionService.unsubscribeAll(session.id)
        }
    }

    private fun sendMessage(session: WebSocketSession, message: String) {
        if (session.isOpen) {
            scope.launch {
                session.sendMessage(TextMessage(message))
            }
        }
    }

    fun sendToUser(userId: String, message: String) {
        scope.launch {
            val connections = subscriptionService.getConnections(userId)
            connections.forEach { sessionId ->
                sessions[sessionId]?.takeIf { it.isOpen }?.sendMessage(TextMessage(message))
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        println("Transport error: ${exception.message}")
    }

    @PreDestroy
    fun destroy() {
        scope.cancel()
    }


}