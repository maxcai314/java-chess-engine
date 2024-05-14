package ax.xz.max.chess.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

public class LimitedCache<K, V> {
	private final int maxEntries;
	private final ConcurrentHashMap<K, V> data;
	private final ConcurrentLinkedDeque<K> lastAdded = new ConcurrentLinkedDeque<>();

	public LimitedCache(int maxEntries) {
		this.maxEntries = maxEntries;
		this.data = new ConcurrentHashMap<>(maxEntries);
	}

	private void trimOldest() {
		while (data.size() > maxEntries) {
			var key = lastAdded.poll();
			if (key == null) break; // protects from race condition
			data.remove(key);
		}
	}

	public V get(K key) {
		return data.get(key);
	}

	public V put(K key, V value) {
		var result = data.put(key, value);
		if (result == null) { // added something new
			lastAdded.addLast(key);
			trimOldest();
		}
		return result;
	}

	public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
		boolean contains = data.containsKey(key); // non-critical race condition
		var result = data.computeIfAbsent(key, mappingFunction);
		if (!contains) { // added something new
			lastAdded.addLast(key);
			trimOldest();
		}
		return result;
	}

	public void clear() {
		lastAdded.clear();
		data.clear(); // shouldn't cause memory leaks
	}

	public String toString() {
		return data.toString();
	}
}
