package dev.kwlew.kCombatLog.kernel;

public interface LifecycleComponent {

    default void init() {}
    default void start() {}
    default void shutdown() {}

}
