package com.example.websocket.handler

import com.example.websocket.service.SubscriptionService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class CustomNotificationHandler(
    private val subscriptionService: SubscriptionService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(CustomNotificationHandler::class.java)

    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val sessionToUserId = ConcurrentHashMap<String, String>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    companion object {
        private const val PING_INTERVAL_MS = 30000L // 30 секунд
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session

        val userId = session.attributes["userId"] as? String
        if (userId == null) {
            logger.warn("WebSocket connection established without userId")
            session.close(CloseStatus(4001, "UserId required"))
            return
        }

        sessionToUserId[session.id] = userId

        scope.launch {
            try {
                subscriptionService.registerConnection(userId, session.id)
                logger.info("User $userId connected with session ${session.id}")

                // Отправляем приветственное сообщение
                sendMessage(session,
                    """{"type": "connected", "sessionId": "${session.id}", "timestamp": "${System.currentTimeMillis()}"}"""
                )

                // Запускаем пинг
                startPing(session)

            } catch (e: Exception) {
                logger.error("Error registering connection for user $userId", e)
                session.close(CloseStatus(500, "Internal server error"))
            }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        scope.launch {
            try {
                val request = objectMapper.readTree(message.payload)
                val type = request.get("type").asText()
                val requestId = request.get("requestId")?.asText() ?: ""

                when (type) {
                    "subscribe" -> {
                        val channel = request.get("channel").asText()
                        subscriptionService.subscribe(session.id, channel)
                        sendMessage(session,
                            """{"type": "subscribed", "channel": "$channel", "requestId": "$requestId"}"""
                        )
                        logger.info("Session ${session.id} subscribed to channel $channel")
                    }
                    "unsubscribe" -> {
                        val channel = request.get("channel").asText()
                        subscriptionService.unsubscribe(session.id, channel)
                        sendMessage(session,
                            """{"type": "unsubscribed", "channel": "$channel", "requestId": "$requestId"}"""
                        )
                    }
                    "ping" -> {
                        sendMessage(session,
                            """{"type": "pong", "timestamp": "${System.currentTimeMillis()}", "requestId": "$requestId"}"""
                        )
                    }
                    else -> {
                        sendMessage(session,
                            """{"type": "error", "message": "Unknown message type: $type", "requestId": "$requestId"}"""
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing message: ${message.payload}", e)
                sendMessage(session,
                    """{"type": "error", "message": "Invalid message format", "requestId": ""}"""
                )
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = sessionToUserId[session.id]

        sessions.remove(session.id)
        sessionToUserId.remove(session.id)

        scope.launch {
            subscriptionService.unsubscribeAll(session.id)
        }

        logger.info("User $userId disconnected with status: $status")
    }

    private suspend fun sendMessage(session: WebSocketSession, message: String) {
        if (session.isOpen) {
            try {
                session.sendMessage(TextMessage(message))
            } catch (e: Exception) {
                logger.error("Error sending message to session ${session.id}", e)
            }
        }
    }

    fun sendToUser(userId: String, message: String) {
        scope.launch {
            val connections = subscriptionService.getConnections(userId)
            connections.forEach { sessionId ->
                sessions[sessionId]?.takeIf { it.isOpen }?.let { session ->
                    try {
                        session.sendMessage(TextMessage(message))
                    } catch (e: Exception) {
                        logger.error("Error sending message to user $userId, session $sessionId", e)
                    }
                }
            }
        }
    }

    fun broadcastToChannel(channel: String, message: String) {
        scope.launch {
            val subscribers = subscriptionService.getSubscribers(channel)
            subscribers.forEach { sessionId ->
                sessions[sessionId]?.takeIf { it.isOpen }?.let { session ->
                    try {
                        session.sendMessage(TextMessage(message))
                    } catch (e: Exception) {
                        logger.error("Error broadcasting to channel $channel, session $sessionId", e)
                    }
                }
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}: ${exception.message}", exception)
    }

    private fun startPing(session: WebSocketSession) {
        scope.launch {
            while (session.isOpen) {
                delay(PING_INTERVAL_MS)
                if (session.isOpen) {
                    try {
                        session.sendMessage(TextMessage("""{"type": "ping"}"""))
                    } catch (e: Exception) {
                        logger.error("Ping failed for session ${session.id}", e)
                        break
                    }
                }
            }
        }
    }

    @PreDestroy
    fun destroy() {
        scope.cancel()
        // Закрываем все активные сессии
        sessions.values.forEach { session ->
            if (session.isOpen) {
                try {
                    session.close(CloseStatus(1001, "Server shutdown"))
                } catch (e: Exception) {
                    logger.error("Error closing session ${session.id}", e)
                }
            }
        }
    }
}