package ax.xz.max.chess.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class LRUCache<K, V> implements Cache<K, V> {
	private final int maxEntries;

	private final CircularLinkedList<Map.Entry<K, V>> list;
	private final ConcurrentHashMap<K, CircularLinkedList<Map.Entry<K, V>>.Node> data;

	public LRUCache(int maxEntries) {
		this.maxEntries = maxEntries;

		list = new CircularLinkedList<>();
		this.data = new ConcurrentHashMap<>();
	}

	private final ReentrantLock lock = new ReentrantLock();

	private void trimOldest() {
		while (data.size() > maxEntries) {
			var oldest = list.front();

			data.remove(oldest.data().getKey());
			oldest.remove();
		}
	}

	public void clear() {
		lock.lock();
		try {
			data.clear();
			list.clear();
		} finally {
			lock.unlock();
		}
	}

	public V get(K key) {
		var result = data.get(key); // doesn't need lock
		if (result == null) return null;
		else return result.data().getValue();
	}

	public V put(K key, V value) {
		lock.lock();
		try {
			var node = list.pushBack(Map.entry(key, value));
			var old = data.put(key, node);
			if (old != null)
				old.remove();

			trimOldest();
			return old == null ? null : old.data().getValue();
		} finally {
			lock.unlock();
		}
	}

	private static final AtomicInteger cacheHits = new AtomicInteger();
	private static final AtomicInteger cacheMisses = new AtomicInteger();

	public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
		var result = get(key);

		if (result == null) cacheMisses.incrementAndGet();
		else cacheHits.incrementAndGet();
		if (Math.random() < 0.00005) {
			System.out.println("Cache hits: " + cacheHits.get() + ", cache misses: " + cacheMisses.get());
			double total = cacheHits.get() + cacheMisses.get();
			System.out.println("Hit ratio: " + (cacheHits.get() / total));
			System.out.println("Cache size: " + data.size());
		}

		if (result != null) return result;

		V value = mappingFunction.apply(key);
		put(key, value);
		return value;
	}
}
