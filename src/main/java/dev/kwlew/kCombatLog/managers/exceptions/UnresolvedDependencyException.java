package dev.kwlew.kCombatLog.managers.exceptions;

public class UnresolvedDependencyException extends RuntimeException {
    public UnresolvedDependencyException(Class<?> type) {
        super("Unresolved dependency: " + type.getName());
    }
}
