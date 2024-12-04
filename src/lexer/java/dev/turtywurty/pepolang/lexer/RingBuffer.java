package dev.turtywurty.pepolang.lexer;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicInteger;

public class RingBuffer<E> {
    private static final int DEFAULT_CAPACITY = 8;
    private final E[] buffer;
    private final int capacity;
    private final AtomicInteger readSequence = new AtomicInteger(0);
    private final AtomicInteger writeSequence = new AtomicInteger(-1);

    @SuppressWarnings("unchecked")
    public RingBuffer(Class<E> clazz, int capacity) {
        this.capacity = capacity < 1 ? DEFAULT_CAPACITY : capacity;
        this.buffer = (E[]) Array.newInstance(clazz, this.capacity);
    }

    public boolean offer(E element) {
        boolean isFull = (this.writeSequence.get() - this.readSequence.get()) + 1 == this.capacity;
        if(!isFull) {
            int nextWriteSequence = this.writeSequence.get() + 1;
            this.buffer[nextWriteSequence % this.capacity] = element;
            this.writeSequence.getAndIncrement();
            return true;
        }

        return false;
    }

    public @Nullable E poll() {
        boolean isEmpty = this.writeSequence.get() < this.readSequence.get();
        if(!isEmpty)
             return this.buffer[this.readSequence.getAndIncrement() % this.capacity];

        return null;
    }
}
