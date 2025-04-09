package dev.turtywurty.pepolang.semanticAnalysis;

import java.util.HashMap;

public class BiMap<K1, K2, V> extends HashMap<BiMap.Key<K1, K2>, V> {
    public V put(K1 key1, K2 key2, V value) {
        return super.put(new Key<>(key1, key2), value);
    }

    public V get(K1 key1, K2 key2) {
        return super.get(new Key<>(key1, key2));
    }

    public boolean containsKey(K1 key1, K2 key2) {
        return super.containsKey(new Key<>(key1, key2));
    }

    public V removeElement(K1 key1, K2 key2) {
        return super.remove(new Key<>(key1, key2));
    }

    public boolean removeElement(K1 key1, K2 key2, V value) {
        return super.remove(new Key<>(key1, key2), value);
    }

    public record Key<K1, K2>(K1 key1, K2 key2) {}
}
