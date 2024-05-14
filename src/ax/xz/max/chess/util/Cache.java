package ax.xz.max.chess.util;

import java.util.function.Function;

public interface Cache<K, V> {
	V get(K key);
	V put(K key, V value);
	V computeIfAbsent(K key, Function<K, V> mappingFunction);
	void clear();
}
