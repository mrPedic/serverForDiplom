package com.example.com.venom.controller

import com.example.websocket.handler.CustomNotificationHandler
import com.example.websocket.service.SubscriptionService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/websocket")
class WebSocketController(
    private val notificationHandler: CustomNotificationHandler,
    private val subscriptionService: SubscriptionService
) {

    @PostMapping("/send-to-user/{userId}")
    fun sendToUser(
        @PathVariable userId: String,
        @RequestBody message: String
    ): ResponseEntity<Map<String, String>> {
        notificationHandler.sendToUser(userId, message)
        return ResponseEntity.ok(mapOf("status" to "message_sent", "userId" to userId))
    }

    @PostMapping("/broadcast/{channel}")
    fun broadcastToChannel(
        @PathVariable channel: String,
        @RequestBody message: String
    ): ResponseEntity<Map<String, String>> {
        notificationHandler.broadcastToChannel(channel, message)
        return ResponseEntity.ok(mapOf("status" to "broadcast_sent", "channel" to channel))
    }

    @GetMapping("/stats")
    fun getStats(): ResponseEntity<Map<String, Any>> = runBlocking {
        val stats = subscriptionService.getStats()
        ResponseEntity.ok(stats)
    }

    @GetMapping("/user/{userId}/connections")
    fun getUserConnections(@PathVariable userId: String): ResponseEntity<Map<String, Any>> = runBlocking {
        val connections = subscriptionService.getConnections(userId)
        ResponseEntity.ok(mapOf(
            "userId" to userId,
            "connections" to connections,
            "count" to connections.size
        ))
    }
}