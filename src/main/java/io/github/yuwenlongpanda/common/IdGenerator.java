package io.github.yuwenlongpanda.common;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class IdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
