package com.example.com.venom.websocket

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component 
class MyWebSocketHandler : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(MyWebSocketHandler::class.java)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        logger.info("WebSocket подключился: ${session.id}")
        session.sendMessage(TextMessage("Привет! Ты подключён к Venom чату"))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        logger.info("Сообщение от ${session.id}: $payload")

        // Рассылка всем подключённым
        sessions.values.forEach { s ->
            try {
                s.sendMessage(TextMessage("Пользователь сказал: $payload"))
            } catch (e: Exception) {
                logger.error("Ошибка отправки клиенту ${s.id}", e)
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        logger.info("WebSocket отключился: ${session.id}")
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Ошибка WebSocket ${session.id}", exception)
    }
}