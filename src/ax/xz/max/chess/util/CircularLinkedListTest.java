package ax.xz.max.chess.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircularLinkedListTest {
	@Test
	void test() {
		var list = new CircularLinkedList<Integer>();
		var node1 = list.pushBack(1);
		var node2 = list.pushBack(2);
		var node3 = list.pushBack(3);

		assertEquals(1, list.front().data());
		assertEquals(3, list.back().data());

		node2.remove();

		assertEquals(1, list.front().data());
		assertEquals(3, list.back().data());

		node1.remove();

		assertEquals(3, list.front().data());
		assertEquals(3, list.back().data());

		node3.remove();

		assertNull(list.front());
		assertNull(list.back());
	}

}