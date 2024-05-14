package ax.xz.max.chess.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class LRUCache<K, V> implements Cache<K, V> {
	private final int maxEntries;
	private final ConcurrentHashMap<K, Node> data;

	public LRUCache(int maxEntries) {
		this.maxEntries = maxEntries;
		this.data = new ConcurrentHashMap<>(maxEntries);
	}

	private final ReentrantLock lock = new ReentrantLock();

	// everything is guarded by the lock
	private Node head = null;
	private Node tail = null;

	private class Node {
		private final K key;
		private final V value;
		private Node prev = null;
		private Node next = null;

		private Node(K key, V value) {
			this.key = key;
			this.value = value;
		}

		private void remove() {
			if (prev != null) {
				prev.next = this.next;
			}
			if (next != null) {
				next.prev = this.prev;
			}
			if (head == this) {
				head = next;
			}
			if (tail == this) {
				tail = prev;
			}

			this.prev = null;
			this.next = null;
		}

		public void push() {
			if (head == null) {
				head = this;
				tail = this;
			} else {
				tail.next = this;
				this.prev = tail;
			}
		}
	}

	private void trimOldest() {
		while (data.size() > maxEntries) {
			data.remove(head.key);
			head.remove();
		}
	}

	public void clear() {
		lock.lock();
		try {
			data.clear();
			head = null;
			tail = null;
		} finally {
			lock.unlock();
		}
	}

	public V get(K key) {
		var result = data.get(key); // doesn't need lock
		if (result == null) return null;
		else return result.value;
	}

	public V put(K key, V value) {
		lock.lock();
		try {
			var node = new Node(key, value);
			var old = data.put(key, node);
			if (old != null)
				old.remove();

			node.push();

			trimOldest();
			return old == null ? null : old.value;
		} finally {
			lock.unlock();
		}
	}

	private final AtomicInteger cacheHits = new AtomicInteger();
	private final AtomicInteger cacheMisses = new AtomicInteger();

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
