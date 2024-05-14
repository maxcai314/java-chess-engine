package ax.xz.max.chess.util;

public class CircularLinkedList<T> {
	private final Node sentinel ;

	public CircularLinkedList() {
		this.sentinel = new Node(null);
		sentinel.next = sentinel.prev = sentinel;
	}

	public class Node {
		private final T data;

		private Node prev = null;
		private Node next = null;

		private Node(T data) {
			this.data = data;
		}

		public void remove() {
			if (this == sentinel)
				throw new IllegalStateException("Cannot remove sentinel");

			prev.next = next;
			next.prev = prev;
		}

		public T data() {
			return data;
		}
	}

	public Node front() {
		if (sentinel.next == sentinel)
			return null;

		return sentinel.next;
	}

	public Node back() {
		if (sentinel.prev == sentinel)
			return null;

		return sentinel.prev;
	}

	public void clear() {
		sentinel.next = sentinel.prev = sentinel;
	}

	public Node pushBack(T data) {
		var node = new Node(data);

		node.prev = sentinel.prev;
		node.next = sentinel;

		node.prev.next = node;
		node.next.prev = node;

		return node;
	}
}
