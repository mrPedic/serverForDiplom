package com.example.websocket.service

import org.springframework.stereotype.Service
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

@Service
class SubscriptionService {
    // Хранилище подписок: userId -> Set<sessionId>
    private val userConnections = ConcurrentHashMap<String, MutableSet<String>>()
    // Хранилище подписок: channel -> Set<sessionId>
    private val channelSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    // Мьютекс для потокобезопасности
    private val mutex = Mutex()

    suspend fun registerConnection(userId: String, sessionId: String) {
        mutex.withLock {
            userConnections.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
        }
    }

    suspend fun subscribe(sessionId: String, channel: String) {
        mutex.withLock {
            channelSubscriptions.computeIfAbsent(channel) { ConcurrentHashMap.newKeySet() }.add(sessionId)
        }
    }

    suspend fun unsubscribe(sessionId: String, channel: String) {
        mutex.withLock {
            channelSubscriptions[channel]?.remove(sessionId)
        }
    }

    suspend fun unsubscribeAll(sessionId: String) {
        mutex.withLock {
            // Удаляем сессию из всех каналов
            channelSubscriptions.values.forEach { it.remove(sessionId) }
            // Удаляем сессию из пользовательских подключений
            userConnections.values.forEach { it.remove(sessionId) }
        }
    }

    suspend fun getConnections(userId: String): Set<String> {
        return mutex.withLock {
            userConnections[userId]?.toSet() ?: emptySet()
        }
    }

    suspend fun getSubscribers(channel: String): Set<String> {
        return mutex.withLock {
            channelSubscriptions[channel]?.toSet() ?: emptySet()
        }
    }
}