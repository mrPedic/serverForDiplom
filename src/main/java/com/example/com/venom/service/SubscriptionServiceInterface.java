package com.example.com.venom.service;

public interface SubscriptionServiceInterface {
    int broadcastToChannel(String channel, String message);
}