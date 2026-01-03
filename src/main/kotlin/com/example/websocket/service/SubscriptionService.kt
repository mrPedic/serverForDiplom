package com.example.websocket.service

import org.springframework.stereotype.Service
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class SubscriptionService {
    private val userConnections = ConcurrentHashMap<String, MutableSet<String>>()
    private val channelSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    private val sessionToChannels = ConcurrentHashMap<String, MutableSet<String>>()
    private val mutex = Mutex()

    // Статистика
    private val totalConnections = AtomicInteger(0)
    private val totalSubscriptions = AtomicInteger(0)

    suspend fun registerConnection(userId: String, sessionId: String) {
        mutex.withLock {
            userConnections.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
            totalConnections.incrementAndGet()
        }
    }

    suspend fun subscribe(sessionId: String, channel: String) {
        mutex.withLock {
            channelSubscriptions.computeIfAbsent(channel) { ConcurrentHashMap.newKeySet() }.add(sessionId)
            sessionToChannels.computeIfAbsent(sessionId) { ConcurrentHashMap.newKeySet() }.add(channel)
            totalSubscriptions.incrementAndGet()
        }
    }

    suspend fun unsubscribe(sessionId: String, channel: String) {
        mutex.withLock {
            channelSubscriptions[channel]?.remove(sessionId)
            sessionToChannels[sessionId]?.remove(channel)
            // Очищаем пустые записи
            if (channelSubscriptions[channel].isNullOrEmpty()) {
                channelSubscriptions.remove(channel)
            }
            totalSubscriptions.decrementAndGet()
        }
    }

    suspend fun unsubscribeAll(sessionId: String) {
        mutex.withLock {
            val channels = sessionToChannels[sessionId]
            channels?.forEach { channel ->
                channelSubscriptions[channel]?.remove(sessionId)
                if (channelSubscriptions[channel].isNullOrEmpty()) {
                    channelSubscriptions.remove(channel)
                }
                totalSubscriptions.decrementAndGet()
            }

            // Удаляем сессию из пользовательских подключений
            userConnections.values.forEach { connections ->
                if (connections.remove(sessionId)) {
                    totalConnections.decrementAndGet()
                }
            }

            // Очищаем пустые записи пользователей
            userConnections.entries.removeIf { it.value.isEmpty() }

            sessionToChannels.remove(sessionId)
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

    suspend fun getUserBySession(sessionId: String): String? {
        return mutex.withLock {
            userConnections.entries.find { it.value.contains(sessionId) }?.key
        }
    }

    suspend fun getChannelsBySession(sessionId: String): Set<String> {
        return mutex.withLock {
            sessionToChannels[sessionId]?.toSet() ?: emptySet()
        }
    }

    suspend fun getStats(): Map<String, Any> {
        return mutex.withLock {
            mapOf(
                "totalUsers" to userConnections.size,
                "totalConnections" to totalConnections.get(),
                "totalChannels" to channelSubscriptions.size,
                "totalSubscriptions" to totalSubscriptions.get(),
                "users" to userConnections.keys.toList(),
                "channels" to channelSubscriptions.keys.toList()
            )
        }
    }
}