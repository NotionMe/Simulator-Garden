package ua.notion.infrastructure.websocket;

import ua.notion.domain.entity.User;

/** Result of HTTP {@code /api/login} or {@code /api/register}. */
public record AuthSession(String token, String publicKey, User user) {}
