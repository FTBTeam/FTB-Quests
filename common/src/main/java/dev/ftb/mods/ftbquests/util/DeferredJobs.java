package dev.ftb.mods.ftbquests.util;

import java.util.ArrayDeque;
import java.util.Deque;

public enum DeferredJobs {
    INSTANCE;

    private final Deque<Runnable> deferred = new ArrayDeque<>();

    public void add(Runnable r) {
        deferred.add(r);
    }

    public void run() {
        while (!deferred.isEmpty()) {
            deferred.pop().run();
        }
    }
}
