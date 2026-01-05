package com.example.com.venom.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SubscriptionService implements SubscriptionServiceInterface {

    private final Map<String, Set<String>> userConnections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> channelSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionToChannels = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUserId = new ConcurrentHashMap<>();
    private final Map<String, Instant> sessionLastActivity = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    // Статистика
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger totalSubscriptions = new AtomicInteger(0);
    private final AtomicInteger connectionAttempts = new AtomicInteger(0);
    private final AtomicInteger failedConnections = new AtomicInteger(0);

    public void registerConnection(String userId, String sessionId) {
        lock.lock();
        try {
            connectionAttempts.incrementAndGet();
            try {
                userConnections.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                        .add(sessionId);
                sessionToUserId.put(sessionId, userId);
                sessionLastActivity.put(sessionId, Instant.now());
                totalConnections.incrementAndGet();
            } catch (Exception e) {
                failedConnections.incrementAndGet();
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean subscribe(String sessionId, String channel) {
        lock.lock();
        try {
            String userId = sessionToUserId.get(sessionId);
            if (userId == null) {
                return false; // Сессия не зарегистрирована
            }

            channelSubscriptions.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
            sessionToChannels.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                    .add(channel);
            sessionLastActivity.put(sessionId, Instant.now());
            totalSubscriptions.incrementAndGet();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean unsubscribe(String sessionId, String channel) {
        lock.lock();
        try {
            Set<String> channelSessions = channelSubscriptions.get(channel);
            boolean removed = channelSessions != null && channelSessions.remove(sessionId);

            Set<String> sessionChannels = sessionToChannels.get(sessionId);
            if (sessionChannels != null) {
                sessionChannels.remove(channel);
            }

            // Очищаем пустые записи
            if (channelSessions != null && channelSessions.isEmpty()) {
                channelSubscriptions.remove(channel);
            }

            if (removed) {
                totalSubscriptions.decrementAndGet();
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    public void unsubscribeAll(String sessionId) {
        lock.lock();
        try {
            Set<String> channels = sessionToChannels.get(sessionId);
            if (channels != null) {
                for (String channel : channels) {
                    Set<String> channelSessions = channelSubscriptions.get(channel);
                    if (channelSessions != null) {
                        channelSessions.remove(sessionId);
                        if (channelSessions.isEmpty()) {
                            channelSubscriptions.remove(channel);
                        }
                        totalSubscriptions.decrementAndGet();
                    }
                }
            }

            // Удаляем сессию из пользовательских подключений
            String userId = sessionToUserId.get(sessionId);
            if (userId != null) {
                Set<String> userSessions = userConnections.get(userId);
                if (userSessions != null) {
                    userSessions.remove(sessionId);
                    if (userSessions.isEmpty()) {
                        userConnections.remove(userId);
                    }
                    totalConnections.decrementAndGet();
                }
            }

            // Очищаем метаданные сессии
            sessionToUserId.remove(sessionId);
            sessionToChannels.remove(sessionId);
            sessionLastActivity.remove(sessionId);
        } finally {
            lock.unlock();
        }
    }

    public void updateActivity(String sessionId) {
        lock.lock();
        try {
            sessionLastActivity.put(sessionId, Instant.now());
        } finally {
            lock.unlock();
        }
    }

    public int cleanupInactiveSessions(long timeoutMinutes) {
        lock.lock();
        try {
            Instant now = Instant.now();
            Duration timeout = Duration.ofMinutes(timeoutMinutes);
            List<String> inactiveSessions = new ArrayList<>();

            for (Map.Entry<String, Instant> entry : sessionLastActivity.entrySet()) {
                if (Duration.between(entry.getValue(), now).compareTo(timeout) > 0) {
                    inactiveSessions.add(entry.getKey());
                }
            }

            for (String sessionId : inactiveSessions) {
                unsubscribeAll(sessionId);
            }

            return inactiveSessions.size();
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getConnections(String userId) {
        lock.lock();
        try {
            Set<String> connections = userConnections.get(userId);
            return connections != null ? new HashSet<>(connections) : Collections.emptySet();
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getSubscribers(String channel) {
        lock.lock();
        try {
            Set<String> subscribers = channelSubscriptions.get(channel);
            return subscribers != null ? new HashSet<>(subscribers) : Collections.emptySet();
        } finally {
            lock.unlock();
        }
    }

    public String getUserBySession(String sessionId) {
        lock.lock();
        try {
            return sessionToUserId.get(sessionId);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getChannelsBySession(String sessionId) {
        lock.lock();
        try {
            Set<String> channels = sessionToChannels.get(sessionId);
            return channels != null ? new HashSet<>(channels) : Collections.emptySet();
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getActiveUsers() {
        lock.lock();
        try {
            Set<String> activeUsers = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : userConnections.entrySet()) {
                String userId = entry.getKey();
                Set<String> sessions = entry.getValue();

                boolean hasActiveSession = sessions.stream()
                        .anyMatch(sessionId -> {
                            Instant lastActivity = sessionLastActivity.get(sessionId);
                            if (lastActivity == null) return false;
                            return Duration.between(lastActivity, Instant.now())
                                    .compareTo(Duration.ofMinutes(5)) < 0;
                        });

                if (hasActiveSession) {
                    activeUsers.add(userId);
                }
            }
            return activeUsers;
        } finally {
            lock.unlock();
        }
    }

    public Map<String, Object> getStats() {
        lock.lock();
        try {
            Set<String> activeUsers = getActiveUsers();
            long inactiveSessions = sessionLastActivity.entrySet().stream()
                    .filter(entry -> Duration.between(entry.getValue(), Instant.now())
                            .compareTo(Duration.ofMinutes(5)) > 0)
                    .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userConnections.size());
            stats.put("activeUsers", activeUsers.size());
            stats.put("totalConnections", totalConnections.get());
            stats.put("totalChannels", channelSubscriptions.size());
            stats.put("totalSubscriptions", totalSubscriptions.get());
            stats.put("inactiveSessions", inactiveSessions);
            stats.put("connectionAttempts", connectionAttempts.get());
            stats.put("failedConnections", failedConnections.get());
            stats.put("uptime", "OK");
            stats.put("timestamp", Instant.now().toString());
            stats.put("users", new ArrayList<>(userConnections.keySet()));
            stats.put("activeUserIds", new ArrayList<>(activeUsers));
            stats.put("channels", new ArrayList<>(channelSubscriptions.keySet()));

            return stats;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int broadcastToChannel(String channel, String message) {
        // Этот метод должен быть реализован в CustomNotificationHandler
        // Здесь возвращаем 0, так как реализация будет в другом месте
        return 0;
    }
}