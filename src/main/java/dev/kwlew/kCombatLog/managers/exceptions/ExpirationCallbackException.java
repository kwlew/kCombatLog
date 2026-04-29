package dev.kwlew.kCombatLog.managers.exceptions;

import java.util.UUID;

public class ExpirationCallbackException extends RuntimeException {
    public ExpirationCallbackException(UUID uuid) {
        super("Could not get expiration of " + uuid.toString());
    }
}
